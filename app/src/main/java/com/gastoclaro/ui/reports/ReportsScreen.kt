package com.gastoclaro.ui.reports

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gastoclaro.core.util.CsvExporter
import com.gastoclaro.core.util.DateFormats
import com.gastoclaro.core.util.MoneyFormatter
import com.gastoclaro.ui.components.TrendBars
import com.gastoclaro.ui.theme.ExpenseCoral
import com.gastoclaro.ui.theme.IncomeGreen
import java.io.File
import kotlinx.coroutines.launch

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val trend by viewModel.yearlyTrend.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val current = trend.firstOrNull { it.month == DateFormats.currentMonthKey() }
        ?: com.gastoclaro.domain.model.MonthlySummary(DateFormats.currentMonthKey(), 0, 0)
    val previousKey = com.gastoclaro.core.util.MonthRange.monthKeys(2).first()
    val previous = trend.firstOrNull { it.month == previousKey }
        ?: com.gastoclaro.domain.model.MonthlySummary(previousKey, 0, 0)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text("Reporte mensual", style = MaterialTheme.typography.headlineMedium)
                Text(
                    DateFormats.monthLabel(current.month),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Ingresos", MoneyFormatter.formatShort(current.income), IncomeGreen, Modifier.weight(1f))
                MetricCard("Gastos", MoneyFormatter.formatShort(current.expense), ExpenseCoral, Modifier.weight(1f))
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp)
            ) {
                Text("Comparación con el mes anterior", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Ingresos: ${MoneyFormatter.formatShort(current.income)} vs ${MoneyFormatter.formatShort(previous.income)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Gastos: ${MoneyFormatter.formatShort(current.expense)} vs ${MoneyFormatter.formatShort(previous.expense)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp)
            ) {
                Text("Gastos de los últimos 12 meses", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                TrendBars(
                    data = trend.reversed(),
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    color = ExpenseCoral
                )
            }
        }

        item {
            Button(
                onClick = {
                    scope.launch {
                        val tx = viewModel.transactionsForCsv()
                        val csv = CsvExporter.build(
                            tx,
                            accounts.associate { it.id to it.name },
                            categories.associate { it.id to it.name }
                        )
                        val dir = context.cacheDir
                        val file = File(dir, "gastoclaro_report.csv")
                        file.writeText(csv)
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Exportar CSV"))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.FileDownload, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text("  Exportar CSV")
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
    }
}
