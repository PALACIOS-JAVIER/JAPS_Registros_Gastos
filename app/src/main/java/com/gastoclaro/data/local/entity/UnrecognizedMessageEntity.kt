package com.gastoclaro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gastoclaro.domain.model.TransactionSource

@Entity(tableName = "unrecognized_messages")
data class UnrecognizedMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val text: String,
    val source: TransactionSource,
    val appPackage: String? = null,
    val sender: String? = null,
    val receivedAt: Long
)
