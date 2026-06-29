package com.harekrishna.otpClasses.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harekrishna.otpClasses.R
import com.harekrishna.otpClasses.data.models.User
import com.harekrishna.otpClasses.data.sources.repos.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.harekrishna.otpClasses.ui.login.fetchGoogleIdToken

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isSigningOut: Boolean = false,
    val isLinkingGoogle: Boolean = false,
    val isSavingProfile: Boolean = false,
    val error: String? = null,
    val signOutSuccess: Boolean = false,
    val googleLinkSuccess: Boolean = false,
    val profileUpdateSuccess: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = authRepository.getCurrentUser()
                _uiState.update { it.copy(user = user, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load user profile",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningOut = true) }
            try {
                authRepository.signOut(context)
                    .onSuccess {
                        _uiState.update { it.copy(isSigningOut = false, signOutSuccess = true) }
                    }
                    .onFailure {
                        _uiState.update {
                            it.copy(
                                error = it.error ?: "Sign out failed",
                                isSigningOut = false
                            )
                        }
                    }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Sign out failed",
                        isSigningOut = false
                    )
                }
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLinkingGoogle = true) }
            try {
                val webClientId = context.getString(R.string.web_client_id)
                val token = fetchGoogleIdToken(context, webClientId)

                if (token != null) {
                    // Check if there's a live anonymous session right now
                    val isCurrentlyAnonymous = authRepository.getCurrentUser()?.isGuest == true

                    val result = if (isCurrentlyAnonymous) {
                        authRepository.linkAnonymousWithGoogle(token)
                    } else {
                        authRepository.signInWithGoogle(token)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Google sign-in failed",
                        isLinkingGoogle = false
                    )
                }
            }
        }
    }

    fun updateProfile(name: String, phone: String) {
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()
        if (trimmedName.isBlank()) {
            _uiState.update { it.copy(error = "Name cannot be empty") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProfile = true) }
            try {
                authRepository.updateUserProfile(name = trimmedName, phone = trimmedPhone)
                val updatedUser = authRepository.getCurrentUser()
                _uiState.update {
                    it.copy(
                        user = updatedUser,
                        isSavingProfile = false,
                        profileUpdateSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to update profile",
                        isSavingProfile = false
                    )
                }
            }
        }
    }

    fun onProfileUpdateHandled() {
        _uiState.update { it.copy(profileUpdateSuccess = false) }
    }



    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onSignOutHandled() {
        _uiState.update { it.copy(signOutSuccess = false) }
    }
}