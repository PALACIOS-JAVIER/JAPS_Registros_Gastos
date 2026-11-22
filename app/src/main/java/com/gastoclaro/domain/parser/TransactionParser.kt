package com.gastoclaro.domain.parser

interface TransactionParser {
    val id: String

    fun matches(raw: RawMessage): Boolean

    fun parse(raw: RawMessage): ParsedTransaction?
}
