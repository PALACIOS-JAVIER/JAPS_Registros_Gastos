package com.gastoclaro.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.gastoclaro.core.util.PermissionsHelper
import com.gastoclaro.ui.settings.SettingsViewModel
import com.gastoclaro.ui.theme.IncomeGreen

@Composable
fun OnboardingScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var notificationsOk by remember { mutableStateOf(false) }
    var smsOk by remember { mutableStateOf(false) }
    var batteryOk by remember { mutableStateOf(false) }
    var overlayOk by remember { mutableStateOf(false) }

    fun refresh() {
        notificationsOk = PermissionsHelper.isNotificationListenerEnabled(context)
        smsOk = PermissionsHelper.hasSmsPermission(context)
        batteryOk = PermissionsHelper.isIgnoringBatteryOptimizations(context)
        overlayOk = PermissionsHelper.canDrawOverlays(context)
    }

    LaunchedEffect(Unit) { refresh() }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val smsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { refresh() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "GastoClaro",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Detecta tus movimientos bancarios automáticamente y separa tus transferencias propias.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))

        PermissionItem(
            title = "Acceso a notificaciones",
            description = "Lee solo las notificaciones de tus apps bancarias.",
            granted = notificationsOk,
            actionLabel = "Conceder",
            onAction = { PermissionsHelper.openNotificationAccessSettings(context) }
        )
        PermissionItem(
            title = "Permisos de SMS",
            description = "Respaldo para detectar movimientos por mensaje de texto.",
            granted = smsOk,
            actionLabel = "Conceder",
            onAction = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    smsLauncher.launch(arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS))
                } else {
                    smsLauncher.launch(arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS))
                }
            }
        )
        PermissionItem(
            title = "Optimización de batería",
            description = "Evita que Android detenga la detección en segundo plano.",
            granted = batteryOk,
            actionLabel = "Excluir",
            onAction = { PermissionsHelper.openBatteryOptimizationSettings(context) }
        )
        PermissionItem(
            title = "Mostrar sobre otras apps",
            description = "Permite la burbuja flotante de preguntas.",
            granted = overlayOk,
            actionLabel = "Conceder",
            onAction = { PermissionsHelper.openOverlaySettings(context) }
        )

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                viewModel.setOnboardingCompleted(true)
                onComplete()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comenzar a usar GastoClaro")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    granted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.size(12.dp))
        if (granted) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(IncomeGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Check, contentDescription = "Concedido", tint = IncomeGreen)
            }
        } else {
            Button(onClick = onAction) {
                Text(actionLabel)
                Icon(Icons.Rounded.ChevronRight, contentDescription = null)
            }
        }
    }
}
