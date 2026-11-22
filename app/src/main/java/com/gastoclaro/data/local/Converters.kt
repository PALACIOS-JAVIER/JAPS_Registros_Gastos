package com.gastoclaro.data.local

import androidx.room.TypeConverter
import com.gastoclaro.domain.model.AccountType
import com.gastoclaro.domain.model.BubbleMode
import com.gastoclaro.domain.model.CategoryType
import com.gastoclaro.domain.model.TransactionSource
import com.gastoclaro.domain.model.TransactionStatus
import com.gastoclaro.domain.model.TransactionType

class Converters {

    @TypeConverter
    fun accountTypeToString(value: AccountType): String = value.name

    @TypeConverter
    fun stringToAccountType(value: String): AccountType = AccountType.valueOf(value)

    @TypeConverter
    fun categoryTypeToString(value: CategoryType): String = value.name

    @TypeConverter
    fun stringToCategoryType(value: String): CategoryType = CategoryType.valueOf(value)

    @TypeConverter
    fun transactionSourceToString(value: TransactionSource): String = value.name

    @TypeConverter
    fun stringToTransactionSource(value: String): TransactionSource = TransactionSource.valueOf(value)

    @TypeConverter
    fun transactionStatusToString(value: TransactionStatus): String = value.name

    @TypeConverter
    fun stringToTransactionStatus(value: String): TransactionStatus = TransactionStatus.valueOf(value)

    @TypeConverter
    fun transactionTypeToString(value: TransactionType): String = value.name

    @TypeConverter
    fun stringToTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun bubbleModeToString(value: BubbleMode): String = value.name

    @TypeConverter
    fun stringToBubbleMode(value: String): BubbleMode = BubbleMode.valueOf(value)
}
