package com.gastoclaro.domain.usecase

import com.gastoclaro.core.util.TextNormalizer
import com.gastoclaro.domain.model.KnownCounterparty
import com.gastoclaro.domain.repository.KnownCounterpartyRepository
import com.gastoclaro.domain.repository.TransactionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkTransactionInternalUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val knownCounterpartyRepository: KnownCounterpartyRepository
) {

    suspend operator fun invoke(transactionId: Long, rememberCounterparty: Boolean = true) {
        val transaction = transactionRepository.getTransaction(transactionId) ?: return
        transactionRepository.update(transaction.copy(isTransferInternal = true))

        if (rememberCounterparty) {
            transaction.merchant?.let { merchant ->
                val normalized = TextNormalizer.normalizeCompact(merchant)
                if (normalized.isNotEmpty()) {
                    knownCounterpartyRepository.upsert(
                        KnownCounterparty(identifier = normalized, isMine = true, accountId = null)
                    )
                }
            }
        }
    }
}
