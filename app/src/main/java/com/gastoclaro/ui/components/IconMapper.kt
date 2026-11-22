package com.gastoclaro.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalAtm
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {
    fun icon(name: String): ImageVector = when (name) {
        "restaurant" -> Icons.Rounded.Restaurant
        "directions_bus" -> Icons.Rounded.DirectionsBus
        "shopping_cart" -> Icons.Rounded.ShoppingCart
        "receipt_long" -> Icons.Rounded.ReceiptLong
        "medical_services" -> Icons.Rounded.MedicalServices
        "movie" -> Icons.Rounded.Movie
        "school" -> Icons.Rounded.School
        "home" -> Icons.Rounded.Home
        "subscriptions" -> Icons.Rounded.Subscriptions
        "category" -> Icons.Rounded.Category
        "payments" -> Icons.Rounded.Payments
        "trending_up" -> Icons.Rounded.TrendingUp
        "account_balance" -> Icons.Rounded.AccountBalance
        "account_balance_wallet" -> Icons.Rounded.AccountBalanceWallet
        "credit_card" -> Icons.Rounded.CreditCard
        "savings" -> Icons.Rounded.Savings
        "local_atm" -> Icons.Rounded.LocalAtm
        else -> Icons.Rounded.Category
    }

    fun accountIcon(type: com.gastoclaro.domain.model.AccountType): ImageVector = when (type) {
        com.gastoclaro.domain.model.AccountType.SAVINGS -> Icons.Rounded.Savings
        com.gastoclaro.domain.model.AccountType.CHECKING -> Icons.Rounded.AccountBalance
        com.gastoclaro.domain.model.AccountType.WALLET -> Icons.Rounded.AccountBalanceWallet
        com.gastoclaro.domain.model.AccountType.CARD -> Icons.Rounded.CreditCard
        com.gastoclaro.domain.model.AccountType.CASH -> Icons.Rounded.LocalAtm
    }
}
