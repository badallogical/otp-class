package com.harekrishna.otpClasses.domain

import com.harekrishna.otpClasses.data.sources.repos.MessagePreferencesRepository
import com.harekrishna.otpClasses.data.sources.repos.SettingsPreferencesRepository
import com.harekrishna.otpClasses.data.sources.repos.UserPreferencesRepository
import com.harekrishna.otpClasses.ui.settings.SettingsUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val userPrefRepo: UserPreferencesRepository,
    private val messagePrefRepo: MessagePreferencesRepository,
    private val settingsPrefRepo: SettingsPreferencesRepository,
) {

    operator fun invoke(): Flow<SettingsUiState> {
        return combine(
            userPrefRepo.getUserData(),
            settingsPrefRepo.themeModeFlow
        ) { userData, themeMode ->

            SettingsUiState(
                name = userData.first ?: "",
                phone = userData.second ?: "",
                themeMode = themeMode
            )
        }
    }
}