package com.gastoclaro.domain.usecase

import com.gastoclaro.domain.model.Transaction

sealed interface DetectionResult {
    data class Detected(val transaction: Transaction) : DetectionResult
    data object Duplicate : DetectionResult
    data object Unrecognized : DetectionResult
}
