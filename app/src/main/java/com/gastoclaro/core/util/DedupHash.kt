package com.gastoclaro.core.util

import java.security.MessageDigest

object DedupHash {
    fun compute(amount: Long, dateTime: Long, reference: String?, account: String?): String {
        val minuteBucket = dateTime / 60_000L
        val raw = "$amount|$minuteBucket|${reference.orEmpty()}|${account.orEmpty()}"
        return sha256(raw)
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
