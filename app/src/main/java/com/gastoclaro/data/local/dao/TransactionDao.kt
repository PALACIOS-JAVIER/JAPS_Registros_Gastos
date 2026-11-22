package com.gastoclaro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gastoclaro.data.local.entity.TransactionEntity
import com.gastoclaro.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY dateTime DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY dateTime DESC")
    suspend fun getAll(): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun clearAll()

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun observeById(id: Long): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE status = 'PENDING_REVIEW' ORDER BY dateTime DESC")
    fun observePendingReview(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE rawTextHash = :hash LIMIT 1")
    suspend fun getByRawTextHash(hash: String): TransactionEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM transactions WHERE rawTextHash = :hash)")
    suspend fun existsByRawTextHash(hash: String): Boolean

    @Query("SELECT * FROM transactions WHERE transferGroupId = :groupId ORDER BY dateTime ASC")
    suspend fun getByTransferGroup(groupId: String): List<TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions
        WHERE type = :type
          AND amount BETWEEN :minAmount AND :maxAmount
          AND dateTime BETWEEN :from AND :to
          AND isTransferInternal = 0
          AND transferGroupId IS NULL
        ORDER BY ABS(amount - :targetAmount) ASC, dateTime ASC
        LIMIT 30
        """
    )
    suspend fun findMatchCandidates(
        type: TransactionType,
        minAmount: Long,
        maxAmount: Long,
        targetAmount: Long,
        from: Long,
        to: Long
    ): List<TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions
        WHERE (merchant LIKE '%' || :query || '%'
            OR note LIKE '%' || :query || '%'
            OR reference LIKE '%' || :query || '%')
        ORDER BY dateTime DESC
        LIMIT 200
        """
    )
    suspend fun search(query: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE dateTime >= :start AND dateTime < :end ORDER BY dateTime DESC")
    suspend fun getBetween(start: Long, end: Long): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
