package com.gastoclaro.domain.repository

import com.gastoclaro.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeBudgets(): Flow<List<Budget>>
    suspend fun getBudget(categoryId: Long): Budget?
    suspend fun upsertBudget(budget: Budget): Long
    suspend fun deleteBudget(budget: Budget)
}
