package com.gastoclaro.domain.usecase

import com.gastoclaro.core.util.DedupHash
import com.gastoclaro.domain.matching.InternalTransferMatcher
import com.gastoclaro.domain.matching.TransferClassification
import com.gastoclaro.domain.model.Account
import com.gastoclaro.domain.model.AccountType
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.TransactionStatus
import com.gastoclaro.domain.model.UnrecognizedMessage
import com.gastoclaro.domain.parser.ParsedTransaction
import com.gastoclaro.domain.parser.RawMessage
import com.gastoclaro.domain.parser.BankNames
import com.gastoclaro.domain.parser.ParserEngine
import com.gastoclaro.domain.repository.AccountRepository
import com.gastoclaro.domain.repository.CategoryRuleRepository
import com.gastoclaro.domain.repository.TransactionRepository
import com.gastoclaro.domain.repository.UnrecognizedMessageRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessDetectedMessageUseCase @Inject constructor(
    private val parserEngine: ParserEngine,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRuleRepository: CategoryRuleRepository,
    private val unrecognizedMessageRepository: UnrecognizedMessageRepository,
    private val internalTransferMatcher: InternalTransferMatcher
) {

    suspend operator fun invoke(raw: RawMessage): DetectionResult {
        val result = parserEngine.parse(raw) ?: run {
            unrecognizedMessageRepository.insert(
                UnrecognizedMessage(
                    text = raw.text,
                    source = raw.source,
                    appPackage = raw.appPackage,
                    sender = raw.sender,
                    receivedAt = raw.receivedAt
                )
            )
            return DetectionResult.Unrecognized
        }

        val parsed = result.parsed
        val account = resolveAccount(parsed, result.parserId)

        val rawTextHash = DedupHash.compute(
            amount = parsed.amount,
            dateTime = parsed.dateTime,
            reference = parsed.reference,
            account = parsed.accountLast4 ?: result.parserId
        )
        if (transactionRepository.existsByRawTextHash(rawTextHash)) {
            return DetectionResult.Duplicate
        }

        val categoryId = parsed.merchant?.let { categoryRuleRepository.findMatchingRule(it)?.categoryId }

        val preliminary = Transaction(
            accountId = account.id,
            type = parsed.type,
            amount = parsed.amount,
            dateTime = parsed.dateTime,
            merchant = parsed.merchant,
            categoryId = categoryId,
            source = raw.source,
            rawTextHash = rawTextHash,
            status = TransactionStatus.CONFIRMED,
            reference = parsed.reference,
            accountLast4 = parsed.accountLast4,
            balanceAfter = parsed.balanceAfter,
            isTransferInternal = false
        )

        val classification = internalTransferMatcher.classify(preliminary)

        val transaction = when (classification) {
            is TransferClassification.Internal -> {
                val opposite = classification.oppositeTransaction
                val groupId = opposite?.transferGroupId ?: UUID.randomUUID().toString()
                preliminary.copy(
                    isTransferInternal = true,
                    transferGroupId = groupId,
                    linkedAccountId = classification.counterpartAccountId ?: opposite?.accountId,
                    categoryId = null
                )
            }
            TransferClassification.External -> preliminary
        }

        val id = transactionRepository.insert(transaction)
        val saved = transaction.copy(id = id)

        if (classification is TransferClassification.Internal) {
            classification.oppositeTransaction?.let { opposite ->
                transactionRepository.update(
                    opposite.copy(
                        isTransferInternal = true,
                        transferGroupId = saved.transferGroupId,
                        linkedAccountId = saved.accountId
                    )
                )
            }
        }

        return DetectionResult.Detected(saved)
    }

    private suspend fun resolveAccount(parsed: ParsedTransaction, parserId: String): Account {
        val accounts = accountRepository.getAllAccounts()

        parsed.accountLast4?.let { last4 ->
            accounts.firstOrNull { it.last4Digits == last4 }?.let { return it }
        }

        val bankName = BankNames.display(parserId)
        accounts.firstOrNull {
            it.bank.equals(bankName, ignoreCase = true) || it.name.equals(bankName, ignoreCase = true)
        }?.let { return it }

        val type = when {
            BankNames.isWallet(parserId) -> AccountType.WALLET
            BankNames.isCardBank(parserId) -> AccountType.CARD
            else -> AccountType.SAVINGS
        }
        val newAccount = Account(
            name = bankName,
            bank = bankName,
            type = type,
            last4Digits = parsed.accountLast4,
            icon = "account_balance_wallet"
        )
        val id = accountRepository.upsertAccount(newAccount)
        return newAccount.copy(id = id)
    }
}
