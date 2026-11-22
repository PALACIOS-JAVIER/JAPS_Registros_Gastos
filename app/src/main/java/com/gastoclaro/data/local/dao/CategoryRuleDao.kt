package com.gastoclaro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gastoclaro.data.local.entity.CategoryRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryRuleDao {

    @Query("SELECT * FROM category_rules ORDER BY usageCount DESC")
    fun observeAll(): Flow<List<CategoryRuleEntity>>

    @Query("SELECT * FROM category_rules ORDER BY usageCount DESC")
    suspend fun getAll(): List<CategoryRuleEntity>

    @Query("SELECT * FROM category_rules WHERE id = :id")
    suspend fun getById(id: Long): CategoryRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: CategoryRuleEntity): Long

    @Query("UPDATE category_rules SET usageCount = usageCount + 1, confidence = MIN(100, confidence + 5) WHERE id = :id")
    suspend fun incrementUsage(id: Long)

    @Query("DELETE FROM category_rules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM category_rules")
    suspend fun clearAll()
}
