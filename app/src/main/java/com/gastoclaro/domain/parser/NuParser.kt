package com.gastoclaro.domain.parser

import javax.inject.Inject

class NuParser @Inject constructor() : AbstractBankParser() {
    override val id: String = "nu"
    override val packages: Set<String> = setOf("co.nu", "com.nu", "nubank")
    override val senders: Set<String> = setOf("nu", "nubank")
}
