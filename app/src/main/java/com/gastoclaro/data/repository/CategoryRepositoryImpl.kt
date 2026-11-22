package com.gastoclaro.data.repository

import com.gastoclaro.data.local.dao.CategoryDao
import com.gastoclaro.data.local.toDomain
import com.gastoclaro.data.local.toEntity
import com.gastoclaro.domain.model.Category
import com.gastoclaro.domain.model.CategoryType
import com.gastoclaro.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeCategories(type: CategoryType): Flow<List<Category>> =
        dao.observeByType(type).map { list -> list.map { it.toDomain() } }

    override suspend fun getCategory(id: Long): Category? =
        dao.getById(id)?.toDomain()

    override suspend fun getAllCategories(): List<Category> =
        dao.getAll().map { it.toDomain() }

    override suspend fun upsertCategory(category: Category): Long =
        dao.insert(category.toEntity())

    override suspend fun deleteCategory(category: Category) =
        dao.deleteById(category.id)

    override suspend fun insertDefaults(categories: List<Category>) =
        dao.insertAll(categories.map { it.toEntity() })

    override suspend fun count(): Int = dao.count()
}
