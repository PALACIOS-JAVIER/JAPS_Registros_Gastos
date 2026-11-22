package com.gastoclaro.domain.repository

import com.gastoclaro.domain.model.PendingQuestion
import kotlinx.coroutines.flow.Flow

interface PendingQuestionRepository {
    fun observeQuestions(): Flow<List<PendingQuestion>>
    suspend fun enqueue(question: PendingQuestion): Long
    suspend fun dequeue(question: PendingQuestion)
    suspend fun deleteAll()
}
