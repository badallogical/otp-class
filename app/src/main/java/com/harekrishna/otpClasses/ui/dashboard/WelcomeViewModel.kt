package com.harekrishna.otpClasses.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harekrishna.otpClasses.domain.InitializeDefaultMessagesUseCase
import com.harekrishna.otpClasses.domain.SaveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val saveUserProfile: SaveUserProfileUseCase,
    private val initializeDefaultMessages: InitializeDefaultMessagesUseCase
) : ViewModel() {
    fun proceed( name: String, phone: String, onDone: () -> Unit ){
        viewModelScope.launch {
            saveUserProfile(name, phone)
            initializeDefaultMessages()
            onDone()
        }
    }
}