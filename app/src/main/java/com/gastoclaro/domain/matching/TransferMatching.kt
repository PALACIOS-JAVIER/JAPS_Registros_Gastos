package com.gastoclaro.domain.matching

import com.gastoclaro.core.util.TextNormalizer
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.TransactionType
import kotlin.math.abs

object TransferMatching {

    const val COMMISSION_TOLERANCE = 0.02

    fun namesMatch(a: String, b: String): Boolean {
        val na = TextNormalizer.normalizeCompact(a)
        val nb = TextNormalizer.normalizeCompact(b)
        if (na.isEmpty() || nb.isEmpty()) return false
        return na == nb || na.contains(nb) || nb.contains(na)
    }

    fun isOppositeType(a: TransactionType, b: TransactionType): Boolean =
        (a == TransactionType.TRANSFER_IN && b == TransactionType.TRANSFER_OUT) ||
            (a == TransactionType.TRANSFER_OUT && b == TransactionType.TRANSFER_IN)

    fun amountsMatch(a: Long, b: Long, tolerance: Double = COMMISSION_TOLERANCE): Boolean {
        if (a <= 0L || b <= 0L) return false
        val smaller = minOf(a, b)
        val larger = maxOf(a, b)
        return larger.toDouble() <= smaller.toDouble() * (1.0 + tolerance)
    }

    fun withinWindow(a: Long, b: Long, windowMillis: Long): Boolean =
        abs(a - b) <= windowMillis

    fun findOpposite(
        newTransaction: Transaction,
        candidates: List<Transaction>,
        windowMillis: Long,
        tolerance: Double = COMMISSION_TOLERANCE
    ): Transaction? = candidates
        .filter { isOppositeType(newTransaction.type, it.type) }
        .filter { it.id != newTransaction.id }
        .filter { it.accountId != newTransaction.accountId }
        .filter { withinWindow(newTransaction.dateTime, it.dateTime, windowMillis) }
        .filter { amountsMatch(newTransaction.amount, it.amount, tolerance) }
        .minByOrNull { abs(it.dateTime - newTransaction.dateTime) }
}
