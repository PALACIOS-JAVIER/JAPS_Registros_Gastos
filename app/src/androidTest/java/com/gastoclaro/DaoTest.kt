package com.gastoclaro

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gastoclaro.data.local.GastoClaroDatabase
import com.gastoclaro.data.local.entity.AccountEntity
import com.gastoclaro.data.local.entity.CategoryEntity
import com.gastoclaro.data.local.entity.TransactionEntity
import com.gastoclaro.domain.model.AccountType
import com.gastoclaro.domain.model.CategoryType
import com.gastoclaro.domain.model.TransactionSource
import com.gastoclaro.domain.model.TransactionStatus
import com.gastoclaro.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DaoTest {

    private lateinit var db: GastoClaroDatabase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            GastoClaroDatabase::class.java
        ).build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun aggregateQueriesWork() = runBlocking {
        val accountId = db.accountDao().upsert(
            AccountEntity(name = "Nequi", bank = "Nequi", type = AccountType.WALLET, initialBalance = 100000L)
        )
        val categoryId = db.categoryDao().insert(
            CategoryEntity(name = "Comida", icon = "restaurant", color = 0xFF0000.toInt(), type = CategoryType.EXPENSE)
        )

        val start = System.currentTimeMillis()
        db.transactionDao().insert(
            TransactionEntity(
                accountId = accountId,
                type = TransactionType.EXPENSE,
                amount = 2500000L,
                dateTime = start,
                categoryId = categoryId,
                source = TransactionSource.NOTIFICATION,
                month = "2024-11"
            )
        )
        db.transactionDao().insert(
            TransactionEntity(
                accountId = accountId,
                type = TransactionType.INCOME,
                amount = 5000000L,
                dateTime = start,
                source = TransactionSource.NOTIFICATION,
                month = "2024-11"
            )
        )
        db.transactionDao().insert(
            TransactionEntity(
                accountId = accountId,
                type = TransactionType.TRANSFER_IN,
                amount = 100000L,
                dateTime = start,
                source = TransactionSource.NOTIFICATION,
                isTransferInternal = true,
                month = "2024-11"
            )
        )

        val spending = db.analyticsDao().getCategorySpending(0L, System.currentTimeMillis() + 1000L)
        assertEquals(1, spending.size)
        assertEquals(2500000L, spending.first().total)

        val expense = db.analyticsDao().getMonthExpense(0L, System.currentTimeMillis() + 1000L)
        assertEquals(2500000L, expense)

        val income = db.analyticsDao().getMonthIncome(0L, System.currentTimeMillis() + 1000L)
        assertEquals(5000000L, income)

        val balances = db.analyticsDao().observeAccountBalances().first()
        val balance = balances.firstOrNull { it.accountId == accountId }
        // 100000 (inicial) - 2500000 + 5000000 + 100000 = 2700000
        assertEquals(2700000L, balance?.balance)
    }
}
