package com.gastoclaro.ui.navigation

object Destinations {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val TRANSACTIONS = "transactions"
    const val CATEGORIES = "categories"
    const val ACCOUNTS = "accounts"
    const val BUDGETS = "budgets"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"
    const val TRANSACTION_FORM = "transaction_form"
    const val ACCOUNT_FORM = "account_form"
    const val UNRECOGNIZED = "unrecognized"
    const val DEBUG = "debug"
    const val BACKUP = "backup"

    fun transactionForm(transactionId: Long) = "transaction_form?transactionId=$transactionId"
    fun accountForm(accountId: Long) = "account_form?accountId=$accountId"
}
