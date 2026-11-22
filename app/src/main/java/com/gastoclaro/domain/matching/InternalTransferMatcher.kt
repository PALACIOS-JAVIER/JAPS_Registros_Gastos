package com.gastoclaro.domain.matching

import com.gastoclaro.domain.model.Account
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.TransactionType
import com.gastoclaro.domain.repository.AccountRepository
import com.gastoclaro.domain.repository.KnownCounterpartyRepository
import com.gastoclaro.domain.repository.SettingsRepository
import com.gastoclaro.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InternalTransferMatcher @Inject constructor(
    private val accountRepository: AccountRepository,
    private val knownCounterpartyRepository: KnownCounterpartyRepository,
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository
) {

    suspend fun classify(
        transaction: Transaction
    ): TransferClassification {
        if (!transaction.type.isTransfer) return TransferClassification.External

        val merchant = transaction.merchant
        val account = accountRepository.getAccount(transaction.accountId)
        val accounts = accountRepository.getAllAccounts()

        // 1. Known counterparty rule (learned / manual)
        if (merchant != null) {
            val counterparties = knownCounterpartyRepository.getAll()
            val knownMine = counterparties.firstOrNull { it.isMine && TransferMatching.namesMatch(it.identifier, merchant) }
            if (knownMine != null) {
                return TransferClassification.Internal(
                    reason = InternalReason.KNOWN_COUNTERPARTY,
                    counterpartAccountId = knownMine.accountId,
                    oppositeTransaction = null
                )
            }
        }

        // 2. Match against a registered account (name / holder / last4 / phone)
        val matchedAccount = findAccountByCounterparty(accounts, merchant, transaction.accountLast4)
        if (matchedAccount != null && matchedAccount.id != transaction.accountId) {
            return TransferClassification.Internal(
                reason = InternalReason.ACCOUNT_MATCH,
                counterpartAccountId = matchedAccount.id,
                oppositeTransaction = null
            )
        }

        // 3. Opposite transaction within the configurable window (with commission tolerance)
        val windowMillis = settingsRepository.matchWindowMinutes.first() * 60_000L
        val opposite = findOppositeCandidate(transaction, windowMillis)
        if (opposite != null) {
            return TransferClassification.Internal(
                reason = InternalReason.OPPOSITE_TRANSACTION,
                counterpartAccountId = opposite.accountId,
                oppositeTransaction = opposite
            )
        }

        return TransferClassification.External
    }

    private suspend fun findOppositeCandidate(transaction: Transaction, windowMillis: Long): Transaction? {
        val oppositeType = when (transaction.type) {
            TransactionType.TRANSFER_IN -> TransactionType.TRANSFER_OUT
            TransactionType.TRANSFER_OUT -> TransactionType.TRANSFER_IN
            else -> return null
        }
        val tolerance = TransferMatching.COMMISSION_TOLERANCE
        val (minAmount, maxAmount) = when (transaction.type) {
            TransactionType.TRANSFER_IN ->
                transaction.amount to (transaction.amount * (1.0 + tolerance)).toLong()
            TransactionType.TRANSFER_OUT ->
                (transaction.amount * (1.0 - tolerance)).toLong() to transaction.amount
            else -> return null
        }
        val from = transaction.dateTime - windowMillis
        val to = transaction.dateTime + windowMillis

        val candidates = transactionRepository.findMatchCandidates(
            type = oppositeType,
            minAmount = minAmount.coerceAtLeast(0L),
            maxAmount = maxAmount,
            targetAmount = transaction.amount,
            from = from,
            to = to
        )
        return TransferMatching.findOpposite(transaction, candidates, windowMillis, tolerance)
    }

    private fun findAccountByCounterparty(
        accounts: List<Account>,
        merchant: String?,
        last4: String?
    ): Account? {
        if (merchant != null) {
            accounts.firstOrNull { account ->
                val nameMatch = account.holderName?.let { TransferMatching.namesMatch(it, merchant) } == true ||
                    TransferMatching.namesMatch(account.name, merchant)
                val phoneMatch = account.phoneNumber?.let { phone ->
                    val normalizedPhone = phone.filter { it.isDigit() }
                    val merchantDigits = merchant.filter { it.isDigit() }
                    normalizedPhone.isNotEmpty() && normalizedPhone.contains(merchantDigits) ||
                        merchantDigits.isNotEmpty() && merchant.contains(phone)
                } == true
                nameMatch || phoneMatch
            }?.let { return it }
        }
        if (last4 != null) {
            accounts.firstOrNull { it.last4Digits == last4 }?.let { return it }
        }
        return null
    }
}
