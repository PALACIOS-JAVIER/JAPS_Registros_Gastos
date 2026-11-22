package com.gastoclaro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gastoclaro.data.local.entity.PendingQuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingQuestionDao {

    @Query("SELECT * FROM pending_questions ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<PendingQuestionEntity>>

    @Query("SELECT * FROM pending_questions ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingQuestionEntity>

    @Insert
    suspend fun insert(question: PendingQuestionEntity): Long

    @Query("DELETE FROM pending_questions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM pending_questions")
    suspend fun deleteAll()
}
