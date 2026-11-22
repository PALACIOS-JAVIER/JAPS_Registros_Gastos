package com.gastoclaro.data.repository

import com.gastoclaro.data.local.dao.AccountDao
import com.gastoclaro.data.local.toDomain
import com.gastoclaro.data.local.toEntity
import com.gastoclaro.domain.model.Account
import com.gastoclaro.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val dao: AccountDao
) : AccountRepository {

    override fun observeAccounts(): Flow<List<Account>> =
        dao.observeActive().map { list -> list.map { it.toDomain() } }

    override fun observeAccount(accountId: Long): Flow<Account?> =
        dao.observeById(accountId).map { it?.toDomain() }

    override suspend fun getAccount(accountId: Long): Account? =
        dao.getById(accountId)?.toDomain()

    override suspend fun getAllAccounts(): List<Account> =
        dao.getAllActive().map { it.toDomain() }

    override suspend fun upsertAccount(account: Account): Long =
        dao.upsert(account.toEntity())

    override suspend fun deleteAccount(account: Account) =
        dao.deleteById(account.id)
}
