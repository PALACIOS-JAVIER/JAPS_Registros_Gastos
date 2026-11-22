package com.gastoclaro

import com.gastoclaro.domain.matching.TransferMatching
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.TransactionSource
import com.gastoclaro.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransferMatchingTest {

    @Test
    fun `names match ignoring case and accents`() {
        assertTrue(TransferMatching.namesMatch("MARIA PEREZ", "maria perez"))
        assertTrue(TransferMatching.namesMatch("PÉREZ", "perez"))
        assertTrue(TransferMatching.namesMatch("José García", "JOSE GARCIA"))
        assertFalse(TransferMatching.namesMatch("MARIA PEREZ", "JUAN GOMEZ"))
    }

    @Test
    fun `names match with partial names`() {
        assertTrue(TransferMatching.namesMatch("maria", "MARIA PEREZ RODRIGUEZ"))
    }

    @Test
    fun `commission does not break amount matching`() {
        // 4x1000 -> outgoing includes commission (0.4%)
        assertTrue(TransferMatching.amountsMatch(100000L, 100400L))
        assertTrue(TransferMatching.amountsMatch(100400L, 100000L))
        assertFalse(TransferMatching.amountsMatch(100000L, 200000L))
    }

    @Test
    fun `opposite transactions match within window`() {
        val incoming = tx(1, 2, TransactionType.TRANSFER_IN, 100000L, 1_000_000L)
        val outgoing = tx(2, 1, TransactionType.TRANSFER_OUT, 100400L, 1_000_000L + 60_000L)
        val result = TransferMatching.findOpposite(outgoing, listOf(incoming), windowMillis = 10 * 60_000L)
        assertEquals(incoming.id, result?.id)
    }

    @Test
    fun `opposite transactions outside window do not match`() {
        val incoming = tx(1, 2, TransactionType.TRANSFER_IN, 100000L, 1_000_000L)
        val outgoing = tx(2, 1, TransactionType.TRANSFER_OUT, 100400L, 1_000_000L + 11 * 60_000L)
        assertNull(TransferMatching.findOpposite(outgoing, listOf(incoming), windowMillis = 10 * 60_000L))
    }

    @Test
    fun `same account is never matched as opposite`() {
        val incoming = tx(1, 1, TransactionType.TRANSFER_IN, 100000L, 1_000_000L)
        val outgoing = tx(2, 1, TransactionType.TRANSFER_OUT, 100400L, 1_000_000L + 60_000L)
        assertNull(TransferMatching.findOpposite(outgoing, listOf(incoming), windowMillis = 10 * 60_000L))
    }

    @Test
    fun `out-of-order arrival still matches`() {
        // entry (TRANSFER_IN) arrived first, then exit (TRANSFER_OUT) arrives
        val incoming = tx(1, 2, TransactionType.TRANSFER_IN, 100000L, 1_000_000L)
        val outgoing = tx(2, 1, TransactionType.TRANSFER_OUT, 100400L, 1_000_000L + 30_000L)
        val result = TransferMatching.findOpposite(outgoing, listOf(incoming), windowMillis = 10 * 60_000L)
        assertEquals(incoming.id, result?.id)
    }

    private fun tx(id: Long, accountId: Long, type: TransactionType, amount: Long, dateTime: Long) = Transaction(
        id = id,
        accountId = accountId,
        type = type,
        amount = amount,
        dateTime = dateTime,
        source = TransactionSource.NOTIFICATION
    )
}
