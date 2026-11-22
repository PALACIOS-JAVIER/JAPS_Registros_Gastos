package com.gastoclaro.data.repository

import com.gastoclaro.data.local.dao.AnalyticsDao
import com.gastoclaro.domain.model.AccountBalance
import com.gastoclaro.domain.model.CategorySpending
import com.gastoclaro.domain.model.MonthlySummary
import com.gastoclaro.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val dao: AnalyticsDao
) : AnalyticsRepository {

    override fun observeCategorySpending(start: Long, end: Long): Flow<List<CategorySpending>> =
        dao.observeCategorySpending(start, end)

    override fun observeMonthlyTrend(months: Int): Flow<List<MonthlySummary>> =
        dao.observeMonthlyTrend(months)

    override fun observeAccountBalances(): Flow<List<AccountBalance>> =
        dao.observeAccountBalances()

    override fun observeTotalBalance(): Flow<Long> =
        dao.observeAccountBalances().map { list -> list.sumOf { it.balance } }

    override suspend fun getMonthExpense(start: Long, end: Long): Long =
        dao.getMonthExpense(start, end)

    override suspend fun getMonthIncome(start: Long, end: Long): Long =
        dao.getMonthIncome(start, end)

    override suspend fun getCategorySpendingFor(start: Long, end: Long): List<CategorySpending> =
        dao.getCategorySpending(start, end)

    override suspend fun getMonthlyTrendFor(months: Int): List<MonthlySummary> =
        dao.getMonthlyTrend(months)

    override suspend fun getSpentForCategory(categoryId: Long, start: Long, end: Long): Long =
        dao.getSpentForCategory(categoryId, start, end)
}
