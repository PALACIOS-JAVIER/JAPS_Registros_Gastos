package com.gastoclaro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gastoclaro.ui.accounts.AccountFormScreen
import com.gastoclaro.ui.accounts.AccountsScreen
import com.gastoclaro.ui.budgets.BudgetsScreen
import com.gastoclaro.ui.categories.CategoriesScreen
import com.gastoclaro.ui.debug.DebugScreen
import com.gastoclaro.ui.home.HomeScreen
import com.gastoclaro.ui.navigation.Destinations
import com.gastoclaro.ui.onboarding.OnboardingScreen
import com.gastoclaro.ui.reports.ReportsScreen
import com.gastoclaro.ui.settings.BackupScreen
import com.gastoclaro.ui.settings.SettingsScreen
import com.gastoclaro.ui.settings.UnrecognizedMessagesScreen
import com.gastoclaro.ui.transactions.TransactionFormScreen
import com.gastoclaro.ui.transactions.TransactionsScreen

@Composable
fun AppRoot(appViewModel: AppViewModel = hiltViewModel()) {
    val start by appViewModel.startDestination.collectAsStateWithLifecycle()
    val destination = start ?: run {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }
    val navController = rememberNavController()
    AppScaffold(navController = navController, startDestination = destination)
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Destinations.HOME, "Inicio", Icons.Rounded.Home),
    Tab(Destinations.TRANSACTIONS, "Movimientos", Icons.Rounded.ReceiptLong),
    Tab(Destinations.CATEGORIES, "Categorías", Icons.Rounded.Category),
    Tab(Destinations.ACCOUNTS, "Cuentas", Icons.Rounded.AccountBalanceWallet),
    Tab(Destinations.BUDGETS, "Presupuestos", Icons.Rounded.PieChart),
    Tab(Destinations.REPORTS, "Reportes", Icons.Rounded.BarChart),
    Tab(Destinations.SETTINGS, "Ajustes", Icons.Rounded.Settings)
)

@Composable
private fun AppScaffold(navController: NavHostController, startDestination: String) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isMainTab = tabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (isMainTab) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.navigateToTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, maxLines = 1) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (isMainTab) {
                FloatingActionButton(onClick = { navController.navigate(Destinations.transactionForm(-1L)) }) {
                    Icon(Icons.Rounded.Add, contentDescription = "Nuevo movimiento")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destinations.ONBOARDING) {
                OnboardingScreen(onComplete = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.ONBOARDING) { inclusive = true }
                    }
                })
            }
            composable(Destinations.HOME) {
                HomeScreen(
                    onTransactionClick = { id -> navController.navigate(Destinations.transactionForm(id)) },
                    onSeeAll = { navController.navigateToTab(Destinations.TRANSACTIONS) },
                    onNavigatePending = { navController.navigateToTab(Destinations.TRANSACTIONS) }
                )
            }
            composable(Destinations.TRANSACTIONS) {
                TransactionsScreen(
                    onTransactionClick = { id -> navController.navigate(Destinations.transactionForm(id)) }
                )
            }
            composable(Destinations.CATEGORIES) { CategoriesScreen() }
            composable(Destinations.ACCOUNTS) {
                AccountsScreen(
                    onAccountClick = { id -> navController.navigate(Destinations.accountForm(id)) },
                    onAddAccount = { navController.navigate(Destinations.accountForm(-1L)) }
                )
            }
            composable(Destinations.BUDGETS) { BudgetsScreen() }
            composable(Destinations.REPORTS) { ReportsScreen() }
            composable(Destinations.SETTINGS) {
                SettingsScreen(
                    onNavigateUnrecognized = { navController.navigate(Destinations.UNRECOGNIZED) },
                    onNavigateDebug = { navController.navigate(Destinations.DEBUG) },
                    onNavigateBackup = { navController.navigate(Destinations.BACKUP) }
                )
            }
            composable(
                route = "transaction_form?transactionId={transactionId}",
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType; defaultValue = -1L })
            ) {
                TransactionFormScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = "account_form?accountId={accountId}",
                arguments = listOf(navArgument("accountId") { type = NavType.LongType; defaultValue = -1L })
            ) {
                AccountFormScreen(onBack = { navController.popBackStack() })
            }
            composable(Destinations.UNRECOGNIZED) {
                UnrecognizedMessagesScreen(onBack = { navController.popBackStack() })
            }
            composable(Destinations.DEBUG) {
                DebugScreen(onBack = { navController.popBackStack() })
            }
            composable(Destinations.BACKUP) {
                BackupScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
