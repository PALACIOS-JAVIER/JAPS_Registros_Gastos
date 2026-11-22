package com.gastoclaro.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "known_counterparties",
    indices = [Index(value = ["identifier"], unique = true)]
)
data class KnownCounterpartyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val identifier: String,
    val isMine: Boolean,
    val accountId: Long? = null
)
