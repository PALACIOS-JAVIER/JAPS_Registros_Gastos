package com.gastoclaro.domain.model

data class Transaction(
    val id: Long = 0L,
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
