package com.gastoclaro.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gastoclaro.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterMenuRow(
    label: String,
    value: String,
    options: List<Pair<Long?, String>>,
    onSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeFilterRow(
    value: TransactionType?,
    onSelect: (TransactionType?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = value?.let { transactionTypeLabel(it) } ?: "Todos"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = current,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipo") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Todos") }, onClick = { onSelect(null); expanded = false })
            TransactionType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(transactionTypeLabel(type)) },
                    onClick = { onSelect(type); expanded = false }
                )
            }
        }
    }
}

fun transactionTypeLabel(type: TransactionType): String = when (type) {
    TransactionType.EXPENSE -> "Gasto"
    TransactionType.INCOME -> "Ingreso"
    TransactionType.TRANSFER_OUT -> "Transferencia enviada"
    TransactionType.TRANSFER_IN -> "Transferencia recibida"
    TransactionType.WITHDRAWAL -> "Retiro"
}
