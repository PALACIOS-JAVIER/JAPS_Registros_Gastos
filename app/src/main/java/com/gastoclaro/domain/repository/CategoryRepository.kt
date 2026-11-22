package com.gastoclaro.domain.repository

import com.gastoclaro.domain.model.Category
import com.gastoclaro.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    fun observeCategories(type: CategoryType): Flow<List<Category>>
    suspend fun getCategory(id: Long): Category?
    suspend fun getAllCategories(): List<Category>
    suspend fun upsertCategory(category: Category): Long
    suspend fun deleteCategory(category: Category)
    suspend fun insertDefaults(categories: List<Category>)
    suspend fun count(): Int
}
