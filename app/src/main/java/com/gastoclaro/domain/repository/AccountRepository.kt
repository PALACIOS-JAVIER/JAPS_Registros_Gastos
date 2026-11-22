package com.gastoclaro.domain.repository

import com.gastoclaro.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>
    fun observeAccount(accountId: Long): Flow<Account?>
    suspend fun getAccount(accountId: Long): Account?
    suspend fun getAllAccounts(): List<Account>
    suspend fun upsertAccount(account: Account): Long
    suspend fun deleteAccount(account: Account)
}
