package com.gastoclaro.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastoclaro.core.util.MonthRange
import com.gastoclaro.domain.model.Budget
import com.gastoclaro.domain.model.Category
import com.gastoclaro.domain.model.CategorySpending
import com.gastoclaro.domain.model.CategoryType
import com.gastoclaro.domain.repository.AnalyticsRepository
import com.gastoclaro.domain.repository.BudgetRepository
import com.gastoclaro.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val monthRange = MonthRange.current()

    val budgets: StateFlow<List<Budget>> = budgetRepository.observeBudgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepository.observeCategories(CategoryType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val spending: StateFlow<List<CategorySpending>> =
        analyticsRepository.observeCategorySpending(monthRange.first, monthRange.second)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun upsertBudget(budget: Budget) {
        viewModelScope.launch { budgetRepository.upsertBudget(budget) }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch { budgetRepository.deleteBudget(budget) }
    }
}
