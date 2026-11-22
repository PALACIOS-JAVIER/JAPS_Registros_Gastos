package com.gastoclaro

import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.TransactionSource
import com.gastoclaro.domain.model.TransactionType
import com.gastoclaro.domain.repository.AccountRepository
import com.gastoclaro.domain.repository.PendingQuestionRepository
import com.gastoclaro.domain.repository.SettingsRepository
import com.gastoclaro.domain.usecase.EnqueueExpenseQuestionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class EnqueueExpenseQuestionUseCaseTest {

    private fun buildUseCase(
        settings: SettingsRepository,
        pending: PendingQuestionRepository = mockk(relaxed = true),
        accounts: AccountRepository = mockk(relaxed = true)
    ) = EnqueueExpenseQuestionUseCase(settings, pending, accounts)

    private fun baseSettings(): SettingsRepository = mockk {
        every { bubbleEnabled } returns flowOf(true)
        every { bubbleMinAmount } returns flowOf(0L)
        every { neverAskCounterparties } returns flowOf(emptySet())
        every { dndStart } returns flowOf("00:00")
        every { dndEnd } returns flowOf("00:00")
    }

    private fun expense() = Transaction(
        id = 1,
        accountId = 1,
        type = TransactionType.EXPENSE,
        amount = 5_000_000L,
        dateTime = System.currentTimeMillis(),
        merchant = "EXITO",
        source = TransactionSource.NOTIFICATION
    )

    @Test
    fun `does not ask for internal transfers`() = runTest {
        val pending = mockk<PendingQuestionRepository>(relaxed = true)
        val useCase = buildUseCase(baseSettings(), pending)
        assertFalse(useCase(expense().copy(isTransferInternal = true)))
        coVerify(exactly = 0) { pending.enqueue(any()) }
    }

    @Test
    fun `does not ask for income`() = runTest {
        val pending = mockk<PendingQuestionRepository>(relaxed = true)
        val useCase = buildUseCase(baseSettings(), pending)
        assertFalse(useCase(expense().copy(type = TransactionType.INCOME)))
        coVerify(exactly = 0) { pending.enqueue(any()) }
    }

    @Test
    fun `does not ask below minimum amount`() = runTest {
        val settings = mockk<SettingsRepository> {
            every { bubbleEnabled } returns flowOf(true)
            every { bubbleMinAmount } returns flowOf(10_000_000L)
            every { neverAskCounterparties } returns flowOf(emptySet())
            every { dndStart } returns flowOf("00:00")
            every { dndEnd } returns flowOf("00:00")
        }
        val pending = mockk<PendingQuestionRepository>(relaxed = true)
        val useCase = buildUseCase(settings, pending)
        assertFalse(useCase(expense().copy(amount = 5_000_000L)))
        coVerify(exactly = 0) { pending.enqueue(any()) }
    }

    @Test
    fun `does not ask for never-ask counterparties`() = runTest {
        val settings = mockk<SettingsRepository> {
            every { bubbleEnabled } returns flowOf(true)
            every { bubbleMinAmount } returns flowOf(0L)
            every { neverAskCounterparties } returns flowOf(setOf("exito"))
            every { dndStart } returns flowOf("00:00")
            every { dndEnd } returns flowOf("00:00")
        }
        val pending = mockk<PendingQuestionRepository>(relaxed = true)
        val useCase = buildUseCase(settings, pending)
        assertFalse(useCase(expense().copy(merchant = "EXITO")))
        coVerify(exactly = 0) { pending.enqueue(any()) }
    }

    @Test
    fun `does not ask when already categorized`() = runTest {
        val pending = mockk<PendingQuestionRepository>(relaxed = true)
        val useCase = buildUseCase(baseSettings(), pending)
        assertFalse(useCase(expense().copy(categoryId = 1L)))
        coVerify(exactly = 0) { pending.enqueue(any()) }
    }

    @Test
    fun `enqueues a question for a normal external expense`() = runTest {
        val pending = mockk<PendingQuestionRepository>(relaxed = true)
        coEvery { pending.enqueue(any()) } returns 1L
        val useCase = buildUseCase(baseSettings(), pending)
        assertTrue(useCase(expense()))
        coVerify(exactly = 1) { pending.enqueue(any()) }
    }

    @Test
    fun `dnd window detection`() {
        assertTrue(EnqueueExpenseQuestionUseCase.isInDndWindow("22:00", "07:00", LocalTime.of(23, 0)))
        assertTrue(EnqueueExpenseQuestionUseCase.isInDndWindow("22:00", "07:00", LocalTime.of(1, 0)))
        assertFalse(EnqueueExpenseQuestionUseCase.isInDndWindow("22:00", "07:00", LocalTime.of(12, 0)))
        assertTrue(EnqueueExpenseQuestionUseCase.isInDndWindow("08:00", "18:00", LocalTime.of(12, 0)))
        assertFalse(EnqueueExpenseQuestionUseCase.isInDndWindow("08:00", "18:00", LocalTime.of(20, 0)))
    }
}
