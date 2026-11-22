package com.gastoclaro.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gastoclaro.domain.model.TransactionSource
import com.gastoclaro.domain.model.TransactionStatus
import com.gastoclaro.domain.model.TransactionType

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["rawTextHash"]),
        Index(value = ["transferGroupId"]),
        Index(value = ["accountId"]),
        Index(value = ["dateTime"]),
        Index(value = ["categoryId"]),
        Index(value = ["month"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val accountId: Long,
    val type: TransactionType,
    val amount: Long,
    val dateTime: Long,
    val merchant: String? = null,
    val categoryId: Long? = null,
    val note: String? = null,
    val source: TransactionSource,
    val rawTextHash: String = "",
    val transferGroupId: String? = null,
    val linkedAccountId: Long? = null,
    val status: TransactionStatus = TransactionStatus.CONFIRMED,
    val reference: String? = null,
    val accountLast4: String? = null,
    val balanceAfter: Long? = null,
    val isTransferInternal: Boolean = false,
    val month: String = ""
)
