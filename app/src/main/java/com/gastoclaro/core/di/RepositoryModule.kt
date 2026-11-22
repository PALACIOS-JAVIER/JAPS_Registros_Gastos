package com.gastoclaro.core.di

import com.gastoclaro.data.repository.AccountRepositoryImpl
import com.gastoclaro.data.repository.AnalyticsRepositoryImpl
import com.gastoclaro.data.repository.BudgetRepositoryImpl
import com.gastoclaro.data.repository.CategoryRepositoryImpl
import com.gastoclaro.data.repository.CategoryRuleRepositoryImpl
import com.gastoclaro.data.repository.KnownCounterpartyRepositoryImpl
import com.gastoclaro.data.repository.PendingQuestionRepositoryImpl
import com.gastoclaro.data.repository.SettingsRepositoryImpl
import com.gastoclaro.data.repository.TransactionRepositoryImpl
import com.gastoclaro.data.repository.UnrecognizedMessageRepositoryImpl
import com.gastoclaro.domain.repository.AccountRepository
import com.gastoclaro.domain.repository.AnalyticsRepository
import com.gastoclaro.domain.repository.BudgetRepository
import com.gastoclaro.domain.repository.CategoryRepository
import com.gastoclaro.domain.repository.CategoryRuleRepository
import com.gastoclaro.domain.repository.KnownCounterpartyRepository
import com.gastoclaro.domain.repository.PendingQuestionRepository
import com.gastoclaro.domain.repository.SettingsRepository
import com.gastoclaro.domain.repository.TransactionRepository
import com.gastoclaro.domain.repository.UnrecognizedMessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRuleRepository(impl: CategoryRuleRepositoryImpl): CategoryRuleRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindKnownCounterpartyRepository(impl: KnownCounterpartyRepositoryImpl): KnownCounterpartyRepository

    @Binds
    @Singleton
    abstract fun bindUnrecognizedMessageRepository(impl: UnrecognizedMessageRepositoryImpl): UnrecognizedMessageRepository

    @Binds
    @Singleton
    abstract fun bindPendingQuestionRepository(impl: PendingQuestionRepositoryImpl): PendingQuestionRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository
}
