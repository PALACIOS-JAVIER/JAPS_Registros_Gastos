package com.gastoclaro.domain.usecase

import com.gastoclaro.core.util.TextNormalizer
import com.gastoclaro.domain.bubble.AnswerResult
import com.gastoclaro.domain.bubble.ResponseInterpreter
import com.gastoclaro.domain.model.Category
import com.gastoclaro.domain.model.CategoryRule
import com.gastoclaro.domain.model.CategoryType
import com.gastoclaro.domain.model.PendingQuestion
import com.gastoclaro.domain.repository.CategoryRepository
import com.gastoclaro.domain.repository.CategoryRuleRepository
import com.gastoclaro.domain.repository.PendingQuestionRepository
import com.gastoclaro.domain.repository.TransactionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnswerQuestionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val categoryRuleRepository: CategoryRuleRepository,
    private val pendingQuestionRepository: PendingQuestionRepository,
    private val responseInterpreter: ResponseInterpreter,
    private val markTransactionInternal: MarkTransactionInternalUseCase
) {

    suspend fun markInternal(question: PendingQuestion): AnswerResult {
        if (transactionRepository.getTransaction(question.transactionId) == null) {
            pendingQuestionRepository.dequeue(question)
            return AnswerResult.NotFound
        }
        markTransactionInternal(question.transactionId, rememberCounterparty = true)
        pendingQuestionRepository.dequeue(question)
        return AnswerResult.MarkedInternal
    }

    suspend fun answer(question: PendingQuestion, response: String): AnswerResult {
        val transaction = transactionRepository.getTransaction(question.transactionId)
            ?: run {
                pendingQuestionRepository.dequeue(question)
                return AnswerResult.NotFound
            }
        val categories = categoryRepository.getAllCategories().filter { it.type == CategoryType.EXPENSE }
        val category = responseInterpreter.interpret(response, categories)
            ?: return AnswerResult.NeedCategory
        applyCategory(question, transaction, category, response)
        return AnswerResult.Categorized(category, response)
    }

    suspend fun assignCategory(question: PendingQuestion, categoryId: Long, note: String?): AnswerResult {
        val transaction = transactionRepository.getTransaction(question.transactionId)
            ?: run {
                pendingQuestionRepository.dequeue(question)
                return AnswerResult.NotFound
            }
        val category = categoryRepository.getCategory(categoryId)
            ?: return AnswerResult.NotFound
        applyCategory(question, transaction, category, note)
        return AnswerResult.Categorized(category, note.orEmpty())
    }

    private suspend fun applyCategory(
        question: PendingQuestion,
        transaction: com.gastoclaro.domain.model.Transaction,
        category: Category,
        note: String?
    ) {
        transactionRepository.update(transaction.copy(categoryId = category.id, note = note))
        transaction.merchant?.let { merchant ->
            val normalized = TextNormalizer.normalize(merchant)
            if (normalized.isNotBlank()) {
                categoryRuleRepository.upsertRule(
                    CategoryRule(
                        pattern = normalized,
                        categoryId = category.id,
                        isLearned = true,
                        confidence = 80,
                        usageCount = 1
                    )
                )
            }
        }
        pendingQuestionRepository.dequeue(question)
    }
}
