package com.gastoclaro.ui.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastoclaro.domain.model.Account
import com.gastoclaro.domain.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountFormViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val accountId: Long? = savedStateHandle.get<Long>("accountId")?.takeIf { it > 0 }

    val isEdit: Boolean get() = accountId != null

    val existing: StateFlow<Account?> = accountId?.let { id ->
        accountRepository.observeAccount(id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } ?: MutableStateFlow<Account?>(null).asStateFlow()

    fun save(account: Account) {
        viewModelScope.launch { accountRepository.upsertAccount(account) }
    }

    fun delete(account: Account) {
        viewModelScope.launch { accountRepository.deleteAccount(account) }
    }
}
