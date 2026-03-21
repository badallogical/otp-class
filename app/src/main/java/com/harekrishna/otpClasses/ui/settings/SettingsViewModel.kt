package com.harekrishna.otpClasses.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harekrishna.otpClasses.domain.GetSettingsUseCase
import com.harekrishna.otpClasses.domain.ResetMessagesUseCase
import com.harekrishna.otpClasses.domain.SaveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase,
    private val resetMessages: ResetMessagesUseCase
) : ViewModel() {

    // ✅ Single source of truth (Flow → StateFlow)
    val uiState: StateFlow<SettingsUiState> = getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState(isLoading = true)
        )

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun save(uiState: SettingsUiState) {
        viewModelScope.launch {
            saveSettings(uiState)
            _uiEvent.emit("Settings saved successfully")
        }
    }

    fun reset() {
        viewModelScope.launch {
            resetMessages()
            _uiEvent.emit("Messages reset")
        }
    }

}
