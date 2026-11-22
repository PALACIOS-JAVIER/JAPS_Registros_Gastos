package com.gastoclaro

import com.gastoclaro.core.util.DefaultCategories
import com.gastoclaro.domain.bubble.AnswerResult
import com.gastoclaro.domain.bubble.ResponseInterpreter
import com.gastoclaro.domain.model.PendingQuestion
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.TransactionSource
import com.gastoclaro.domain.model.TransactionType
import com.gastoclaro.domain.repository.CategoryRepository
import com.gastoclaro.domain.repository.CategoryRuleRepository
import com.gastoclaro.domain.repository.PendingQuestionRepository
import com.gastoclaro.domain.repository.TransactionRepository
import com.gastoclaro.domain.usecase.AnswerQuestionUseCase
import com.gastoclaro.domain.usecase.MarkTransactionInternalUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerQuestionUseCaseTest {

    private fun tx() = Transaction(
        id = 1,
        accountId = 1,
        type = TransactionType.EXPENSE,
        amount = 5_000_000L,
        dateTime = System.currentTimeMillis(),
        merchant = "EXITO",
        source = TransactionSource.NOTIFICATION
    )

    private fun build(
        txRepo: TransactionRepository,
        catRepo: CategoryRepository,
        ruleRepo: CategoryRuleRepository,
        pendingRepo: PendingQuestionRepository
    ) = AnswerQuestionUseCase(
        transactionRepository = txRepo,
        categoryRepository = catRepo,
        categoryRuleRepository = ruleRepo,
        pendingQuestionRepository = pendingRepo,
        responseInterpreter = ResponseInterpreter(),
        markTransactionInternal = mockk(relaxed = true)
    )

    @Test
    fun `answer maps text to category and learns a rule`() = runTest {
        val txRepo = mockk<TransactionRepository>()
        val catRepo = mockk<CategoryRepository>()
        val ruleRepo = mockk<CategoryRuleRepository>()
        val pendingRepo = mockk<PendingQuestionRepository>()

        coEvery { txRepo.getTransaction(1) } returns tx()
        coEvery { catRepo.getAllCategories() } returns DefaultCategories.all()
        coEvery { txRepo.update(any()) } returns Unit
        coEvery { ruleRepo.upsertRule(any()) } returns 1L
        coEvery { pendingRepo.dequeue(any()) } returns Unit

        val question = PendingQuestion(id = 1, transactionId = 1, createdAt = 0L, questionText = "q")
        val result = build(txRepo, catRepo, ruleRepo, pendingRepo).answer(question, "almuerzo")

        assertTrue(result is AnswerResult.Categorized)
        assertEquals("Comida", (result as AnswerResult.Categorized).category.name)
        coVerify { txRepo.update(match { it.categoryId != null && it.note == "almuerzo" }) }
        coVerify { ruleRepo.upsertRule(match { it.pattern.contains("exito") }) }
        coVerify { pendingRepo.dequeue(question) }
    }

    @Test
    fun `answer returns NeedCategory when text is unknown`() = runTest {
        val txRepo = mockk<TransactionRepository>()
        val catRepo = mockk<CategoryRepository>()
        val ruleRepo = mockk<CategoryRuleRepository>()
        val pendingRepo = mockk<PendingQuestionRepository>()

        coEvery { txRepo.getTransaction(1) } returns tx()
        coEvery { catRepo.getAllCategories() } returns DefaultCategories.all()

        val question = PendingQuestion(id = 1, transactionId = 1, createdAt = 0L, questionText = "q")
        val result = build(txRepo, catRepo, ruleRepo, pendingRepo).answer(question, "xyz123")

        assertTrue(result is AnswerResult.NeedCategory)
        coVerify(exactly = 0) { pendingRepo.dequeue(any()) }
    }

    @Test
    fun `markInternal marks transaction as internal and dequeues`() = runTest {
        val txRepo = mockk<TransactionRepository>()
        val catRepo = mockk<CategoryRepository>()
        val ruleRepo = mockk<CategoryRuleRepository>()
        val pendingRepo = mockk<PendingQuestionRepository>()
        val markInternal = mockk<MarkTransactionInternalUseCase>()

        coEvery { txRepo.getTransaction(1) } returns tx()
        coEvery { markInternal.invoke(1, true) } returns Unit
        coEvery { pendingRepo.dequeue(any()) } returns Unit

        val useCase = AnswerQuestionUseCase(
            transactionRepository = txRepo,
            categoryRepository = catRepo,
            categoryRuleRepository = ruleRepo,
            pendingQuestionRepository = pendingRepo,
            responseInterpreter = ResponseInterpreter(),
            markTransactionInternal = markInternal
        )
        val question = PendingQuestion(id = 1, transactionId = 1, createdAt = 0L, questionText = "q")
        val result = useCase.markInternal(question)

        assertTrue(result is AnswerResult.MarkedInternal)
        coVerify { markInternal.invoke(1, true) }
        coVerify { pendingRepo.dequeue(question) }
    }
}
