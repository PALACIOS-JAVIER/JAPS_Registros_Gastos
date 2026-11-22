package com.gastoclaro.domain.parser

object BankNames {
    fun display(parserId: String): String = when (parserId.lowercase()) {
        "bancolombia" -> "Bancolombia"
        "nequi" -> "Nequi"
        "daviplata" -> "Daviplata"
        "davivienda" -> "Davivienda"
        "bbva" -> "BBVA"
        "nu" -> "Nu"
        else -> parserId
    }

    fun isWallet(parserId: String): Boolean = parserId.lowercase() in setOf("nequi", "daviplata")

    fun isCardBank(parserId: String): Boolean = parserId.lowercase() == "nu"
}
