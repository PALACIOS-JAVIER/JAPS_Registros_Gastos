package com.gastoclaro.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gastoclaro.core.util.DateFormats
import com.gastoclaro.core.util.MoneyParser
import com.gastoclaro.domain.model.Account
import com.gastoclaro.domain.model.Category
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.TransactionSource
import com.gastoclaro.domain.model.TransactionType
import com.gastoclaro.ui.components.transactionTypeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    viewModel: TransactionFormViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val existing by viewModel.existing.collectAsStateWithLifecycle()

    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf<Long?>(null) }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var merchant by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var initialized by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(existing, accounts) {
        if (initialized) return@LaunchedEffect
        if (viewModel.isEdit && existing != null) {
            type = existing!!.type
            amountText = (existing!!.amount / 100).toString()
            accountId = existing!!.accountId
            categoryId = existing!!.categoryId
            merchant = existing!!.merchant.orEmpty()
            note = existing!!.note.orEmpty()
            dateTime = existing!!.dateTime
            initialized = true
        } else if (!viewModel.isEdit) {
            accountId = accounts.firstOrNull { it.type == com.gastoclaro.domain.model.AccountType.CASH }?.id
                ?: accounts.firstOrNull()?.id
            initialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEdit) "Editar movimiento" else "Nuevo movimiento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (viewModel.isEdit) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TypeSelector(selected = type, onSelect = { type = it; categoryId = null })

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Monto") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            AccountSelector(
                selected = accountId,
                accounts = accounts,
                onSelect = { accountId = it }
            )

            if (type == TransactionType.EXPENSE || type == TransactionType.INCOME) {
                CategorySelector(
                    selected = categoryId,
                    categories = categories.filter { it.type == type.toCategoryType() },
                    onSelect = { categoryId = it }
                )
            }

            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Comercio / destinatario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Nota") },
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(onClick = { showDatePicker = true }) {
                Text("Fecha: ${DateFormats.fullDate(dateTime)}")
            }

            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            TextButton(
                onClick = {
                    val amount = MoneyParser.parseAmountToken(amountText)
                    val account = accountId
                    when {
                        amount == null || amount <= 0 -> error = "Ingresa un monto válido"
                        account == null -> error = "Selecciona una cuenta"
                        else -> {
                            val tx = Transaction(
                                id = existing?.id ?: 0L,
                                accountId = account,
                                type = type,
                                amount = amount,
                                dateTime = dateTime,
                                merchant = merchant.ifBlank { null },
                                categoryId = categoryId,
                                note = note.ifBlank { null },
                                source = TransactionSource.MANUAL,
                                rawTextHash = existing?.rawTextHash ?: "",
                                transferGroupId = existing?.transferGroupId,
                                linkedAccountId = existing?.linkedAccountId,
                                status = existing?.status ?: com.gastoclaro.domain.model.TransactionStatus.CONFIRMED,
                                reference = existing?.reference,
                                accountLast4 = existing?.accountLast4,
                                balanceAfter = existing?.balanceAfter,
                                isTransferInternal = existing?.isTransferInternal ?: false,
                                month = existing?.month ?: ""
                            )
                            viewModel.save(tx)
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (viewModel.isEdit) "Guardar cambios" else "Guardar")
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateTime)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { dateTime = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = state) }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Eliminar movimiento") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    existing?.let { viewModel.delete(it) }
                    confirmDelete = false
                    onBack()
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } }
        )
    }
}

private fun TransactionType.toCategoryType(): com.gastoclaro.domain.model.CategoryType =
    if (this == TransactionType.INCOME) com.gastoclaro.domain.model.CategoryType.INCOME
    else com.gastoclaro.domain.model.CategoryType.EXPENSE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeSelector(selected: TransactionType, onSelect: (TransactionType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = transactionTypeLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TransactionType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(transactionTypeLabel(type)) },
                    onClick = { onSelect(type); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSelector(selected: Long?, accounts: List<Account>, onSelect: (Long?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = accounts.firstOrNull { it.id == selected }?.name ?: "Selecciona una cuenta"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Cuenta") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.name) },
                    onClick = { onSelect(account.id); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelector(selected: Long?, categories: List<Category>, onSelect: (Long?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = categories.firstOrNull { it.id == selected }?.name ?: "Sin categoría"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoría") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Sin categoría") },
                onClick = { onSelect(null); expanded = false }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = { onSelect(category.id); expanded = false }
                )
            }
        }
    }
}
