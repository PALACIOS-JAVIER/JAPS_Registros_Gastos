package com.gastoclaro.domain.parser

import javax.inject.Inject

class NequiParser @Inject constructor() : AbstractBankParser() {
    override val id: String = "nequi"
    override val packages: Set<String> = setOf("com.nequi.mobileapp", "com.nequi")
    override val senders: Set<String> = setOf("nequi")
}
