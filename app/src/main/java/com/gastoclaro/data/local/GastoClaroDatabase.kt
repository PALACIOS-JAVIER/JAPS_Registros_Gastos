package com.gastoclaro.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gastoclaro.data.local.dao.AccountDao
import com.gastoclaro.data.local.dao.AnalyticsDao
import com.gastoclaro.data.local.dao.BudgetDao
import com.gastoclaro.data.local.dao.CategoryDao
import com.gastoclaro.data.local.dao.CategoryRuleDao
import com.gastoclaro.data.local.dao.KnownCounterpartyDao
import com.gastoclaro.data.local.dao.PendingQuestionDao
import com.gastoclaro.data.local.dao.TransactionDao
import com.gastoclaro.data.local.dao.UnrecognizedMessageDao
import com.gastoclaro.data.local.entity.AccountEntity
import com.gastoclaro.data.local.entity.BudgetEntity
import com.gastoclaro.data.local.entity.CategoryEntity
import com.gastoclaro.data.local.entity.CategoryRuleEntity
import com.gastoclaro.data.local.entity.KnownCounterpartyEntity
import com.gastoclaro.data.local.entity.PendingQuestionEntity
import com.gastoclaro.data.local.entity.TransactionEntity
import com.gastoclaro.data.local.entity.UnrecognizedMessageEntity

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        CategoryRuleEntity::class,
        BudgetEntity::class,
        KnownCounterpartyEntity::class,
        UnrecognizedMessageEntity::class,
        PendingQuestionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GastoClaroDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun categoryRuleDao(): CategoryRuleDao
    abstract fun budgetDao(): BudgetDao
    abstract fun knownCounterpartyDao(): KnownCounterpartyDao
    abstract fun unrecognizedMessageDao(): UnrecognizedMessageDao
    abstract fun pendingQuestionDao(): PendingQuestionDao
    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        const val DATABASE_NAME = "gastoclaro.db"
    }
}
