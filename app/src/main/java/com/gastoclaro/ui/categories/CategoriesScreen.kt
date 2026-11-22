package com.gastoclaro.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gastoclaro.core.util.MoneyFormatter
import com.gastoclaro.domain.model.Category
import com.gastoclaro.domain.model.CategoryRule
import com.gastoclaro.domain.model.CategoryType
import com.gastoclaro.ui.components.IconMapper

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CategoriesScreen(viewModel: CategoriesViewModel = hiltViewModel()) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val spending by viewModel.spending.collectAsStateWithLifecycle()
    val rules by viewModel.rules.collectAsStateWithLifecycle()

    var editing by remember { mutableStateOf<Category?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Category?>(null) }

    val expenseCategories = categories.filter { it.type == CategoryType.EXPENSE }
    val incomeCategories = categories.filter { it.type == CategoryType.INCOME }
    val totalByCategory = spending.associateBy({ it.categoryId }, { it.total })

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { creating = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Nueva categoría")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item { Text("Gastos", style = MaterialTheme.typography.titleMedium) }
            items(expenseCategories.size) { i ->
                val c = expenseCategories[i]
                CategoryRow(
                    category = c,
                    amount = totalByCategory[c.id],
                    onClick = { editing = c },
                    onLongClick = { deleting = c }
                )
            }
            item { Spacer(Modifier.height(16.dp)); Text("Ingresos", style = MaterialTheme.typography.titleMedium) }
            items(incomeCategories.size) { i ->
                val c = incomeCategories[i]
                CategoryRow(
                    category = c,
                    amount = totalByCategory[c.id],
                    onClick = { editing = c },
                    onLongClick = { deleting = c }
                )
            }

            if (rules.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Reglas de categorización", style = MaterialTheme.typography.titleMedium)
                }
                items(rules.size) { i ->
                    val rule = rules[i]
                    val catName = categories.firstOrNull { it.id == rule.categoryId }?.name ?: "?"
                    RuleRow(rule, catName, onDelete = { viewModel.deleteRule(rule) })
                }
            }
        }
    }

    if (creating) {
        CategoryDialog(
            title = "Nueva categoría",
            initial = null,
            onSave = { name, type ->
                viewModel.upsertCategory(
                    Category(
                        name = name,
                        icon = "category",
                        color = 0xFF64748B.toInt(),
                        type = type,
                        isDefault = false,
                        order = categories.size
                    )
                )
                creating = false
            },
            onDismiss = { creating = false }
        )
    }

    editing?.let { category ->
        CategoryDialog(
            title = "Editar categoría",
            initial = category,
            onSave = { name, type ->
                viewModel.upsertCategory(category.copy(name = name, type = type))
                editing = null
            },
            onDismiss = { editing = null }
        )
    }

    deleting?.let { category ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Eliminar categoría") },
            text = { Text("Se eliminará \"${category.name}\". Los movimientos quedarán sin categoría.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteCategory(category); deleting = null }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancelar") } }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CategoryRow(
    category: Category,
    amount: Long?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(category.color)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconMapper.icon(category.icon),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (amount != null) {
            Text(
                text = MoneyFormatter.formatShort(amount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RuleRow(rule: CategoryRule, categoryName: String, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("\"${rule.pattern}\"", style = MaterialTheme.typography.bodyMedium)
            Text(
                "→ $categoryName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Rounded.Delete, contentDescription = "Eliminar regla")
        }
    }
}

@Composable
private fun CategoryDialog(
    title: String,
    initial: Category?,
    onSave: (String, CategoryType) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var type by remember { mutableStateOf(initial?.type ?: CategoryType.EXPENSE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeChip("Gasto", type == CategoryType.EXPENSE) { type = CategoryType.EXPENSE }
                    TypeChip("Ingreso", type == CategoryType.INCOME) { type = CategoryType.INCOME }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onSave(name.trim(), type) }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun TypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
