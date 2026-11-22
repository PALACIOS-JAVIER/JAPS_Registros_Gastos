package com.gastoclaro.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastoclaro.core.util.DateFormats
import com.gastoclaro.core.util.MonthRange
import com.gastoclaro.domain.model.Account
import com.gastoclaro.domain.model.Category
import com.gastoclaro.domain.model.MonthlySummary
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.repository.AccountRepository
import com.gastoclaro.domain.repository.AnalyticsRepository
import com.gastoclaro.domain.repository.CategoryRepository
import com.gastoclaro.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    analyticsRepository: AnalyticsRepository,
    private val transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    val yearlyTrend: StateFlow<List<MonthlySummary>> = analyticsRepository.observeMonthlyTrend(12)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<Account>> = accountRepository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentMonthLabel: String = DateFormats.monthLabel(DateFormats.currentMonthKey())

    suspend fun transactionsForCsv(): List<Transaction> {
        val yearStart = MonthRange.monthKeys(12).first()
        val (start, _) = MonthRange.forMonthKey(yearStart)
        return transactionRepository.getTransactionsBetween(start, System.currentTimeMillis())
    }
}
