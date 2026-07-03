package com.harekrishna.otpClasses.domain

import com.harekrishna.otpClasses.data.sources.repos.SettingsPreferencesRepository
import com.harekrishna.otpClasses.domain.GetUserProfileUseCase
import com.harekrishna.otpClasses.ui.settings.SettingsUiState
import javax.inject.Inject

// Save all settings
class SaveSettingsUseCase @Inject constructor(
    private val settingsPrefRepo : SettingsPreferencesRepository,
) {

    suspend operator fun invoke( state : SettingsUiState){
        settingsPrefRepo.saveThemeMode(state.themeMode)
    }
}