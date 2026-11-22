package com.gastoclaro.domain.repository

import com.gastoclaro.domain.model.CategoryRule
import kotlinx.coroutines.flow.Flow

interface CategoryRuleRepository {
    fun observeRules(): Flow<List<CategoryRule>>
    suspend fun getAllRules(): List<CategoryRule>
    suspend fun findMatchingRule(merchant: String): CategoryRule?
    suspend fun upsertRule(rule: CategoryRule): Long
    suspend fun deleteRule(rule: CategoryRule)
    suspend fun incrementUsage(ruleId: Long)
}
