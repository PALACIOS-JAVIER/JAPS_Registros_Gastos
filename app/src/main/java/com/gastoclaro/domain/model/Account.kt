package com.gastoclaro.domain.model

data class Account(
    val id: Long = 0L,
    val name: String,
    val bank: String,
    val type: AccountType,
    val last4Digits: String? = null,
    val phoneNumber: String? = null,
    val holderName: String? = null,
    val initialBalance: Long = 0L,
    val color: Int = 0xFF4C8DFF.toInt(),
    val icon: String = "account_balance_wallet",
    val isArchived: Boolean = false
)
