package com.gastoclaro.data.repository

import com.gastoclaro.data.local.dao.PendingQuestionDao
import com.gastoclaro.data.local.toDomain
import com.gastoclaro.data.local.toEntity
import com.gastoclaro.domain.model.PendingQuestion
import com.gastoclaro.domain.repository.PendingQuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingQuestionRepositoryImpl @Inject constructor(
    private val dao: PendingQuestionDao
) : PendingQuestionRepository {

    override fun observeQuestions(): Flow<List<PendingQuestion>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun enqueue(question: PendingQuestion): Long =
        dao.insert(question.toEntity())

    override suspend fun dequeue(question: PendingQuestion) =
        dao.deleteById(question.id)

    override suspend fun deleteAll() =
        dao.deleteAll()
}
