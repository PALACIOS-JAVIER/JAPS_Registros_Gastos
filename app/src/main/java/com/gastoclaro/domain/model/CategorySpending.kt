package com.gastoclaro.domain.model

data class CategorySpending(
    val categoryId: Long?,
    val name: String,
    val icon: String,
    val color: Int,
    val total: Long
)
