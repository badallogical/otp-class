package com.harekrishna.otpClasses.domain

import android.util.Log
import com.harekrishna.otpClasses.data.sources.repos.MessagePreferencesRepository
import com.harekrishna.otpClasses.data.sources.repos.UserPreferencesRepository
import com.harekrishna.otpClasses.ui.dashboard.SettingsUiState
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val userPrefRepo : UserPreferencesRepository,
    private val messagePrefRepo : MessagePreferencesRepository
) {

    suspend operator fun invoke(): SettingsUiState {
        val userData = userPrefRepo.getUserData().first()
        Log.d("Message Get", messagePrefRepo.welcomeMessageFlow.first())
        Log.d("Message Get", messagePrefRepo.thanksMessageFlow.first())

        return SettingsUiState(
            name = userData.first ?: "",
            phone = userData.second ?: "",
        )
    }
}