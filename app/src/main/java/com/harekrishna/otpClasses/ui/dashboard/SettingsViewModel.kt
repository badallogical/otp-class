package com.harekrishna.otpClasses.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harekrishna.otpClasses.domain.GetSettingsUseCase
import com.harekrishna.otpClasses.domain.InitializeDefaultMessagesUseCase
import com.harekrishna.otpClasses.domain.ResetMessagesUseCase
import com.harekrishna.otpClasses.domain.SaveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase,
    private val resetMessages: ResetMessagesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            load()
        }
    }

    private suspend fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        val settings = getSettings()
        _uiState.value = settings.copy(isLoading = false)
    }

    fun save() {
        viewModelScope.launch {
            saveSettings(_uiState.value)
            _uiEvent.emit("Settings saved successfully")
        }
    }

    fun reset() {
        viewModelScope.launch {
            resetMessages()
            load()
        }
    }

    fun updateName(v: String) { _uiState.update { it.copy(name = v) } }
    fun updatePhone(v: String) { _uiState.update { it.copy(phone = v) } }
    fun updateWelcomeMessage(v: String) { _uiState.update { it.copy(welcomeMessage = v) } }
    fun updateThanksMessage(v: String) { _uiState.update { it.copy(thanksMessage = v) } }
    fun updateNotifications(v: Boolean) { _uiState.update { it.copy(notificationsEnabled = v) } }
}
