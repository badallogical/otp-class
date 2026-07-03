package com.harekrishna.otpClasses.domain

import com.harekrishna.otpClasses.data.sources.repos.SettingsPreferencesRepository
import com.harekrishna.otpClasses.ui.settings.SettingsUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val settingsPrefRepo: SettingsPreferencesRepository,
) {

    operator fun invoke(): Flow<SettingsUiState> =
        settingsPrefRepo.themeModeFlow.map { themeMode ->
            SettingsUiState(
                themeMode = themeMode
            )
        }
}