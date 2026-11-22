package com.gastoclaro.domain.repository

import com.gastoclaro.domain.model.UnrecognizedMessage
import kotlinx.coroutines.flow.Flow

interface UnrecognizedMessageRepository {
    fun observeUnrecognized(): Flow<List<UnrecognizedMessage>>
    suspend fun insert(message: UnrecognizedMessage): Long
    suspend fun delete(message: UnrecognizedMessage)
    suspend fun deleteAll()
}
