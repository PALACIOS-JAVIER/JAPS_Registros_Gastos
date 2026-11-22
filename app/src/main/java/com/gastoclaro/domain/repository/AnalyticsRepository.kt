package com.gastoclaro.domain.repository

import com.gastoclaro.domain.model.AccountBalance
import com.gastoclaro.domain.model.CategorySpending
import com.gastoclaro.domain.model.MonthlySummary
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun observeCategorySpending(start: Long, end: Long): Flow<List<CategorySpending>>
    fun observeMonthlyTrend(months: Int): Flow<List<MonthlySummary>>
    fun observeAccountBalances(): Flow<List<AccountBalance>>
    fun observeTotalBalance(): Flow<Long>
    suspend fun getMonthExpense(start: Long, end: Long): Long
    suspend fun getMonthIncome(start: Long, end: Long): Long
    suspend fun getCategorySpendingFor(start: Long, end: Long): List<CategorySpending>
    suspend fun getMonthlyTrendFor(months: Int): List<MonthlySummary>
    suspend fun getSpentForCategory(categoryId: Long, start: Long, end: Long): Long
}
