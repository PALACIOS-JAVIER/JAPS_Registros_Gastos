package com.gastoclaro.domain.model

data class CategoryRule(
    val id: Long = 0L,
    val pattern: String,
    val categoryId: Long,
    val isLearned: Boolean = false,
    val confidence: Int = 0,
    val usageCount: Int = 0
)
