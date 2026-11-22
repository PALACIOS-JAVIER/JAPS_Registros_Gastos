package com.gastoclaro.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastoclaro.core.util.TextNormalizer
import com.gastoclaro.domain.model.Category
import com.gastoclaro.domain.model.CategoryRule
import com.gastoclaro.domain.model.UnrecognizedMessage
import com.gastoclaro.domain.repository.CategoryRepository
import com.gastoclaro.domain.repository.CategoryRuleRepository
import com.gastoclaro.domain.repository.UnrecognizedMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnrecognizedMessagesViewModel @Inject constructor(
    private val unrecognizedMessageRepository: UnrecognizedMessageRepository,
    categoryRepository: CategoryRepository,
    private val categoryRuleRepository: CategoryRuleRepository
) : ViewModel() {

    val messages: StateFlow<List<UnrecognizedMessage>> = unrecognizedMessageRepository.observeUnrecognized()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(message: UnrecognizedMessage) {
        viewModelScope.launch { unrecognizedMessageRepository.delete(message) }
    }

    fun clearAll() {
        viewModelScope.launch { unrecognizedMessageRepository.deleteAll() }
    }

    fun createRule(pattern: String, categoryId: Long, sourceMessage: UnrecognizedMessage) {
        viewModelScope.launch {
            categoryRuleRepository.upsertRule(
                CategoryRule(
                    pattern = TextNormalizer.normalize(pattern),
                    categoryId = categoryId,
                    isLearned = false,
                    confidence = 100,
                    usageCount = 0
                )
            )
            unrecognizedMessageRepository.delete(sourceMessage)
        }
    }
}
