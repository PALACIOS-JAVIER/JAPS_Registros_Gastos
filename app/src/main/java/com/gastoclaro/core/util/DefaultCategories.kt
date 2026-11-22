package com.gastoclaro.core.util

import com.gastoclaro.domain.model.Category
import com.gastoclaro.domain.model.CategoryType

object DefaultCategories {
    fun expense(): List<Category> = listOf(
        Category(name = "Comida", icon = "restaurant", color = 0xFFFFB020.toInt(), type = CategoryType.EXPENSE, isDefault = true, order = 1),
        Category(name = "Transporte", icon = "directions_bus", color = 0xFF4C8DFF.toInt(), type = CategoryType.EXPENSE, isDefault = true, order = 2),
        Category(name = "Mercado", icon = "shopping_cart", color = 0xFF2EC4B6.toInt(), type = CategoryType.EXPENSE, isDefault = true, order = 3),
        Category(name = "Servicios", icon = "receipt_long", color = 0xFF9C6ADE.toInt(), type = CategoryType.EXPENSE, isDefault = true, order = 4),
        Category(name = "Salud", icon = "medical_services", color = 0xFFF05252.toInt(), type = CategoryType.EXPENSE, isDefault = true, order = 5),
        Category(name = "Entretenimiento", icon = "movie", color = 0xFFFF5C8A.toInt(), type = CategoryType.EXPENSE, isDefault = true, order = 6),
        Category(name = "Educación", icon = "school", color = 0xFF3B82F6.toInt(), type = CategoryType.EXPENSE, isDefault = true, order = 7),
        Category(name = "Hogar", icon = "home", color = 0xFF8B5CF6.toInt(), type = CategoryType.EXPENSE, isDefault = true, order = 8),
        Category(name = "Suscripciones", icon = "subscriptions", color = 0xFFF59E0B.toInt(), type = CategoryType.EXPENSE, isDefault = true, order = 9),
        Category(name = "Otros", icon = "category", color = 0xFF64748B.toInt(), type = CategoryType.EXPENSE, isDefault = true, order = 10)
    )

    fun income(): List<Category> = listOf(
        Category(name = "Salario", icon = "payments", color = 0xFF34D399.toInt(), type = CategoryType.INCOME, isDefault = true, order = 1),
        Category(name = "Otros ingresos", icon = "trending_up", color = 0xFF22C55E.toInt(), type = CategoryType.INCOME, isDefault = true, order = 2)
    )

    fun all(): List<Category> = expense() + income()
}
