package com.gastoclaro.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastoclaro.data.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {

    fun export(onResult: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { backupManager.exportEncrypted() }
                .onSuccess { onResult(it) }
                .onFailure { onResult("") }
        }
    }

    fun import(encoded: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            runCatching { backupManager.importEncrypted(encoded) }
                .onSuccess { onResult(true) }
                .onFailure { onResult(false) }
        }
    }
}
