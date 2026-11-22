package com.gastoclaro.data.repository

import com.gastoclaro.data.local.dao.BudgetDao
import com.gastoclaro.data.local.toDomain
import com.gastoclaro.data.local.toEntity
import com.gastoclaro.domain.model.Budget
import com.gastoclaro.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao
) : BudgetRepository {

    override fun observeBudgets(): Flow<List<Budget>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getBudget(categoryId: Long): Budget? =
        dao.getByCategory(categoryId)?.toDomain()

    override suspend fun upsertBudget(budget: Budget): Long =
        dao.upsert(budget.toEntity())

    override suspend fun deleteBudget(budget: Budget) =
        dao.deleteById(budget.id)
}
