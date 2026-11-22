package com.gastoclaro.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "category_rules",
    indices = [Index(value = ["pattern"], unique = true)]
)
data class CategoryRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val pattern: String,
    val categoryId: Long,
    val isLearned: Boolean = false,
    val confidence: Int = 0,
    val usageCount: Int = 0
)
