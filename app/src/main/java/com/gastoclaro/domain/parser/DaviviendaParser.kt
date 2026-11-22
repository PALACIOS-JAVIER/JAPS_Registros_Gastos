package com.gastoclaro.domain.parser

import javax.inject.Inject

class DaviviendaParser @Inject constructor() : AbstractBankParser() {
    override val id: String = "davivienda"
    override val packages: Set<String> = setOf("com.davivienda")
    override val senders: Set<String> = setOf("davivienda")
}
