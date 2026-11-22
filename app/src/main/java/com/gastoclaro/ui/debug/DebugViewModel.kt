package com.gastoclaro.ui.debug

import androidx.lifecycle.ViewModel
import com.gastoclaro.domain.model.TransactionSource
import com.gastoclaro.domain.parser.ParsedResult
import com.gastoclaro.domain.parser.RawMessage
import com.gastoclaro.domain.parser.ParserEngine
import com.gastoclaro.domain.usecase.DetectionResult
import com.gastoclaro.domain.usecase.ProcessDetectedMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val parserEngine: ParserEngine,
    private val processDetectedMessage: ProcessDetectedMessageUseCase
) : ViewModel() {

    data class Sample(val bank: String, val raw: RawMessage)

    val samples: List<Sample> = listOf(
        Sample("Bancolombia", RawMessage(TransactionSource.NOTIFICATION, "com.bancolombia.app", null, "Compra por \$45.900 en EXITO con Tarjeta *1234. 22/11/2024 14:32", 0L)),
        Sample("Bancolombia", RawMessage(TransactionSource.NOTIFICATION, "com.bancolombia.app", null, "Transferencia enviada por \$150.000 a MARIA PEREZ desde cuenta *4567. 22/11 10:05", 0L)),
        Sample("Bancolombia", RawMessage(TransactionSource.NOTIFICATION, "com.bancolombia.app", null, "Transferencia recibida por \$200.000 de JUAN GOMEZ a cuenta *4567. 21/11 09:00", 0L)),
        Sample("Nequi", RawMessage(TransactionSource.NOTIFICATION, "com.nequi.MobileApp", null, "¡Listo! Enviaste \$25.000 a JUAN PEREZ. 22 nov 2:32 PM", 0L)),
        Sample("Nequi", RawMessage(TransactionSource.SMS, null, "Nequi", "Te llegaron \$100.000 de MARIA GOMEZ. 22 nov 1:00 PM", 0L)),
        Sample("Daviplata", RawMessage(TransactionSource.SMS, null, "Daviplata", "Te llegó un giro de \$200.000 de CARLOS RUIZ. 22/11/2024 14:30", 0L)),
        Sample("Davivienda", RawMessage(TransactionSource.NOTIFICATION, "com.davivienda.movil", null, "Compra por \$89.900 en FALABELLA con Tarjeta *5678. 22/11/2024 15:00", 0L)),
        Sample("BBVA", RawMessage(TransactionSource.NOTIFICATION, "com.bbva.nexo", null, "Transferencia recibida por \$100.000 de JORGE MARTINEZ. 21/11 15:00", 0L)),
        Sample("Nu", RawMessage(TransactionSource.NOTIFICATION, "co.nu", null, "Compra a crédito por \$50.000 en RAPPI. 22 nov 14:00", 0L))
    )

    suspend fun testParse(raw: RawMessage): ParsedResult? = parserEngine.parse(raw)

    suspend fun inject(raw: RawMessage): DetectionResult = processDetectedMessage(raw)
}
