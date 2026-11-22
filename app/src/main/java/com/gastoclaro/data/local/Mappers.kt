package com.gastoclaro.data.local

import com.gastoclaro.data.local.entity.AccountEntity
import com.gastoclaro.data.local.entity.BudgetEntity
import com.gastoclaro.data.local.entity.CategoryEntity
import com.gastoclaro.data.local.entity.CategoryRuleEntity
import com.gastoclaro.data.local.entity.KnownCounterpartyEntity
import com.gastoclaro.data.local.entity.PendingQuestionEntity
import com.gastoclaro.data.local.entity.TransactionEntity
import com.gastoclaro.data.local.entity.UnrecognizedMessageEntity
import com.gastoclaro.domain.model.Account
import com.gastoclaro.domain.model.Budget
import com.gastoclaro.domain.model.Category
import com.gastoclaro.domain.model.CategoryRule
import com.gastoclaro.domain.model.KnownCounterparty
import com.gastoclaro.domain.model.PendingQuestion
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.UnrecognizedMessage

fun AccountEntity.toDomain() = Account(
    id = id,
    name = name,
    bank = bank,
    type = type,
    last4Digits = last4Digits,
    phoneNumber = phoneNumber,
    holderName = holderName,
    initialBalance = initialBalance,
    color = color,
    icon = icon,
    isArchived = isArchived
)

fun Account.toEntity() = AccountEntity(
    id = id,
    name = name,
    bank = bank,
    type = type,
    last4Digits = last4Digits,
    phoneNumber = phoneNumber,
    holderName = holderName,
    initialBalance = initialBalance,
    color = color,
    icon = icon,
    isArchived = isArchived
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    accountId = accountId,
    type = type,
    amount = amount,
    dateTime = dateTime,
    merchant = merchant,
    categoryId = categoryId,
    note = note,
    source = source,
    rawTextHash = rawTextHash,
    transferGroupId = transferGroupId,
    linkedAccountId = linkedAccountId,
    status = status,
    reference = reference,
    accountLast4 = accountLast4,
    balanceAfter = balanceAfter,
    isTransferInternal = isTransferInternal,
    month = month
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    accountId = accountId,
    type = type,
    amount = amount,
    dateTime = dateTime,
    merchant = merchant,
    categoryId = categoryId,
    note = note,
    source = source,
    rawTextHash = rawTextHash,
    transferGroupId = transferGroupId,
    linkedAccountId = linkedAccountId,
    status = status,
    reference = reference,
    accountLast4 = accountLast4,
    balanceAfter = balanceAfter,
    isTransferInternal = isTransferInternal,
    month = month
)

fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = type,
    isDefault = isDefault,
    order = order
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    icon = icon,
    color = color,
    type = type,
    isDefault = isDefault,
    order = order
)

fun CategoryRuleEntity.toDomain() = CategoryRule(
    id = id,
    pattern = pattern,
    categoryId = categoryId,
    isLearned = isLearned,
    confidence = confidence,
    usageCount = usageCount
)

fun CategoryRule.toEntity() = CategoryRuleEntity(
    id = id,
    pattern = pattern,
    categoryId = categoryId,
    isLearned = isLearned,
    confidence = confidence,
    usageCount = usageCount
)

fun BudgetEntity.toDomain() = Budget(id = id, categoryId = categoryId, amount = amount)

fun Budget.toEntity() = BudgetEntity(id = id, categoryId = categoryId, amount = amount)

fun KnownCounterpartyEntity.toDomain() = KnownCounterparty(
    id = id,
    identifier = identifier,
    isMine = isMine,
    accountId = accountId
)

fun KnownCounterparty.toEntity() = KnownCounterpartyEntity(
    id = id,
    identifier = identifier,
    isMine = isMine,
    accountId = accountId
)

fun UnrecognizedMessageEntity.toDomain() = UnrecognizedMessage(
    id = id,
    text = text,
    source = source,
    appPackage = appPackage,
    sender = sender,
    receivedAt = receivedAt
)

fun UnrecognizedMessage.toEntity() = UnrecognizedMessageEntity(
    id = id,
    text = text,
    source = source,
    appPackage = appPackage,
    sender = sender,
    receivedAt = receivedAt
)

fun PendingQuestionEntity.toDomain() = PendingQuestion(
    id = id,
    transactionId = transactionId,
    createdAt = createdAt,
    questionText = questionText
)

fun PendingQuestion.toEntity() = PendingQuestionEntity(
    id = id,
    transactionId = transactionId,
    createdAt = createdAt,
    questionText = questionText
)
