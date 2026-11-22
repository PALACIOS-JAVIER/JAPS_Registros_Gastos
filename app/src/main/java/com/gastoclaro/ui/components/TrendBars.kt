package com.gastoclaro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gastoclaro.domain.model.MonthlySummary

@Composable
fun TrendBars(
    data: List<MonthlySummary>,
    modifier: Modifier = Modifier,
    color: Color
) {
    val max = (data.maxOfOrNull { it.expense } ?: 1L).coerceAtLeast(1L)
    Canvas(modifier = modifier.fillMaxSize()) {
        if (data.isEmpty()) return@Canvas
        val gap = size.width / data.size
        val barWidth = gap * 0.55f
        data.forEachIndexed { index, item ->
            val height = (item.expense.toFloat() / max.toFloat()) * (size.height - 4.dp.toPx())
            val left = index * gap + (gap - barWidth) / 2f
            drawRoundRect(
                color = color,
                topLeft = Offset(left, size.height - height),
                size = Size(barWidth, height),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
        }
    }
}
