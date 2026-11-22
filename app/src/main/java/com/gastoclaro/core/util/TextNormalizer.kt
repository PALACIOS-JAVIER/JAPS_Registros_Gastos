package com.gastoclaro.core.util

import java.text.Normalizer

object TextNormalizer {
    fun normalize(input: String): String = Normalizer.normalize(input, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()
        .trim()
        .replace(Regex("\\s+"), " ")

    fun normalizeCompact(input: String): String = normalize(input).replace(Regex("[^a-z0-9]"), "")
}
