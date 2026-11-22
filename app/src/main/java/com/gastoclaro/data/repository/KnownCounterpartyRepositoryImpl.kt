package com.gastoclaro.data.repository

import com.gastoclaro.data.local.dao.KnownCounterpartyDao
import com.gastoclaro.data.local.toDomain
import com.gastoclaro.data.local.toEntity
import com.gastoclaro.domain.model.KnownCounterparty
import com.gastoclaro.domain.repository.KnownCounterpartyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KnownCounterpartyRepositoryImpl @Inject constructor(
    private val dao: KnownCounterpartyDao
) : KnownCounterpartyRepository {

    override fun observeCounterparties(): Flow<List<KnownCounterparty>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun findByNormalized(identifier: String): KnownCounterparty? =
        dao.findByIdentifier(identifier)?.toDomain()

    override suspend fun getAll(): List<KnownCounterparty> =
        dao.getAll().map { it.toDomain() }

    override suspend fun upsert(counterparty: KnownCounterparty): Long =
        dao.upsert(counterparty.toEntity())

    override suspend fun delete(counterparty: KnownCounterparty) =
        dao.deleteById(counterparty.id)
}
