package com.gastoclaro.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastoclaro.domain.model.Account
import com.gastoclaro.domain.model.Category
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.TransactionType
import com.gastoclaro.domain.repository.AccountRepository
import com.gastoclaro.domain.repository.CategoryRepository
import com.gastoclaro.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    data class Filter(
        val query: String = "",
        val accountId: Long? = null,
        val categoryId: Long? = null,
        val type: TransactionType? = null,
        val start: Long? = null,
        val end: Long? = null
    ) {
        val isActive: Boolean get() = query.isNotBlank() || accountId != null || categoryId != null ||
            type != null || start != null || end != null
    }

    private val _filter = MutableStateFlow(Filter())
    val filter: StateFlow<Filter> = _filter.asStateFlow()

    private val _deleted = MutableStateFlow<Transaction?>(null)
    val deleted: StateFlow<Transaction?> = _deleted.asStateFlow()

    val transactions: StateFlow<List<Transaction>> = combine(
        transactionRepository.observeTransactions(),
        _filter
    ) { list, f -> applyFilter(list, f) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<Account>> = accountRepository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(query: String) = _filter.update { it.copy(query = query) }
    fun setAccount(accountId: Long?) = _filter.update { it.copy(accountId = accountId) }
    fun setCategory(categoryId: Long?) = _filter.update { it.copy(categoryId = categoryId) }
    fun setType(type: TransactionType?) = _filter.update { it.copy(type = type) }
    fun setDateRange(start: Long?, end: Long?) = _filter.update { it.copy(start = start, end = end) }
    fun clearFilters() { _filter.value = Filter() }

    fun delete(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.delete(transaction)
            _deleted.value = transaction
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            _deleted.value?.let { transactionRepository.insert(it) }
            _deleted.value = null
        }
    }

    private fun applyFilter(list: List<Transaction>, f: Filter): List<Transaction> = list.filter { tx ->
        val queryOk = f.query.isBlank() ||
            tx.merchant?.contains(f.query, ignoreCase = true) == true ||
            tx.note?.contains(f.query, ignoreCase = true) == true
        val accountOk = f.accountId == null || tx.accountId == f.accountId
        val categoryOk = f.categoryId == null || tx.categoryId == f.categoryId
        val typeOk = f.type == null || tx.type == f.type
        val startOk = f.start == null || tx.dateTime >= f.start
        val endOk = f.end == null || tx.dateTime < f.end
        queryOk && accountOk && categoryOk && typeOk && startOk && endOk
    }
}
