package com.gastoclaro.data.repository

import com.gastoclaro.core.util.TextNormalizer
import com.gastoclaro.data.local.dao.CategoryRuleDao
import com.gastoclaro.data.local.toDomain
import com.gastoclaro.data.local.toEntity
import com.gastoclaro.domain.model.CategoryRule
import com.gastoclaro.domain.repository.CategoryRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRuleRepositoryImpl @Inject constructor(
    private val dao: CategoryRuleDao
) : CategoryRuleRepository {

    override fun observeRules(): Flow<List<CategoryRule>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getAllRules(): List<CategoryRule> =
        dao.getAll().map { it.toDomain() }

    override suspend fun findMatchingRule(merchant: String): CategoryRule? {
        val normalized = TextNormalizer.normalizeCompact(merchant)
        if (normalized.isEmpty()) return null
        return dao.getAll()
            .map { it.toDomain() }
            .filter { rule -> normalized.contains(TextNormalizer.normalizeCompact(rule.pattern)) }
            .maxWithOrNull(compareBy({ it.usageCount }, { it.confidence }, { it.pattern.length }))
    }

    override suspend fun upsertRule(rule: CategoryRule): Long =
        dao.upsert(rule.copy(pattern = TextNormalizer.normalize(rule.pattern)).toEntity())

    override suspend fun deleteRule(rule: CategoryRule) =
        dao.deleteById(rule.id)

    override suspend fun incrementUsage(ruleId: Long) =
        dao.incrementUsage(ruleId)
}
