package com.gastoclaro.domain.parser

import com.gastoclaro.domain.parser.ParsedTransaction
import com.gastoclaro.domain.parser.RawMessage
import com.gastoclaro.domain.parser.TransactionParser

abstract class AbstractBankParser : TransactionParser {

    protected abstract val packages: Set<String>
    protected abstract val senders: Set<String>

    final override fun matches(raw: RawMessage): Boolean {
        val pkg = raw.appPackage?.lowercase().orEmpty()
        val sender = raw.sender?.lowercase().orEmpty()
        val packageOk = packages.any { it.isNotEmpty() && pkg.contains(it) }
        val senderOk = senders.any { it.isNotEmpty() && sender.contains(it) }
        if (!packageOk && !senderOk) return false
        return ParseSupport.transactionKeywords.containsMatchIn(raw.text)
    }

    final override fun parse(raw: RawMessage): ParsedTransaction? {
        val type = ParseSupport.detectType(raw.text) ?: return null
        val amount = ParseSupport.amount(raw.text) ?: return null
        return ParsedTransaction(
            type = type,
            amount = amount,
            dateTime = ParseSupport.dateTime(raw.text, raw.receivedAt),
            merchant = ParseSupport.cleanMerchant(ParseSupport.extractMerchant(raw.text, type)),
            accountLast4 = ParseSupport.last4(raw.text),
            reference = ParseSupport.reference(raw.text),
            balanceAfter = ParseSupport.balance(raw.text)
        )
    }
}
