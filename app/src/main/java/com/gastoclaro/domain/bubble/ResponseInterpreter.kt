package com.gastoclaro.domain.bubble

import com.gastoclaro.core.util.TextNormalizer
import com.gastoclaro.domain.model.Category
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResponseInterpreter @Inject constructor() {

    fun interpret(text: String, categories: List<Category>): Category? {
        val normalized = TextNormalizer.normalize(text)
        if (normalized.isEmpty()) return null

        val compact = TextNormalizer.normalizeCompact(text)

        categories.firstOrNull {
            TextNormalizer.normalizeCompact(it.name) == compact
        }?.let { return it }

        var best: Category? = null
        var bestScore = 0
        categories.forEach { category ->
            val keywords = keywordMap[category.name.lowercase()] ?: emptyList()
            val score = keywords.count { keyword ->
                val kw = TextNormalizer.normalize(keyword)
                kw.isNotEmpty() && normalized.contains(kw)
            }
            if (score > bestScore) {
                bestScore = score
                best = category
            }
        }
        return best
    }

    companion object {
        val keywordMap: Map<String, List<String>> = mapOf(
            "comida" to listOf(
                "almuerzo", "cena", "desayuno", "comida", "comer", "restaurante", "hamburguesa",
                "pizza", "cafe", "corrientazo", "domicilio", "pollo", "arepa", "empanada",
                "menu", "snack", "helado", "panaderia", "tamal", "asado"
            ),
            "transporte" to listOf(
                "transporte", "bus", "taxi", "uber", "didi", "indriver", "transmilenio",
                "metro", "gasolina", "combustible", "parqueadero", "peaje", "soat",
                "viaje", "pasaje", "estacion"
            ),
            "mercado" to listOf(
                "mercado", "supermercado", "compras", "viveres", "recao", "exito", "carulla",
                "olimpica", "d1", "ara", "jumbo", "alkosto", "abarrotes", "frutas", "verduras"
            ),
            "servicios" to listOf(
                "servicios", "agua", "luz", "energia", "internet", "gas", "factura", "recibo",
                "epm", "enel", "codensa", "acueducto", "telefono", "celular", "plan", "recarga"
            ),
            "salud" to listOf(
                "salud", "medicina", "medicamentos", "drogueria", "farmacia", "medico", "doctor",
                "eps", "hospital", "clinica", "cita medica", "examen", "ortodoncia", "odontologia",
                "gimnasio", "terapia", "vacuna"
            ),
            "entretenimiento" to listOf(
                "entretenimiento", "cine", "netflix", "concierto", "fiesta", "juego", "streaming",
                "teatro", "bar", "discoteca", "rumba", "parche", "videojuego", "suscripcion juego"
            ),
            "educacion" to listOf(
                "educacion", "curso", "universidad", "colegio", "matricula", "libro", "clase",
                "taller", "diplomado", "seminario", "utiles", "cuaderno", "fotocopia"
            ),
            "hogar" to listOf(
                "hogar", "casa", "arriendo", "arrendamiento", "reparacion", "muebles", "aseo",
                "electrodomestico", "plomero", "electricista", "decoracion", "administracion",
                "cuota de administracion"
            ),
            "suscripciones" to listOf(
                "suscripcion", "spotify", "disney", "hbo", "prime", "icloud", "google one",
                "membresia", "gympass", "youtube premium", "netflix"
            ),
            "otros" to listOf(
                "regalo", "donacion", "ropa", "zapatos", "electronico", "accesorio", "mascota",
                "impuesto", "multa", "comision"
            )
        )
    }
}
