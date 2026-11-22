package com.gastoclaro.ui.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastoclaro.domain.model.Account
import com.gastoclaro.domain.model.Category
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.repository.AccountRepository
import com.gastoclaro.domain.repository.CategoryRepository
import com.gastoclaro.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionFormViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: Long? = savedStateHandle.get<Long>("transactionId")?.takeIf { it > 0 }

    val isEdit: Boolean get() = transactionId != null

    val accounts: StateFlow<List<Account>> = accountRepository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val existing: StateFlow<Transaction?> = transactionId?.let { id ->
        transactionRepository.observeTransaction(id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } ?: MutableStateFlow<Transaction?>(null).asStateFlow()

    fun save(transaction: Transaction) {
        viewModelScope.launch {
            if (isEdit) transactionRepository.update(transaction)
            else transactionRepository.insert(transaction)
        }
    }

    fun delete(transaction: Transaction) {
        viewModelScope.launch { transactionRepository.delete(transaction) }
    }
}
