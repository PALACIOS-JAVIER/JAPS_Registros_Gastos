package com.gastoclaro.domain.parser

import javax.inject.Inject

class BbvaParser @Inject constructor() : AbstractBankParser() {
    override val id: String = "bbva"
    override val packages: Set<String> = setOf("com.bbva")
    override val senders: Set<String> = setOf("bbva")
}
