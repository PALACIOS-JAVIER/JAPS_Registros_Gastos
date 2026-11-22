package com.gastoclaro.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gastoclaro.core.util.MoneyParser
import com.gastoclaro.domain.model.Account
import com.gastoclaro.domain.model.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountFormScreen(
    viewModel: AccountFormViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val existing by viewModel.existing.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(AccountType.SAVINGS) }
    var last4 by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var holderName by remember { mutableStateOf("") }
    var initialBalance by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(0xFF4C8DFF.toInt()) }
    var initialized by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(existing) {
        if (initialized || existing == null) return@LaunchedEffect
        name = existing!!.name
        bank = existing!!.bank
        type = existing!!.type
        last4 = existing!!.last4Digits.orEmpty()
        phone = existing!!.phoneNumber.orEmpty()
        holderName = existing!!.holderName.orEmpty()
        initialBalance = (existing!!.initialBalance / 100).toString()
        color = existing!!.color
        initialized = true
    }

    val colors = listOf(
        0xFF4C8DFF.toInt(), 0xFF2EC4B6.toInt(), 0xFF8B7CF8.toInt(),
        0xFFFF7A70.toInt(), 0xFFFFB020.toInt(), 0xFF34D399.toInt(),
        0xFFF05252.toInt(), 0xFF9C6ADE.toInt()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEdit) "Editar cuenta" else "Nueva cuenta") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver") }
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
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Alias (ej. Nequi)") }, singleLine = true)
            OutlinedTextField(value = bank, onValueChange = { bank = it }, label = { Text("Banco") }, singleLine = true)

            AccountTypeSelector(selected = type, onSelect = { type = it })

            OutlinedTextField(
                value = last4, onValueChange = { last4 = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text("Últimos 4 dígitos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Celular asociado") }, singleLine = true)
            OutlinedTextField(value = holderName, onValueChange = { holderName = it }, label = { Text("Nombre del titular") }, singleLine = true)
            OutlinedTextField(
                value = initialBalance, onValueChange = { initialBalance = it },
                label = { Text("Saldo inicial") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Text("Color", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colors.forEach { c ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(c))
                            .clickable { color = c }
                    )
                }
            }

            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val account = Account(
                            id = existing?.id ?: 0L,
                            name = name.trim(),
                            bank = bank.trim(),
                            type = type,
                            last4Digits = last4.ifBlank { null },
                            phoneNumber = phone.ifBlank { null },
                            holderName = holderName.ifBlank { null },
                            initialBalance = MoneyParser.parseAmountToken(initialBalance) ?: 0L,
                            color = color,
                            icon = existing?.icon ?: "account_balance_wallet",
                            isArchived = existing?.isArchived ?: false
                        )
                        viewModel.save(account)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (viewModel.isEdit) "Guardar cambios" else "Guardar") }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Eliminar cuenta") },
            text = { Text("Los movimientos de esta cuenta se mantendrán pero quedarán sin cuenta asignada.") },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountTypeSelector(selected: AccountType, onSelect: (AccountType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = accountTypeLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AccountType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(accountTypeLabel(type)) },
                    onClick = { onSelect(type); expanded = false }
                )
            }
        }
    }
}

private fun accountTypeLabel(type: AccountType): String = when (type) {
    AccountType.SAVINGS -> "Ahorros"
    AccountType.CHECKING -> "Corriente"
    AccountType.WALLET -> "Billetera"
    AccountType.CARD -> "Tarjeta de crédito"
    AccountType.CASH -> "Efectivo"
}
