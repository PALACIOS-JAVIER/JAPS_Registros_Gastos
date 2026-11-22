package com.gastoclaro.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.gastoclaro.domain.model.AccountBalance
import com.gastoclaro.domain.model.CategorySpending
import com.gastoclaro.domain.model.MonthlySummary
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {

    @Query(
        """
        SELECT c.id AS categoryId, c.name AS name, c.icon AS icon, c.color AS color,
               SUM(t.amount) AS total
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.isTransferInternal = 0
          AND t.type IN ('EXPENSE','WITHDRAWAL','TRANSFER_OUT')
          AND t.dateTime >= :start AND t.dateTime < :end
        GROUP BY c.id
        ORDER BY total DESC
        """
    )
    fun observeCategorySpending(start: Long, end: Long): Flow<List<CategorySpending>>

    @Query(
        """
        SELECT c.id AS categoryId, c.name AS name, c.icon AS icon, c.color AS color,
               SUM(t.amount) AS total
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.isTransferInternal = 0
          AND t.type IN ('EXPENSE','WITHDRAWAL','TRANSFER_OUT')
          AND t.dateTime >= :start AND t.dateTime < :end
        GROUP BY c.id
        ORDER BY total DESC
        """
    )
    suspend fun getCategorySpending(start: Long, end: Long): List<CategorySpending>

    @Query(
        """
        SELECT t.month AS month,
               COALESCE(SUM(CASE WHEN t.isTransferInternal = 0 AND t.type IN ('INCOME','TRANSFER_IN') THEN t.amount ELSE 0 END), 0) AS income,
               COALESCE(SUM(CASE WHEN t.isTransferInternal = 0 AND t.type IN ('EXPENSE','WITHDRAWAL','TRANSFER_OUT') THEN t.amount ELSE 0 END), 0) AS expense
        FROM transactions t
        GROUP BY t.month
        ORDER BY t.month DESC
        LIMIT :limit
        """
    )
    fun observeMonthlyTrend(limit: Int): Flow<List<MonthlySummary>>

    @Query(
        """
        SELECT t.month AS month,
               COALESCE(SUM(CASE WHEN t.isTransferInternal = 0 AND t.type IN ('INCOME','TRANSFER_IN') THEN t.amount ELSE 0 END), 0) AS income,
               COALESCE(SUM(CASE WHEN t.isTransferInternal = 0 AND t.type IN ('EXPENSE','WITHDRAWAL','TRANSFER_OUT') THEN t.amount ELSE 0 END), 0) AS expense
        FROM transactions t
        GROUP BY t.month
        ORDER BY t.month DESC
        LIMIT :limit
        """
    )
    suspend fun getMonthlyTrend(limit: Int): List<MonthlySummary>

    @Query(
        """
        SELECT a.id AS accountId,
               a.initialBalance + COALESCE(SUM(CASE WHEN t.type IN ('INCOME','TRANSFER_IN') THEN t.amount ELSE -t.amount END), 0) AS balance
        FROM accounts a
        LEFT JOIN transactions t ON t.accountId = a.id
        WHERE a.isArchived = 0
        GROUP BY a.id
        """
    )
    fun observeAccountBalances(): Flow<List<AccountBalance>>

    @Query(
        """
        SELECT COALESCE(SUM(t.amount), 0) FROM transactions t
        WHERE t.isTransferInternal = 0
          AND t.type IN ('EXPENSE','WITHDRAWAL','TRANSFER_OUT')
          AND t.dateTime >= :start AND t.dateTime < :end
        """
    )
    suspend fun getMonthExpense(start: Long, end: Long): Long

    @Query(
        """
        SELECT COALESCE(SUM(t.amount), 0) FROM transactions t
        WHERE t.isTransferInternal = 0
          AND t.type IN ('INCOME','TRANSFER_IN')
          AND t.dateTime >= :start AND t.dateTime < :end
        """
    )
    suspend fun getMonthIncome(start: Long, end: Long): Long

    @Query(
        """
        SELECT COALESCE(SUM(t.amount), 0) FROM transactions t
        WHERE t.categoryId = :categoryId
          AND t.isTransferInternal = 0
          AND t.type IN ('EXPENSE','WITHDRAWAL','TRANSFER_OUT')
          AND t.dateTime >= :start AND t.dateTime < :end
        """
    )
    suspend fun getSpentForCategory(categoryId: Long, start: Long, end: Long): Long
}
