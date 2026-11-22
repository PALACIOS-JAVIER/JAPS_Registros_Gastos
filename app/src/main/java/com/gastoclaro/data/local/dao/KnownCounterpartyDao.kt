package com.gastoclaro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gastoclaro.data.local.entity.KnownCounterpartyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KnownCounterpartyDao {

    @Query("SELECT * FROM known_counterparties ORDER BY identifier")
    fun observeAll(): Flow<List<KnownCounterpartyEntity>>

    @Query("SELECT * FROM known_counterparties WHERE identifier = :identifier")
    suspend fun findByIdentifier(identifier: String): KnownCounterpartyEntity?

    @Query("SELECT * FROM known_counterparties ORDER BY identifier")
    suspend fun getAll(): List<KnownCounterpartyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(counterparty: KnownCounterpartyEntity): Long

    @Query("DELETE FROM known_counterparties WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM known_counterparties")
    suspend fun clearAll()
}
