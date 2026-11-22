package com.gastoclaro.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastoclaro.core.util.MonthRange
import com.gastoclaro.core.util.TextNormalizer
import com.gastoclaro.domain.model.Category
import com.gastoclaro.domain.model.CategoryRule
import com.gastoclaro.domain.model.CategorySpending
import com.gastoclaro.domain.repository.AnalyticsRepository
import com.gastoclaro.domain.repository.CategoryRepository
import com.gastoclaro.domain.repository.CategoryRuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val categoryRuleRepository: CategoryRuleRepository,
    analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val monthRange = MonthRange.current()

    val categories: StateFlow<List<Category>> = categoryRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val spending: StateFlow<List<CategorySpending>> =
        analyticsRepository.observeCategorySpending(monthRange.first, monthRange.second)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rules: StateFlow<List<CategoryRule>> = categoryRuleRepository.observeRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun upsertCategory(category: Category) {
        viewModelScope.launch { categoryRepository.upsertCategory(category) }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch { categoryRepository.deleteCategory(category) }
    }

    fun createRule(merchant: String, categoryId: Long) {
        viewModelScope.launch {
            categoryRuleRepository.upsertRule(
                CategoryRule(
                    pattern = TextNormalizer.normalize(merchant),
                    categoryId = categoryId,
                    isLearned = true,
                    confidence = 80,
                    usageCount = 1
                )
            )
        }
    }

    fun deleteRule(rule: CategoryRule) {
        viewModelScope.launch { categoryRuleRepository.deleteRule(rule) }
    }
}
