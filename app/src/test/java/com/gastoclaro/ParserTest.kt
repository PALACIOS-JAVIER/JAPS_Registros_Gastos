package com.gastoclaro

import com.gastoclaro.domain.model.TransactionSource
import com.gastoclaro.domain.model.TransactionType
import com.gastoclaro.domain.parser.BancolombiaParser
import com.gastoclaro.domain.parser.BbvaParser
import com.gastoclaro.domain.parser.DaviplataParser
import com.gastoclaro.domain.parser.DaviviendaParser
import com.gastoclaro.domain.parser.NequiParser
import com.gastoclaro.domain.parser.NuParser
import com.gastoclaro.domain.parser.RawMessage
import com.gastoclaro.domain.parser.TransactionParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ParserTest {

    @Test
    fun `bancolombia parses 6 message formats`() {
        val parser = BancolombiaParser()
        assertParse(parser, notif("com.bancolombia.app", "Compra por \$45.900 en EXITO con Tarjeta *1234. 22/11/2024 14:32"), TransactionType.EXPENSE, 4590000L, "EXITO")
        assertParse(parser, notif("com.bancolombia.app", "Transferencia enviada por \$150.000 a MARIA PEREZ desde cuenta *4567. 22/11 10:05"), TransactionType.TRANSFER_OUT, 15000000L, "MARIA PEREZ")
        assertParse(parser, notif("com.bancolombia.app", "Transferencia recibida por \$200.000 de JUAN GOMEZ a cuenta *4567. 21/11 09:00"), TransactionType.TRANSFER_IN, 20000000L, "JUAN GOMEZ")
        assertParse(parser, notif("com.bancolombia.app", "Retiro por \$300.000 en Cajero Bancolombia. 20/11 18:20"), TransactionType.WITHDRAWAL, 30000000L, "Cajero Bancolombia")
        assertParse(parser, notif("com.bancolombia.app", "Pago de servicios por \$120.000 a EPM. 19/11 08:00"), TransactionType.EXPENSE, 12000000L, "EPM")
        assertParse(parser, notif("com.bancolombia.app", "Depósito por \$500.000 a cuenta *4567. 18/11 12:00"), TransactionType.INCOME, 50000000L, null)
    }

    @Test
    fun `nequi parses 5 message formats`() {
        val parser = NequiParser()
        assertParse(parser, notif("com.nequi.MobileApp", "¡Listo! Enviaste \$25.000 a JUAN PEREZ. 22 nov 2:32 PM"), TransactionType.TRANSFER_OUT, 2500000L, "JUAN PEREZ")
        assertParse(parser, notif("com.nequi.MobileApp", "Te llegaron \$100.000 de MARIA GOMEZ. 22 nov 1:00 PM"), TransactionType.TRANSFER_IN, 10000000L, "MARIA GOMEZ")
        assertParse(parser, notif("com.nequi.MobileApp", "Compra en EXITO por \$35.000. 21 nov 3:15 PM"), TransactionType.EXPENSE, 3500000L, "EXITO")
        assertParse(parser, notif("com.nequi.MobileApp", "Retiraste \$50.000 en Cajero. 20 nov 5:00 PM"), TransactionType.WITHDRAWAL, 5000000L, "Cajero")
        assertParse(parser, notif("com.nequi.MobileApp", "Pagaste \$80.000 a CLARO. 19 nov 4:45 PM"), TransactionType.EXPENSE, 8000000L, "CLARO")
    }

    @Test
    fun `daviplata parses 5 message formats`() {
        val parser = DaviplataParser()
        assertParse(parser, sms("Daviplata", "Te llegó un giro de \$200.000 de CARLOS RUIZ. 22/11/2024 14:30"), TransactionType.TRANSFER_IN, 20000000L, "CARLOS RUIZ")
        assertParse(parser, sms("Daviplata", "Enviaste \$50.000 a ANA LOPEZ. 22/11/2024 10:00"), TransactionType.TRANSFER_OUT, 5000000L, "ANA LOPEZ")
        assertParse(parser, sms("Daviplata", "Pagaste \$120.000 en DROGUERIA. 21/11/2024 16:20"), TransactionType.EXPENSE, 12000000L, "DROGUERIA")
        assertParse(parser, sms("Daviplata", "Retiraste \$100.000 en cajero. 20/11/2024 11:00"), TransactionType.WITHDRAWAL, 10000000L, "Cajero")
        assertParse(parser, sms("Daviplata", "Compra por \$75.000 en ALKOSTO. 19/11/2024 18:00"), TransactionType.EXPENSE, 7500000L, "ALKOSTO")
    }

    @Test
    fun `davivienda parses 5 message formats`() {
        val parser = DaviviendaParser()
        assertParse(parser, notif("com.davivienda.movil", "Compra por \$89.900 en FALABELLA con Tarjeta *5678. 22/11/2024 15:00"), TransactionType.EXPENSE, 8990000L, "FALABELLA")
        assertParse(parser, notif("com.davivienda.movil", "Transferencia enviada por \$300.000 a PEDRO GARCIA desde cuenta *9012. 22/11 09:00"), TransactionType.TRANSFER_OUT, 30000000L, "PEDRO GARCIA")
        assertParse(parser, notif("com.davivienda.movil", "Transferencia recibida por \$150.000 de LUISA DIAZ a cuenta *9012. 21/11 14:00"), TransactionType.TRANSFER_IN, 15000000L, "LUISA DIAZ")
        assertParse(parser, notif("com.davivienda.movil", "Retiro en cajero por \$200.000. 20/11 10:00"), TransactionType.WITHDRAWAL, 20000000L, "Cajero")
        assertParse(parser, notif("com.davivienda.movil", "Pago de tarjeta por \$250.000. 19/11 08:30"), TransactionType.EXPENSE, 25000000L, null)
    }

    @Test
    fun `bbva parses 5 message formats`() {
        val parser = BbvaParser()
        assertParse(parser, notif("com.bbva.nexo", "Compra por \$120.000 en HOME CENTER con Tarjeta *3456. 22/11/2024 13:00"), TransactionType.EXPENSE, 12000000L, "HOME CENTER")
        assertParse(parser, notif("com.bbva.nexo", "Transferencia enviada por \$400.000 a MARTA SANCHEZ. 22/11 11:00"), TransactionType.TRANSFER_OUT, 40000000L, "MARTA SANCHEZ")
        assertParse(parser, notif("com.bbva.nexo", "Transferencia recibida por \$100.000 de JORGE MARTINEZ. 21/11 15:00"), TransactionType.TRANSFER_IN, 10000000L, "JORGE MARTINEZ")
        assertParse(parser, notif("com.bbva.nexo", "Retiro por \$150.000 en cajero BBVA. 20/11 09:00"), TransactionType.WITHDRAWAL, 15000000L, "Cajero BBVA")
        assertParse(parser, notif("com.bbva.nexo", "Pago de servicios por \$90.000 a ENEL. 19/11 17:00"), TransactionType.EXPENSE, 9000000L, "ENEL")
    }

    @Test
    fun `nu parses 5 message formats`() {
        val parser = NuParser()
        assertParse(parser, notif("co.nu", "Compra a crédito por \$50.000 en RAPPI. 22 nov 14:00"), TransactionType.EXPENSE, 5000000L, "RAPPI")
        assertParse(parser, notif("co.nu", "Pago recibido por \$300.000. 21 nov 10:00"), TransactionType.INCOME, 30000000L, null)
        assertParse(parser, notif("co.nu", "Transferencia enviada por \$120.000. 20 nov 16:00"), TransactionType.TRANSFER_OUT, 12000000L, null)
        assertParse(parser, notif("co.nu", "Compra por \$85.000 en AMAZON. 19 nov 12:00"), TransactionType.EXPENSE, 8500000L, "AMAZON")
        assertParse(parser, notif("co.nu", "Retiro por \$100.000 en cajero. 18 nov 08:00"), TransactionType.WITHDRAWAL, 10000000L, "Cajero")
    }

    @Test
    fun `ignores irrelevant bank notifications`() {
        val parser = BancolombiaParser()
        assertNull(parser.parse(notif("com.bancolombia.app", "Tu código de seguridad es 123456")))
        org.junit.Assert.assertFalse(parser.matches(notif("com.otra.app", "Compra por \$50.000 en EXITO")))
    }

    private fun notif(pkg: String, text: String) = RawMessage(
        source = TransactionSource.NOTIFICATION,
        appPackage = pkg,
        text = text,
        receivedAt = System.currentTimeMillis()
    )

    private fun sms(sender: String, text: String) = RawMessage(
        source = TransactionSource.SMS,
        sender = sender,
        text = text,
        receivedAt = System.currentTimeMillis()
    )

    private fun assertParse(
        parser: TransactionParser,
        raw: RawMessage,
        expectedType: TransactionType?,
        expectedAmount: Long?,
        expectedMerchant: String?
    ) {
        val result = parser.parse(raw)
        if (expectedType == null) {
            assertNull(result)
        } else {
            assertNotNull("expected parse of: ${raw.text}", result)
            assertEquals(expectedType, result!!.type)
            assertEquals(expectedAmount, result.amount)
            if (expectedMerchant != null) {
                assertEquals(expectedMerchant, result.merchant)
            }
        }
    }
}
