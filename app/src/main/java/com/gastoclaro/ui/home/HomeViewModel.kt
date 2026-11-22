package com.gastoclaro.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastoclaro.core.util.DateFormats
import com.gastoclaro.core.util.MonthRange
import com.gastoclaro.domain.model.CategorySpending
import com.gastoclaro.domain.model.MonthlySummary
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.TransactionStatus
import com.gastoclaro.domain.model.TransactionType
import com.gastoclaro.domain.repository.AnalyticsRepository
import com.gastoclaro.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    transactionRepository: TransactionRepository
) : ViewModel() {

    data class State(
        val totalBalance: Long = 0,
        val monthExpense: Long = 0,
        val monthIncome: Long = 0,
        val categorySpending: List<CategorySpending> = emptyList(),
        val trend: List<MonthlySummary> = emptyList(),
        val recent: List<Transaction> = emptyList(),
        val pendingReview: Int = 0
    )

    private val monthRange = MonthRange.current()
    private val monthKey = DateFormats.currentMonthKey()

    val state: StateFlow<State> = combine(
        analyticsRepository.observeTotalBalance(),
        analyticsRepository.observeCategorySpending(monthRange.first, monthRange.second),
        analyticsRepository.observeMonthlyTrend(6),
        transactionRepository.observeTransactions()
    ) { balance, categories, trend, transactions ->
        val monthTx = transactions.filter { it.month == monthKey }
        val expense = monthTx
            .filter { !it.isTransferInternal && it.type in EXPENSE_TYPES }
            .sumOf { it.amount }
        val income = monthTx
            .filter { !it.isTransferInternal && it.type in INCOME_TYPES }
            .sumOf { it.amount }
        State(
            totalBalance = balance,
            monthExpense = expense,
            monthIncome = income,
            categorySpending = categories,
            trend = fillTrend(trend),
            recent = transactions.take(8),
            pendingReview = transactions.count { it.status == TransactionStatus.PENDING_REVIEW }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), State())

    private fun fillTrend(trend: List<MonthlySummary>): List<MonthlySummary> {
        val byMonth = trend.associateBy { it.month }
        return MonthRange.monthKeys(6).map { key ->
            byMonth[key] ?: MonthlySummary(month = key, income = 0, expense = 0)
        }
    }

    companion object {
        val EXPENSE_TYPES = setOf(
            TransactionType.EXPENSE, TransactionType.WITHDRAWAL, TransactionType.TRANSFER_OUT
        )
        val INCOME_TYPES = setOf(
            TransactionType.INCOME, TransactionType.TRANSFER_IN
        )
    }
}
