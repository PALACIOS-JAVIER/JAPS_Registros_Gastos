package com.gastoclaro.domain.usecase

import com.gastoclaro.core.util.DateFormats
import com.gastoclaro.core.util.MoneyFormatter
import com.gastoclaro.core.util.TextNormalizer
import com.gastoclaro.domain.model.PendingQuestion
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.TransactionType
import com.gastoclaro.domain.repository.AccountRepository
import com.gastoclaro.domain.repository.PendingQuestionRepository
import com.gastoclaro.domain.repository.SettingsRepository
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnqueueExpenseQuestionUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val pendingQuestionRepository: PendingQuestionRepository,
    private val accountRepository: AccountRepository
) {

    suspend operator fun invoke(transaction: Transaction): Boolean {
        if (!settingsRepository.bubbleEnabled.first()) return false
        if (transaction.isTransferInternal) return false
        if (transaction.type !in EXPENSE_TYPES) return false
        if (transaction.categoryId != null) return false
        if (transaction.amount < settingsRepository.bubbleMinAmount.first()) return false

        val neverAsk = settingsRepository.neverAskCounterparties.first()
        transaction.merchant?.let { merchant ->
            val norm = TextNormalizer.normalizeCompact(merchant)
            if (norm.isNotEmpty() && neverAsk.any { TextNormalizer.normalizeCompact(it) == norm }) {
                return false
            }
        }

        if (isInDnd()) return false

        val accountName = transaction.accountId.let { accountRepository.getAccount(it)?.name }
        val questionText = buildString {
            append("Detecté un gasto de ${MoneyFormatter.formatShort(transaction.amount)}")
            transaction.merchant?.let { append(" a $it") }
            accountName?.let { append(" desde $it") }
            append(" a las ${DateFormats.time(transaction.dateTime)}. ¿De qué fue?")
        }

        pendingQuestionRepository.enqueue(
            PendingQuestion(
                transactionId = transaction.id,
                createdAt = System.currentTimeMillis(),
                questionText = questionText
            )
        )
        return true
    }

    private suspend fun isInDnd(): Boolean {
        val start = settingsRepository.dndStart.first()
        val end = settingsRepository.dndEnd.first()
        return isInDndWindow(start, end, LocalTime.now())
    }

    companion object {
        val EXPENSE_TYPES = setOf(
            TransactionType.EXPENSE, TransactionType.WITHDRAWAL, TransactionType.TRANSFER_OUT
        )

        fun isInDndWindow(start: String, end: String, now: LocalTime): Boolean {
            val startTime = runCatching { LocalTime.parse(start) }.getOrNull() ?: return false
            val endTime = runCatching { LocalTime.parse(end) }.getOrNull() ?: return false
            return if (startTime <= endTime) {
                now >= startTime && now <= endTime
            } else {
                now >= startTime || now <= endTime
            }
        }
    }
}
