package com.harekrishna.otpClasses.domain

import android.util.Log
import com.harekrishna.otpClasses.data.sources.repos.MessagePreferencesRepository
import com.harekrishna.otpClasses.data.sources.repos.UserPreferencesRepository
import com.harekrishna.otpClasses.ui.dashboard.SettingsUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

// Save all settings
class SaveSettingsUseCase @Inject constructor(
    private val userPrefRepo : UserPreferencesRepository,
    private val messagePrefRepo : MessagePreferencesRepository
) {

    suspend operator fun invoke( state : SettingsUiState){
        userPrefRepo.saveUserData(state.name, state.phone)
    }
}