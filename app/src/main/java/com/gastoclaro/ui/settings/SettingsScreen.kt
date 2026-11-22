package com.gastoclaro.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gastoclaro.domain.model.BubbleMode

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateUnrecognized: () -> Unit,
    onNavigateDebug: () -> Unit,
    onNavigateBackup: () -> Unit
) {
    val bubbleEnabled by viewModel.bubbleEnabled.collectAsStateWithLifecycle()
    val bubbleMode by viewModel.bubbleMode.collectAsStateWithLifecycle()
    val matchWindow by viewModel.matchWindow.collectAsStateWithLifecycle()
    val biometric by viewModel.biometricEnabled.collectAsStateWithLifecycle()
    val flagSecure by viewModel.flagSecureEnabled.collectAsStateWithLifecycle()
    val packages by viewModel.monitoredPackages.collectAsStateWithLifecycle()
    val senders by viewModel.smsSenders.collectAsStateWithLifecycle()
    val minAmount by viewModel.bubbleMinAmount.collectAsStateWithLifecycle()
    val dndStart by viewModel.dndStart.collectAsStateWithLifecycle()
    val dndEnd by viewModel.dndEnd.collectAsStateWithLifecycle()
    val collapse by viewModel.collapseSeconds.collectAsStateWithLifecycle()
    val neverAsk by viewModel.neverAsk.collectAsStateWithLifecycle()

    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
    ) {
        item { SettingsHeader("Detección automática") }
        item { ClickRow("Bancos monitoreados", "${packages.size} apps") { dialog = SettingsDialog.PACKAGES } }
        item { ClickRow("Remitentes SMS", "${senders.size} remitentes") { dialog = SettingsDialog.SENDERS } }
        item { ClickRow("Ventana de emparejamiento", "${matchWindow} min") { dialog = SettingsDialog.WINDOW } }

        item { SettingsHeader("Burbuja de preguntas") }
        item {
            SwitchRow("Burbuja activada", "Preguntar por gastos externos", bubbleEnabled, viewModel::setBubbleEnabled)
        }
        item { ClickRow("Modo de burbuja", modeLabel(bubbleMode)) { dialog = SettingsDialog.MODE } }
        item { ClickRow("Monto mínimo para preguntar", moneyLabel(minAmount)) { dialog = SettingsDialog.MIN_AMOUNT } }
        item { ClickRow("No molestar", "$dndStart – $dndEnd") { dialog = SettingsDialog.DND } }
        item { ClickRow("Contracción automática", "${collapse}s") { dialog = SettingsDialog.COLLAPSE } }
        item { ClickRow("No preguntar por", "${neverAsk.size} destinatarios") { dialog = SettingsDialog.NEVER_ASK } }

        item { SettingsHeader("Privacidad y seguridad") }
        item { SwitchRow("Bloqueo biométrico", "Pedir huella al abrir la app", biometric, viewModel::setBiometricEnabled) }
        item { SwitchRow("Ocultar saldos (FLAG_SECURE)", "Bloquear capturas de pantalla", flagSecure, viewModel::setFlagSecureEnabled) }

        item { SettingsHeader("Datos") }
        item { ClickRow("Respaldo y restauración", "Copia local cifrada") { onNavigateBackup() } }
        item { ClickRow("Mensajes no reconocidos", "Crear reglas manuales") { onNavigateUnrecognized() } }

        item { SettingsHeader("Desarrollador") }
        item { ClickRow("Modo debug", "Simular notificaciones y SMS") { onNavigateDebug() } }
    }

    when (dialog) {
        SettingsDialog.PACKAGES -> SetEditorDialog(
            title = "Bancos monitoreados",
            values = packages,
            onSave = viewModel::setMonitoredPackages,
            onDismiss = { dialog = null }
        )
        SettingsDialog.SENDERS -> SetEditorDialog(
            title = "Remitentes SMS",
            values = senders,
            onSave = viewModel::setSmsSenders,
            onDismiss = { dialog = null }
        )
        SettingsDialog.WINDOW -> NumberDialog(
            title = "Ventana de emparejamiento (min)",
            initial = matchWindow.toLong(),
            onSave = { viewModel.setMatchWindow(it.toInt()); dialog = null },
            onDismiss = { dialog = null }
        )
        SettingsDialog.MIN_AMOUNT -> NumberDialog(
            title = "Monto mínimo para preguntar",
            initial = minAmount,
            onSave = { viewModel.setBubbleMinAmount(it); dialog = null },
            onDismiss = { dialog = null }
        )
        SettingsDialog.COLLAPSE -> NumberDialog(
            title = "Contracción automática (seg)",
            initial = collapse.toLong(),
            onSave = { viewModel.setCollapseSeconds(it.toInt()); dialog = null },
            onDismiss = { dialog = null }
        )
        SettingsDialog.NEVER_ASK -> SetEditorDialog(
            title = "No preguntar por",
            values = neverAsk,
            onSave = viewModel::setNeverAsk,
            onDismiss = { dialog = null }
        )
        SettingsDialog.DND -> DndDialog(
            start = dndStart,
            end = dndEnd,
            onSave = { s, e -> viewModel.setDndWindow(s, e); dialog = null },
            onDismiss = { dialog = null }
        )
        SettingsDialog.MODE -> ModeDialog(
            current = bubbleMode,
            onSelect = { viewModel.setBubbleMode(it); dialog = null },
            onDismiss = { dialog = null }
        )
        null -> Unit
    }
}

private enum class SettingsDialog { PACKAGES, SENDERS, WINDOW, MODE, MIN_AMOUNT, DND, COLLAPSE, NEVER_ASK }

private fun modeLabel(mode: BubbleMode): String = when (mode) {
    BubbleMode.OVERLAY -> "Burbuja flotante"
    BubbleMode.NATIVE -> "Burbuja nativa"
    BubbleMode.NOTIFICATION -> "Solo notificación"
}

private fun moneyLabel(amount: Long): String = if (amount <= 0) "Siempre" else com.gastoclaro.core.util.MoneyFormatter.formatShort(amount)

@Composable
private fun ModeDialog(current: BubbleMode, onSelect: (BubbleMode) -> Unit, onDismiss: () -> Unit) {
    val options = listOf(
        BubbleMode.OVERLAY to "Burbuja flotante (overlay)",
        BubbleMode.NATIVE to "Burbuja nativa (Android 11+)",
        BubbleMode.NOTIFICATION to "Solo notificación"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modo de burbuja") },
        text = {
            Column {
                options.forEach { (mode, label) ->
                    TextButton(onClick = { onSelect(mode) }, modifier = Modifier.fillMaxWidth()) {
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
private fun NumberDialog(title: String, initial: Long, onSave: (Long) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(initial.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = { TextButton(onClick = { text.toLongOrNull()?.let(onSave) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun DndDialog(start: String, end: String, onSave: (String, String) -> Unit, onDismiss: () -> Unit) {
    var s by remember { mutableStateOf(start) }
    var e by remember { mutableStateOf(end) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Horario de no molestar") },
        text = {
            Column {
                OutlinedTextField(s, { s = it }, label = { Text("Desde (HH:mm)") }, singleLine = true)
                Spacer(Modifier.padding(4.dp))
                OutlinedTextField(e, { e = it }, label = { Text("Hasta (HH:mm)") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { onSave(s, e) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun SetEditorDialog(
    title: String,
    values: Set<String>,
    onSave: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(values.joinToString("\n")) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Uno por línea") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                val set = text.lines().map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                onSave(set)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ClickRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        if (subtitle.isNotBlank()) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
}
