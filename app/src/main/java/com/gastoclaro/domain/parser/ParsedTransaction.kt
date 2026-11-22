package com.gastoclaro.domain.parser

import com.gastoclaro.domain.model.TransactionType

data class ParsedTransaction(
    val type: TransactionType,
    val amount: Long,
    val dateTime: Long,
    val merchant: String? = null,
    val accountLast4: String? = null,
    val reference: String? = null,
    val balanceAfter: Long? = null
)
