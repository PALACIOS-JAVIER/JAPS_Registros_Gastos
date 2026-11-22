package com.gastoclaro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gastoclaro.data.local.entity.UnrecognizedMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnrecognizedMessageDao {

    @Query("SELECT * FROM unrecognized_messages ORDER BY receivedAt DESC")
    fun observeAll(): Flow<List<UnrecognizedMessageEntity>>

    @Insert
    suspend fun insert(message: UnrecognizedMessageEntity): Long

    @Query("DELETE FROM unrecognized_messages WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM unrecognized_messages")
    suspend fun deleteAll()
}
