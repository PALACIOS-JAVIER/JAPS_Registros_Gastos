package com.gastoclaro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gastoclaro.domain.model.CategoryType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val icon: String,
    val color: Int,
    val type: CategoryType,
    val isDefault: Boolean = false,
    val order: Int = 0
)
