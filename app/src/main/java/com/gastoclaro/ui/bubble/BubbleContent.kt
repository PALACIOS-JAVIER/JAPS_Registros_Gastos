package com.gastoclaro.ui.bubble

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.gastoclaro.domain.model.Category
import com.gastoclaro.domain.model.CategoryType
import com.gastoclaro.domain.model.PendingQuestion
import com.gastoclaro.ui.components.IconMapper
import com.gastoclaro.ui.theme.IncomeGreen
import com.gastoclaro.ui.theme.TransferViolet

@Composable
fun BubbleContent(
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    questions: List<PendingQuestion>,
    categories: List<Category>,
    onDragBy: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    onAnswer: (PendingQuestion, String) -> Unit,
    onMarkInternal: (PendingQuestion) -> Unit,
    onPickCategory: (PendingQuestion, Long) -> Unit,
    onDismiss: () -> Unit
) {
    val current = questions.firstOrNull()
    val expenseCategories = categories.filter { it.type == CategoryType.EXPENSE }

    if (!expanded) {
        BubbleBall(
            count = questions.size,
            onTap = { onExpandChange(true) },
            onDragBy = onDragBy,
            onDragEnd = onDragEnd
        )
    } else {
        ExpandedChat(
            question = current,
            categories = expenseCategories,
            onAnswer = onAnswer,
            onMarkInternal = onMarkInternal,
            onPickCategory = onPickCategory,
            onCollapse = { onExpandChange(false) },
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun BubbleBall(
    count: Int,
    onTap: () -> Unit,
    onDragBy: (Float, Float) -> Unit,
    onDragEnd: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .pointerInput(Unit) { detectTapGestures(onTap = { onTap() }) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, amount ->
                        change.consume()
                        onDragBy(amount.x, amount.y)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 0) "$count" else "G",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(TransferViolet),
                contentAlignment = Alignment.Center
            ) {
                Text("$count", style = MaterialTheme.typography.labelMedium, color = Color.White)
            }
        }
    }
}

@Composable
private fun ExpandedChat(
    question: PendingQuestion?,
    categories: List<Category>,
    onAnswer: (PendingQuestion, String) -> Unit,
    onMarkInternal: (PendingQuestion) -> Unit,
    onPickCategory: (PendingQuestion, Long) -> Unit,
    onCollapse: () -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var answered by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.width(300.dp).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (question == null) {
                Text("No hay preguntas pendientes", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onDismiss) { Text("Cerrar") }
                return@Column
            }

            Text(question.questionText, style = MaterialTheme.typography.bodyMedium)

            if (!answered) {
                categories.take(4).forEach { category ->
                    CategoryChip(category, onClick = { onPickCategory(question, category.id) })
                }
                TextButton(onClick = { onMarkInternal(question) }) {
                    Text("Es una cuenta mía", color = TransferViolet)
                }
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Ej: almuerzo con amigos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onCollapse) { Text("Ocultar") }
                    TextButton(onClick = {
                        if (input.isNotBlank()) {
                            onAnswer(question, input.trim())
                            answered = true
                        }
                    }) { Text("Enviar") }
                }
            } else {
                Text(
                    text = "Listo, guardado.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = IncomeGreen
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { answered = false; input = "" }) { Text("Cambiar") }
                    TextButton(onClick = onCollapse) { Text("Cerrar") }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(category: Category, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = IconMapper.icon(category.icon),
            contentDescription = null,
            tint = Color(category.color),
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(category.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}
