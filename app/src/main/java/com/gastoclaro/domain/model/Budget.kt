package com.gastoclaro.domain.model

data class Budget(
    val id: Long = 0L,
    val categoryId: Long,
    val amount: Long
)
