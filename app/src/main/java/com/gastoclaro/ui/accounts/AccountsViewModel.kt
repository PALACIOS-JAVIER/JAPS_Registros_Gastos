package com.gastoclaro.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastoclaro.domain.model.Account
import com.gastoclaro.domain.model.AccountBalance
import com.gastoclaro.domain.repository.AccountRepository
import com.gastoclaro.domain.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    analyticsRepository: AnalyticsRepository
) : ViewModel() {

    val accounts: StateFlow<List<Account>> = accountRepository.observeAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val balances: StateFlow<List<AccountBalance>> = analyticsRepository.observeAccountBalances()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun upsertAccount(account: Account) {
        viewModelScope.launch { accountRepository.upsertAccount(account) }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch { accountRepository.deleteAccount(account) }
    }
}
