package com.gastoclaro.domain.model

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER_OUT,
    TRANSFER_IN,
    WITHDRAWAL;

    val isTransfer: Boolean get() = this == TRANSFER_OUT || this == TRANSFER_IN

    val isInternalTransfer: Boolean get() = isTransfer
}
