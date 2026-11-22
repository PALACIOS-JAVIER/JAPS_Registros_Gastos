package com.gastoclaro.ui.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gastoclaro.core.util.MoneyFormatter
import com.gastoclaro.domain.model.TransactionSource
import com.gastoclaro.domain.parser.ParsedResult
import com.gastoclaro.domain.parser.RawMessage
import com.gastoclaro.domain.usecase.DetectionResult
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    viewModel: DebugViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var packageSender by remember { mutableStateOf("com.bancolombia.app") }
    var source by remember { mutableStateOf(TransactionSource.NOTIFICATION) }
    var parseResult by remember { mutableStateOf("") }
    var injectResult by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modo debug") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver") }
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
            Text("Mensajes de ejemplo", style = MaterialTheme.typography.titleMedium)
            viewModel.samples.forEach { sample ->
                TextButton(onClick = {
                    text = sample.raw.text
                    packageSender = sample.raw.appPackage ?: sample.raw.sender.orEmpty()
                    source = sample.raw.source
                }) {
                    Text("${sample.bank}: ${sample.raw.text.take(50)}...")
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { source = TransactionSource.NOTIFICATION }) {
                    Text(if (source == TransactionSource.NOTIFICATION) "• Notificación" else "Notificación")
                }
                TextButton(onClick = { source = TransactionSource.SMS }) {
                    Text(if (source == TransactionSource.SMS) "• SMS" else "SMS")
                }
            }

            OutlinedTextField(
                value = packageSender,
                onValueChange = { packageSender = it },
                label = { Text(if (source == TransactionSource.NOTIFICATION) "Paquete de la app" else "Remitente") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Mensaje") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            val raw = RawMessage(source, packageSender.takeIf { source == TransactionSource.NOTIFICATION }, packageSender.takeIf { source == TransactionSource.SMS }, text, System.currentTimeMillis())
                            parseResult = formatParsed(viewModel.testParse(raw))
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Probar parser") }

                Button(
                    onClick = {
                        scope.launch {
                            val raw = RawMessage(source, packageSender.takeIf { source == TransactionSource.NOTIFICATION }, packageSender.takeIf { source == TransactionSource.SMS }, text, System.currentTimeMillis())
                            injectResult = formatDetection(viewModel.inject(raw))
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Registrar") }
            }

            if (parseResult.isNotEmpty()) {
                ResultCard("Resultado del parser", parseResult)
            }
            if (injectResult.isNotEmpty()) {
                ResultCard("Resultado del registro", injectResult)
            }
        }
    }
}

@Composable
private fun ResultCard(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatParsed(result: ParsedResult?): String {
    if (result == null) return "No reconocido"
    val p = result.parsed
    return "Banco: ${result.parserId}\n" +
        "Tipo: ${p.type}\n" +
        "Monto: ${MoneyFormatter.formatShort(p.amount)}\n" +
        "Comercio: ${p.merchant ?: "-"}\n" +
        "Cuenta: ${p.accountLast4 ?: "-"}\n" +
        "Referencia: ${p.reference ?: "-"}\n" +
        "Saldo: ${p.balanceAfter?.let { MoneyFormatter.formatShort(it) } ?: "-"}"
}

private fun formatDetection(result: DetectionResult): String = when (result) {
    is DetectionResult.Detected -> "Registrado (id=${result.transaction.id}) como ${result.transaction.type}" +
        if (result.transaction.isTransferInternal) " [transferencia propia]" else ""
    DetectionResult.Duplicate -> "Duplicado (ya existía)"
    DetectionResult.Unrecognized -> "No reconocido (guardado en no reconocidos)"
}
