package com.harekrishna.otpClasses.domain

import com.harekrishna.otpClasses.data.sources.repos.SettingsPreferencesRepository
import com.harekrishna.otpClasses.data.sources.repos.UserPreferencesRepository
import com.harekrishna.otpClasses.ui.settings.SettingsUiState
import javax.inject.Inject

// Save all settings
class SaveSettingsUseCase @Inject constructor(
    private val userPrefRepo : UserPreferencesRepository,
    private val settingsPrefRepo : SettingsPreferencesRepository,
) {

    suspend operator fun invoke( state : SettingsUiState){
        userPrefRepo.saveUserData(state.name, state.phone)
        settingsPrefRepo.saveThemeMode(state.themeMode)
    }
}