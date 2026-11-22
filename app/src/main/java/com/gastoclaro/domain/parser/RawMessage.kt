package com.gastoclaro.domain.parser

import com.gastoclaro.domain.model.TransactionSource

data class RawMessage(
    val source: TransactionSource,
    val appPackage: String? = null,
    val sender: String? = null,
    val text: String,
    val receivedAt: Long
)
