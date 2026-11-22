package com.gastoclaro.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_questions",
    indices = [Index(value = ["createdAt"])]
)
data class PendingQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val transactionId: Long,
    val createdAt: Long,
    val questionText: String
)
