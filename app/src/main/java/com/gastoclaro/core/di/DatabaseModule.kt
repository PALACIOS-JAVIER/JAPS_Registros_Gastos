package com.gastoclaro.core.di

import android.content.Context
import androidx.room.Room
import com.gastoclaro.core.security.CryptoManager
import com.gastoclaro.data.local.GastoClaroDatabase
import com.gastoclaro.data.local.dao.AccountDao
import com.gastoclaro.data.local.dao.AnalyticsDao
import com.gastoclaro.data.local.dao.BudgetDao
import com.gastoclaro.data.local.dao.CategoryDao
import com.gastoclaro.data.local.dao.CategoryRuleDao
import com.gastoclaro.data.local.dao.KnownCounterpartyDao
import com.gastoclaro.data.local.dao.PendingQuestionDao
import com.gastoclaro.data.local.dao.TransactionDao
import com.gastoclaro.data.local.dao.UnrecognizedMessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        cryptoManager: CryptoManager
    ): GastoClaroDatabase {
        System.loadLibrary("sqlcipher")
        val factory = SupportFactory(cryptoManager.getOrCreateDatabaseKey())
        return Room.databaseBuilder(
            context,
            GastoClaroDatabase::class.java,
            GastoClaroDatabase.DATABASE_NAME
        )
            .openHelperFactory(factory)
            .build()
    }

    @Provides
    fun provideAccountDao(db: GastoClaroDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideTransactionDao(db: GastoClaroDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideCategoryDao(db: GastoClaroDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideCategoryRuleDao(db: GastoClaroDatabase): CategoryRuleDao = db.categoryRuleDao()

    @Provides
    fun provideBudgetDao(db: GastoClaroDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideKnownCounterpartyDao(db: GastoClaroDatabase): KnownCounterpartyDao = db.knownCounterpartyDao()

    @Provides
    fun provideUnrecognizedMessageDao(db: GastoClaroDatabase): UnrecognizedMessageDao = db.unrecognizedMessageDao()

    @Provides
    fun providePendingQuestionDao(db: GastoClaroDatabase): PendingQuestionDao = db.pendingQuestionDao()

    @Provides
    fun provideAnalyticsDao(db: GastoClaroDatabase): AnalyticsDao = db.analyticsDao()
}
