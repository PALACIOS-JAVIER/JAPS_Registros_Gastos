package com.gastoclaro.domain.model

data class PendingQuestion(
    val id: Long = 0L,
    val transactionId: Long,
    val createdAt: Long,
    val questionText: String
)
