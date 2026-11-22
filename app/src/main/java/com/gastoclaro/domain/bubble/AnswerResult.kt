package com.gastoclaro.domain.bubble

import com.gastoclaro.domain.model.Category

sealed interface AnswerResult {
    data class Categorized(val category: Category, val note: String) : AnswerResult
    data object NeedCategory : AnswerResult
    data object MarkedInternal : AnswerResult
    data object NotFound : AnswerResult
}
