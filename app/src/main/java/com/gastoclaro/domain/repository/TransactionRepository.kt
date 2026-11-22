package com.gastoclaro.domain.repository

import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    fun observeTransaction(id: Long): Flow<Transaction?>
    fun observePendingReview(): Flow<List<Transaction>>
    suspend fun getTransaction(id: Long): Transaction?
    suspend fun getByIds(ids: List<Long>): List<Transaction>
    suspend fun insert(transaction: Transaction): Long
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    suspend fun findMatchCandidates(
        type: TransactionType,
        minAmount: Long,
        maxAmount: Long,
        targetAmount: Long,
        from: Long,
        to: Long
    ): List<Transaction>
    suspend fun getByTransferGroup(groupId: String): List<Transaction>
    suspend fun existsByRawTextHash(hash: String): Boolean
    suspend fun getByRawTextHash(hash: String): Transaction?
    suspend fun search(query: String): List<Transaction>
    suspend fun getTransactionsBetween(start: Long, end: Long): List<Transaction>
}
