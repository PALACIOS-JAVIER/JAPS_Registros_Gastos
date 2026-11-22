package com.gastoclaro.domain.model

data class Category(
    val id: Long = 0L,
    val name: String,
    val icon: String,
    val color: Int,
    val type: CategoryType,
    val isDefault: Boolean = false,
    val order: Int = 0
)
