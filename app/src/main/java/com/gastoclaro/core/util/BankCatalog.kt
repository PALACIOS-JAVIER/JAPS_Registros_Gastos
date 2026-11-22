package com.gastoclaro.core.util

object BankCatalog {
    val defaultPackages: Set<String> = setOf(
        "com.bancolombia.app",
        "com.nequi.MobileApp",
        "com.davivienda.daviplata",
        "com.davivienda.movil",
        "com.bbva.nexo",
        "co.nu"
    )

    val defaultSmsSenders: Set<String> = setOf(
        "Bancolombia",
        "Nequi",
        "Daviplata",
        "Davivienda",
        "BBVA",
        "Nu"
    )
}
