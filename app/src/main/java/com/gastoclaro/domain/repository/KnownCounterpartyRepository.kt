package com.gastoclaro.domain.repository

import com.gastoclaro.domain.model.KnownCounterparty
import kotlinx.coroutines.flow.Flow

interface KnownCounterpartyRepository {
    fun observeCounterparties(): Flow<List<KnownCounterparty>>
    suspend fun findByNormalized(identifier: String): KnownCounterparty?
    suspend fun getAll(): List<KnownCounterparty>
    suspend fun upsert(counterparty: KnownCounterparty): Long
    suspend fun delete(counterparty: KnownCounterparty)
}
