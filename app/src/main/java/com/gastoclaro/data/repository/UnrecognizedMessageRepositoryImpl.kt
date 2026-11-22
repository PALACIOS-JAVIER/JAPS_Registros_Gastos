package com.gastoclaro.data.repository

import com.gastoclaro.data.local.dao.UnrecognizedMessageDao
import com.gastoclaro.data.local.toDomain
import com.gastoclaro.data.local.toEntity
import com.gastoclaro.domain.model.UnrecognizedMessage
import com.gastoclaro.domain.repository.UnrecognizedMessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnrecognizedMessageRepositoryImpl @Inject constructor(
    private val dao: UnrecognizedMessageDao
) : UnrecognizedMessageRepository {

    override fun observeUnrecognized(): Flow<List<UnrecognizedMessage>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(message: UnrecognizedMessage): Long =
        dao.insert(message.toEntity())

    override suspend fun delete(message: UnrecognizedMessage) =
        dao.deleteById(message.id)

    override suspend fun deleteAll() =
        dao.deleteAll()
}
