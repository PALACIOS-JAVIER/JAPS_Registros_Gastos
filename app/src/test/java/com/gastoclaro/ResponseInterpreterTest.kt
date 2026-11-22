package com.gastoclaro

import com.gastoclaro.core.util.DefaultCategories
import com.gastoclaro.domain.bubble.ResponseInterpreter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResponseInterpreterTest {

    private val interpreter = ResponseInterpreter()
    private val categories = DefaultCategories.expense()

    @Test
    fun `maps lunch to comida`() {
        assertEquals("Comida", interpreter.interpret("almuerzo", categories)?.name)
    }

    @Test
    fun `is case insensitive`() {
        assertEquals("Comida", interpreter.interpret("ALMUERZO", categories)?.name)
    }

    @Test
    fun `handles accents`() {
        assertEquals("Comida", interpreter.interpret("café", categories)?.name)
    }

    @Test
    fun `maps colombian synonyms`() {
        assertEquals("Comida", interpreter.interpret("corrientazo", categories)?.name)
        assertEquals("Transporte", interpreter.interpret("uber", categories)?.name)
        assertEquals("Salud", interpreter.interpret("droguería", categories)?.name)
    }

    @Test
    fun `maps services`() {
        assertEquals("Servicios", interpreter.interpret("recibo de la luz", categories)?.name)
    }

    @Test
    fun `maps housing`() {
        assertEquals("Hogar", interpreter.interpret("arriendo", categories)?.name)
    }

    @Test
    fun `maps subscriptions`() {
        assertEquals("Suscripciones", interpreter.interpret("spotify", categories)?.name)
    }

    @Test
    fun `returns null for unknown text`() {
        assertNull(interpreter.interpret("xyz123", categories))
    }
}
