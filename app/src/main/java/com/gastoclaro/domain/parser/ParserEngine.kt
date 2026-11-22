package com.gastoclaro.domain.parser

import com.gastoclaro.domain.parser.ParsedResult
import com.gastoclaro.domain.parser.ParsedTransaction
import com.gastoclaro.domain.parser.RawMessage
import com.gastoclaro.domain.parser.TransactionParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParserEngine @Inject constructor(
    private val parsers: Set<@JvmSuppressWildcards TransactionParser>
) {
    fun parse(raw: RawMessage): ParsedResult? {
        for (parser in parsers) {
            if (parser.matches(raw)) {
                val parsed = parser.parse(raw) ?: continue
                return ParsedResult(parserId = parser.id, parsed = parsed)
            }
        }
        return null
    }
}
