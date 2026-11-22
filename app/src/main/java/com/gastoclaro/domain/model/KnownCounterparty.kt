package com.gastoclaro.domain.model

data class KnownCounterparty(
    val id: Long = 0L,
    val identifier: String,
    val isMine: Boolean,
    val accountId: Long? = null
)
