package com.gastoclaro.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gastoclaro.core.util.DateFormats
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.TransactionType
import com.gastoclaro.ui.components.EmptyState
import com.gastoclaro.ui.components.FilterMenuRow
import com.gastoclaro.ui.components.TransactionRow
import com.gastoclaro.ui.components.TypeFilterRow
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel = hiltViewModel(),
    onTransactionClick: (Long) -> Unit
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val deleted by viewModel.deleted.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showFilterDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Transaction?>(null) }

    LaunchedEffect(deleted) {
        deleted?.let {
            val result = snackbarHostState.showSnackbar(
                message = "Movimiento eliminado",
                actionLabel = "Deshacer"
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
        }
    }

    val accountNames = remember(accounts) { accounts.associate { it.id to it.name } }
    val categoryNames = remember(categories) { categories.associate { it.id to it.name } }

    val grouped = remember(transactions) {
        transactions
            .groupBy { dayKey(it.dateTime) }
            .toList()
            .sortedByDescending { it.first }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = filter.query,
                    onValueChange = viewModel::setQuery,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    singleLine = true
                )
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(Icons.Rounded.FilterList, contentDescription = "Filtros")
                }
            }

            if (filter.isActive) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = viewModel::clearFilters) {
                        Text("Limpiar filtros")
                    }
                }
            }

            if (grouped.isEmpty()) {
                EmptyState(message = "No hay movimientos que coincidan")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
                ) {
                    grouped.forEach { (_, dayTransactions) ->
                        item(key = "header-${dayKey(dayTransactions.first().dateTime)}") {
                            Text(
                                text = DateFormats.dayGroupLabel(dayTransactions.first().dateTime),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(dayTransactions, key = { it.id }) { tx ->
                            TransactionRow(
                                transaction = tx,
                                accountName = accountNames[tx.accountId],
                                categoryName = tx.categoryId?.let { categoryNames[it] },
                                onClick = { onTransactionClick(tx.id) },
                                onLongClick = { pendingDelete = tx }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            filter = filter,
            accounts = accounts,
            categories = categories,
            onApply = { f ->
                viewModel.setAccount(f.accountId)
                viewModel.setCategory(f.categoryId)
                viewModel.setType(f.type)
                viewModel.setDateRange(f.start, f.end)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    pendingDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Eliminar movimiento") },
            text = { Text("¿Quieres eliminar este movimiento?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(tx)
                    pendingDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

private fun dayKey(millis: Long): Long {
    val zone = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    return date.atStartOfDay(zone).toInstant().toEpochMilli()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDialog(
    filter: TransactionsViewModel.Filter,
    accounts: List<com.gastoclaro.domain.model.Account>,
    categories: List<com.gastoclaro.domain.model.Category>,
    onApply: (TransactionsViewModel.Filter) -> Unit,
    onDismiss: () -> Unit
) {
    var accountId by remember { mutableStateOf(filter.accountId) }
    var categoryId by remember { mutableStateOf(filter.categoryId) }
    var type by remember { mutableStateOf(filter.type) }
    var start by remember { mutableStateOf(filter.start) }
    var end by remember { mutableStateOf(filter.end) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterMenuRow(
                    label = "Cuenta",
                    value = accountId?.let { id -> accounts.firstOrNull { it.id == id }?.name } ?: "Todas",
                    options = listOf(null to "Todas") + accounts.map { it.id to it.name },
                    onSelect = { accountId = it }
                )
                FilterMenuRow(
                    label = "Categoría",
                    value = categoryId?.let { id -> categories.firstOrNull { it.id == id }?.name } ?: "Todas",
                    options = listOf(null to "Todas") + categories.map { it.id to it.name },
                    onSelect = { categoryId = it }
                )
                TypeFilterRow(
                    value = type,
                    onSelect = { type = it }
                )
                DateRangeRow(
                    start = start,
                    end = end,
                    onStart = { start = it },
                    onEnd = { end = it }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onApply(filter.copy(accountId = accountId, categoryId = categoryId, type = type, start = start, end = end))
            }) { Text("Aplicar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeRow(
    start: Long?,
    end: Long?,
    onStart: (Long?) -> Unit,
    onEnd: (Long?) -> Unit
) {
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { pickStart = true }) {
            Text(start?.let { "Desde: ${DateFormats.shortDate(it)}" } ?: "Desde")
        }
        TextButton(onClick = { pickEnd = true }) {
            Text(end?.let { "Hasta: ${DateFormats.shortDate(it)}" } ?: "Hasta")
        }
    }

    if (pickStart) {
        val state = rememberDatePickerState(initialSelectedDateMillis = start)
        DatePickerDialog(
            onDismissRequest = { pickStart = false },
            confirmButton = {
                TextButton(onClick = { onStart(state.selectedDateMillis); pickStart = false }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickStart = false }) { Text("Cancelar") } }
        ) { DatePicker(state = state) }
    }
    if (pickEnd) {
        val state = rememberDatePickerState(initialSelectedDateMillis = end)
        DatePickerDialog(
            onDismissRequest = { pickEnd = false },
            confirmButton = {
                TextButton(onClick = { onEnd(state.selectedDateMillis); pickEnd = false }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickEnd = false }) { Text("Cancelar") } }
        ) { DatePicker(state = state) }
    }
}
