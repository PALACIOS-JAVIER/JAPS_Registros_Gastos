package com.gastoclaro.domain.matching

import com.gastoclaro.domain.model.Transaction

enum class InternalReason {
    KNOWN_COUNTERPARTY,
    ACCOUNT_MATCH,
    OPPOSITE_TRANSACTION
}

sealed interface TransferClassification {
    data object External : TransferClassification

    data class Internal(
        val reason: InternalReason,
        val counterpartAccountId: Long?,
        val oppositeTransaction: Transaction?
    ) : TransferClassification
}
