package com.gastoclaro.data.backup

import com.gastoclaro.core.security.CryptoManager
import com.gastoclaro.data.local.GastoClaroDatabase
import com.gastoclaro.data.local.entity.AccountEntity
import com.gastoclaro.data.local.entity.BudgetEntity
import com.gastoclaro.data.local.entity.CategoryEntity
import com.gastoclaro.data.local.entity.CategoryRuleEntity
import com.gastoclaro.data.local.entity.KnownCounterpartyEntity
import com.gastoclaro.data.local.entity.TransactionEntity
import com.gastoclaro.domain.model.AccountType
import com.gastoclaro.domain.model.CategoryType
import com.gastoclaro.domain.model.TransactionSource
import com.gastoclaro.domain.model.TransactionStatus
import com.gastoclaro.domain.model.TransactionType
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val db: GastoClaroDatabase,
    private val cryptoManager: CryptoManager
) {

    suspend fun exportEncrypted(): String = withContext(Dispatchers.IO) {
        val json = buildJson()
        cryptoManager.encrypt(json)
    }

    suspend fun importEncrypted(encoded: String) = withContext(Dispatchers.IO) {
        val json = cryptoManager.decrypt(encoded)
        restore(json)
    }

    private suspend fun buildJson(): String {
        val accountDao = db.accountDao()
        val transactionDao = db.transactionDao()
        val categoryDao = db.categoryDao()
        val ruleDao = db.categoryRuleDao()
        val budgetDao = db.budgetDao()
        val counterpartyDao = db.knownCounterpartyDao()

        val root = JSONObject()
        root.put("accounts", JSONArray().apply {
            accountDao.getAllActive().forEach { put(accountToJson(it)) }
        })
        root.put("categories", JSONArray().apply {
            categoryDao.getAll().forEach { put(categoryToJson(it)) }
        })
        root.put("transactions", JSONArray().apply {
            transactionDao.getAll().forEach { put(transactionToJson(it)) }
        })
        root.put("rules", JSONArray().apply {
            ruleDao.getAll().forEach { put(ruleToJson(it)) }
        })
        root.put("budgets", JSONArray().apply {
            budgetDao.getAll().forEach { put(budgetToJson(it)) }
        })
        root.put("counterparties", JSONArray().apply {
            counterpartyDao.getAll().forEach { put(counterpartyToJson(it)) }
        })
        return root.toString()
    }

    private suspend fun restore(json: String) {
        val root = JSONObject(json)
        val accountDao = db.accountDao()
        val transactionDao = db.transactionDao()
        val categoryDao = db.categoryDao()
        val ruleDao = db.categoryRuleDao()
        val budgetDao = db.budgetDao()
        val counterpartyDao = db.knownCounterpartyDao()

        db.withTransaction {
            accountDao.clearAll()
            categoryDao.clearAll()
            transactionDao.clearAll()
            ruleDao.clearAll()
            budgetDao.clearAll()
            counterpartyDao.clearAll()

            root.optJSONArray("accounts")?.let { arr ->
                for (i in 0 until arr.length()) accountDao.upsert(accountFromJson(arr.getJSONObject(i)))
            }
            root.optJSONArray("categories")?.let { arr ->
                for (i in 0 until arr.length()) categoryDao.insert(categoryFromJson(arr.getJSONObject(i)))
            }
            root.optJSONArray("transactions")?.let { arr ->
                for (i in 0 until arr.length()) transactionDao.insert(transactionFromJson(arr.getJSONObject(i)))
            }
            root.optJSONArray("rules")?.let { arr ->
                for (i in 0 until arr.length()) ruleDao.upsert(ruleFromJson(arr.getJSONObject(i)))
            }
            root.optJSONArray("budgets")?.let { arr ->
                for (i in 0 until arr.length()) budgetDao.upsert(budgetFromJson(arr.getJSONObject(i)))
            }
            root.optJSONArray("counterparties")?.let { arr ->
                for (i in 0 until arr.length()) counterpartyDao.upsert(counterpartyFromJson(arr.getJSONObject(i)))
            }
        }
    }

    private fun accountToJson(e: AccountEntity): JSONObject = JSONObject().apply {
        put("id", e.id); put("name", e.name); put("bank", e.bank)
        put("type", e.type.name)
        e.last4Digits?.let { put("last4", it) }
        e.phoneNumber?.let { put("phone", it) }
        e.holderName?.let { put("holder", it) }
        put("initialBalance", e.initialBalance); put("color", e.color)
        put("icon", e.icon); put("isArchived", e.isArchived)
    }

    private fun accountFromJson(o: JSONObject): AccountEntity = AccountEntity(
        id = o.getLong("id"), name = o.getString("name"), bank = o.optString("bank"),
        type = AccountType.valueOf(o.getString("type")),
        last4Digits = o.nullableString("last4"),
        phoneNumber = o.nullableString("phone"),
        holderName = o.nullableString("holder"),
        initialBalance = o.getLong("initialBalance"), color = o.getInt("color"),
        icon = o.optString("icon"), isArchived = o.optBoolean("isArchived")
    )

    private fun categoryToJson(e: CategoryEntity): JSONObject = JSONObject().apply {
        put("id", e.id); put("name", e.name); put("icon", e.icon)
        put("color", e.color); put("type", e.type.name)
        put("isDefault", e.isDefault); put("order", e.order)
    }

    private fun categoryFromJson(o: JSONObject): CategoryEntity = CategoryEntity(
        id = o.getLong("id"), name = o.getString("name"), icon = o.optString("icon"),
        color = o.getInt("color"), type = CategoryType.valueOf(o.getString("type")),
        isDefault = o.optBoolean("isDefault"), order = o.optInt("order")
    )

    private fun transactionToJson(e: TransactionEntity): JSONObject = JSONObject().apply {
        put("id", e.id); put("accountId", e.accountId); put("type", e.type.name)
        put("amount", e.amount); put("dateTime", e.dateTime)
        e.merchant?.let { put("merchant", it) }
        e.categoryId?.let { put("categoryId", it) }
        e.note?.let { put("note", it) }
        put("source", e.source.name); put("rawTextHash", e.rawTextHash)
        e.transferGroupId?.let { put("transferGroupId", it) }
        e.linkedAccountId?.let { put("linkedAccountId", it) }
        put("status", e.status.name)
        e.reference?.let { put("reference", it) }
        e.accountLast4?.let { put("accountLast4", it) }
        e.balanceAfter?.let { put("balanceAfter", it) }
        put("isTransferInternal", e.isTransferInternal); put("month", e.month)
    }

    private fun transactionFromJson(o: JSONObject): TransactionEntity = TransactionEntity(
        id = o.getLong("id"), accountId = o.getLong("accountId"),
        type = TransactionType.valueOf(o.getString("type")),
        amount = o.getLong("amount"), dateTime = o.getLong("dateTime"),
        merchant = o.nullableString("merchant"), categoryId = o.nullableLong("categoryId"),
        note = o.nullableString("note"), source = TransactionSource.valueOf(o.getString("source")),
        rawTextHash = o.optString("rawTextHash"),
        transferGroupId = o.nullableString("transferGroupId"),
        linkedAccountId = o.nullableLong("linkedAccountId"),
        status = TransactionStatus.valueOf(o.getString("status")),
        reference = o.nullableString("reference"),
        accountLast4 = o.nullableString("accountLast4"),
        balanceAfter = o.nullableLong("balanceAfter"),
        isTransferInternal = o.optBoolean("isTransferInternal"), month = o.optString("month")
    )

    private fun ruleToJson(e: CategoryRuleEntity): JSONObject = JSONObject().apply {
        put("id", e.id); put("pattern", e.pattern); put("categoryId", e.categoryId)
        put("isLearned", e.isLearned); put("confidence", e.confidence); put("usageCount", e.usageCount)
    }

    private fun ruleFromJson(o: JSONObject): CategoryRuleEntity = CategoryRuleEntity(
        id = o.getLong("id"), pattern = o.getString("pattern"), categoryId = o.getLong("categoryId"),
        isLearned = o.optBoolean("isLearned"), confidence = o.optInt("confidence"),
        usageCount = o.optInt("usageCount")
    )

    private fun budgetToJson(e: BudgetEntity): JSONObject = JSONObject().apply {
        put("id", e.id); put("categoryId", e.categoryId); put("amount", e.amount)
    }

    private fun budgetFromJson(o: JSONObject): BudgetEntity = BudgetEntity(
        id = o.getLong("id"), categoryId = o.getLong("categoryId"), amount = o.getLong("amount")
    )

    private fun counterpartyToJson(e: KnownCounterpartyEntity): JSONObject = JSONObject().apply {
        put("id", e.id); put("identifier", e.identifier); put("isMine", e.isMine)
        e.accountId?.let { put("accountId", it) }
    }

    private fun counterpartyFromJson(o: JSONObject): KnownCounterpartyEntity = KnownCounterpartyEntity(
        id = o.getLong("id"), identifier = o.getString("identifier"), isMine = o.getBoolean("isMine"),
        accountId = o.nullableLong("accountId")
    )
}

private fun JSONObject.nullableString(key: String): String? =
    if (isNull(key) || !has(key)) null else getString(key)

private fun JSONObject.nullableLong(key: String): Long? =
    if (isNull(key) || !has(key)) null else getLong(key)
