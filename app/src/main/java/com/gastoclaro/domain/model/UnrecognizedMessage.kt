package com.gastoclaro.domain.model

data class UnrecognizedMessage(
    val id: Long = 0L,
    val text: String,
    val source: TransactionSource,
    val appPackage: String? = null,
    val sender: String? = null,
    val receivedAt: Long
)
