package com.gastoclaro.ui.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gastoclaro.core.util.MoneyFormatter
import com.gastoclaro.domain.model.Budget
import com.gastoclaro.ui.components.EmptyState
import com.gastoclaro.ui.theme.ExpenseCoral
import com.gastoclaro.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun BudgetsScreen(viewModel: BudgetsViewModel = hiltViewModel()) {
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val spending by viewModel.spending.collectAsStateWithLifecycle()

    val spendingByCategory = spending.associateBy({ it.categoryId }, { it.total })
    val categoryName = categories.associateBy({ it.id }, { it.name })

    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Budget?>(null) }
    var deleting by remember { mutableStateOf<Budget?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Nuevo presupuesto")
            }
        }
    ) { padding ->
        if (budgets.isEmpty()) {
            EmptyState(
                message = "Define un límite mensual por categoría",
                icon = Icons.Rounded.Add
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(budgets.size) { index ->
                    val budget = budgets[index]
                    val spent = spendingByCategory[budget.categoryId] ?: 0L
                    BudgetCard(
                        name = categoryName[budget.categoryId] ?: "Categoría",
                        amount = budget.amount,
                        spent = spent,
                        onClick = { editing = budget },
                        onLongClick = { deleting = budget }
                    )
                }
            }
        }
    }

    if (showDialog) {
        BudgetDialog(
            title = "Nuevo presupuesto",
            initial = null,
            categories = categories.filter { c -> budgets.none { it.categoryId == c.id } },
            onSave = { categoryId, amount ->
                viewModel.upsertBudget(Budget(categoryId = categoryId, amount = amount))
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }

    editing?.let { budget ->
        BudgetDialog(
            title = "Editar presupuesto",
            initial = budget,
            categories = categories,
            onSave = { _, amount ->
                viewModel.upsertBudget(budget.copy(amount = amount))
                editing = null
            },
            onDismiss = { editing = null }
        )
    }

    deleting?.let { budget ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Eliminar presupuesto") },
            text = { Text("Se eliminará el límite de esta categoría.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteBudget(budget); deleting = null }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancelar") } }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun BudgetCard(
    name: String,
    amount: Long,
    spent: Long,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val progress = if (amount > 0) (spent.toFloat() / amount.toFloat()).coerceIn(0f, 1.5f) else 0f
    val color = when {
        progress >= 1f -> ExpenseCoral
        progress >= 0.8f -> Color(0xFFFFB020)
        else -> IncomeGreen
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress.coerceAtMost(1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${MoneyFormatter.formatShort(spent)} de ${MoneyFormatter.formatShort(amount)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetDialog(
    title: String,
    initial: Budget?,
    categories: List<com.gastoclaro.domain.model.Category>,
    onSave: (Long, Long) -> Unit,
    onDismiss: () -> Unit
) {
    var categoryId by remember { mutableStateOf(initial?.categoryId ?: categories.firstOrNull()?.id) }
    var amount by remember { mutableStateOf((initial?.amount?.div(100))?.toString().orEmpty()) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = categories.firstOrNull { it.id == categoryId }?.name ?: "Categoría",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = { categoryId = category.id; expanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Límite mensual") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val id = categoryId ?: return@Button
                val value = com.gastoclaro.core.util.MoneyParser.parseAmountToken(amount) ?: return@Button
                onSave(id, value)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
