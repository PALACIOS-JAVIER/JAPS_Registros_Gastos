package com.gastoclaro.data.repository

import com.gastoclaro.core.util.MonthKey
import com.gastoclaro.data.local.dao.TransactionDao
import com.gastoclaro.data.local.toDomain
import com.gastoclaro.data.local.toEntity
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.TransactionType
import com.gastoclaro.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun observeTransactions(): Flow<List<Transaction>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeTransaction(id: Long): Flow<Transaction?> =
        dao.observeById(id).map { it?.toDomain() }

    override fun observePendingReview(): Flow<List<Transaction>> =
        dao.observePendingReview().map { list -> list.map { it.toDomain() } }

    override suspend fun getTransaction(id: Long): Transaction? =
        dao.getById(id)?.toDomain()

    override suspend fun getByIds(ids: List<Long>): List<Transaction> =
        dao.getByIds(ids).map { it.toDomain() }

    override suspend fun insert(transaction: Transaction): Long {
        val withMonth = transaction.copy(month = MonthKey.of(transaction.dateTime))
        return dao.insert(withMonth.toEntity())
    }

    override suspend fun update(transaction: Transaction) {
        val withMonth = transaction.copy(month = MonthKey.of(transaction.dateTime))
        dao.insert(withMonth.toEntity())
    }

    override suspend fun delete(transaction: Transaction) =
        dao.deleteById(transaction.id)

    override suspend fun findMatchCandidates(
        type: TransactionType,
        minAmount: Long,
        maxAmount: Long,
        targetAmount: Long,
        from: Long,
        to: Long
    ): List<Transaction> =
        dao.findMatchCandidates(type, minAmount, maxAmount, targetAmount, from, to)
            .map { it.toDomain() }

    override suspend fun getByTransferGroup(groupId: String): List<Transaction> =
        dao.getByTransferGroup(groupId).map { it.toDomain() }

    override suspend fun existsByRawTextHash(hash: String): Boolean =
        dao.existsByRawTextHash(hash)

    override suspend fun getByRawTextHash(hash: String): Transaction? =
        dao.getByRawTextHash(hash)?.toDomain()

    override suspend fun search(query: String): List<Transaction> =
        dao.search(query).map { it.toDomain() }

    override suspend fun getTransactionsBetween(start: Long, end: Long): List<Transaction> =
        dao.getBetween(start, end).map { it.toDomain() }
}
