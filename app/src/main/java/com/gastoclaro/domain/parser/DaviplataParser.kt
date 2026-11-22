package com.gastoclaro.domain.parser

import javax.inject.Inject

class DaviplataParser @Inject constructor() : AbstractBankParser() {
    override val id: String = "daviplata"
    override val packages: Set<String> = setOf("com.davivienda.daviplata", "daviplata")
    override val senders: Set<String> = setOf("daviplata")
}
