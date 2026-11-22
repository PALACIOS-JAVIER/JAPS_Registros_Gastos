package com.gastoclaro.domain.parser

import javax.inject.Inject

class BancolombiaParser @Inject constructor() : AbstractBankParser() {
    override val id: String = "bancolombia"
    override val packages: Set<String> = setOf("com.bancolombia")
    override val senders: Set<String> = setOf("bancolombia")
}
