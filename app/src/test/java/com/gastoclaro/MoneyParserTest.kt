package com.gastoclaro

import com.gastoclaro.core.util.MoneyParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyParserTest {

    @Test
    fun parsesAmountWithDotsAndDecimal() {
        assertEquals(123456789L, MoneyParser.parseAmountToken("1.234.567,89"))
    }

    @Test
    fun parsesAmountWithDotsNoDecimal() {
        assertEquals(4590000L, MoneyParser.parseAmountToken("45.900"))
    }

    @Test
    fun parsesSimpleDecimal() {
        assertEquals(2500050L, MoneyParser.parseAmountToken("25.000,50"))
    }

    @Test
    fun parsesPlainNumberAsCents() {
        assertEquals(2500000L, MoneyParser.parseAmountToken("25000"))
    }

    @Test
    fun parsesDollarPrefixed() {
        assertEquals(5000000L, MoneyParser.parseAmountToken("$50.000"))
    }

    @Test
    fun findsAmountInsideText() {
        assertEquals(4590000L, MoneyParser.parseColombianAmount("Compra por \$45.900 en EXITO"))
    }

    @Test
    fun returnsNullForNonAmount() {
        assertNull(MoneyParser.parseAmountToken("abc"))
    }
}
