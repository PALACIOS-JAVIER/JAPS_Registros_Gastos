package com.gastoclaro.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gastoclaro.core.util.DateFormats
import com.gastoclaro.core.util.MoneyFormatter
import com.gastoclaro.ui.components.DonutChart
import com.gastoclaro.ui.components.TransactionRow
import com.gastoclaro.ui.components.TrendBars
import com.gastoclaro.ui.theme.ExpenseCoral
import com.gastoclaro.ui.theme.IncomeGreen

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onTransactionClick: (Long) -> Unit,
    onSeeAll: () -> Unit,
    onNavigatePending: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Saldo total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = MoneyFormatter.formatShort(state.totalBalance),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    label = "Gastado en el mes",
                    value = MoneyFormatter.formatShort(state.monthExpense),
                    color = ExpenseCoral
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    label = "Ingresos",
                    value = MoneyFormatter.formatShort(state.monthIncome),
                    color = IncomeGreen
                )
            }
        }

        if (state.pendingReview > 0) {
            item {
                PendingReviewCard(count = state.pendingReview, onClick = onNavigatePending)
            }
        }

        if (state.categorySpending.isNotEmpty()) {
            item {
                SectionCard(title = "Gastos por categoría") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        DonutChart(
                            items = state.categorySpending.take(6),
                            modifier = Modifier.size(180.dp),
                            centerTop = "Gastado",
                            centerBottom = MoneyFormatter.formatShort(
                                state.categorySpending.sumOf { it.total }
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                        state.categorySpending.take(5).forEach { item ->
                            LegendRow(item.name, item.total, Color(item.color))
                        }
                    }
                }
            }
        }

        item {
            SectionCard(title = "Últimos 6 meses") {
                TrendBars(
                    data = state.trend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    color = ExpenseCoral
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    state.trend.forEach { month ->
                        Text(
                            text = DateFormats.monthLabel(month.month).take(3).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Últimos movimientos", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Ver todos",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable(onClick = onSeeAll)
                        .padding(4.dp)
                )
            }
        }

        if (state.recent.isEmpty()) {
            item {
                Text(
                    text = "Aún no hay movimientos. Se detectarán automáticamente o agrégalos con el botón +.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(state.recent.size) { index ->
                val tx = state.recent[index]
                TransactionRow(
                    transaction = tx,
                    accountName = null,
                    categoryName = null,
                    onClick = { onTransactionClick(tx.id) }
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun LegendRow(name: String, amount: Long, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = MoneyFormatter.formatShort(amount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PendingReviewCard(count: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "$count movimiento(s) por revisar",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Rounded.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
