package com.harekrishna.otpClasses.ui.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harekrishna.otpClasses.R
import com.harekrishna.otpClasses.data.models.UserEntity
import com.harekrishna.otpClasses.data.sources.repos.AuthRepository
import com.harekrishna.otpClasses.data.sources.repos.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.harekrishna.otpClasses.ui.login.fetchGoogleIdToken
import kotlinx.coroutines.flow.first

data class ProfileUiState(
    val user: UserEntity = UserEntity(),
    val isLoading: Boolean = false,
    val isSigningOut: Boolean = false,
    val isLinkingGoogle: Boolean = false,
    val isSavingProfile: Boolean = false,
    val error: String? = null,
    val signOutSuccess: Boolean = false,
    val googleLinkSuccess: Boolean = false,
    val profileUpdateSuccess: Boolean = false,
    val isProfileCompleted: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
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
                userProfileRepository.observeUser().collect { user ->
                    Log.d("login", user.toString())
                    _uiState.update { it.copy(user = user, isLoading = false) }
                }
            } catch (e: Exception) {
                Log.d("login", e.message ?: "User Entity is not recieved properly")
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

                        // Delete the guest user on sign 0ut
                        if( uiState.value.user.isGuest ) {
                            userProfileRepository.deleteGuestUser()
                        }

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

    fun linkWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLinkingGoogle = true) }
            try {
                val webClientId = context.getString(R.string.web_client_id)
                val token = fetchGoogleIdToken(context, webClientId)

                if (token != null) {
                    // Check if there's a live anonymous session right now
                    authRepository.linkAnonymousWithGoogle(token)
                        .onSuccess { user ->
                            userProfileRepository.updateGuestUser(user)
                            _uiState.update { it.copy(isLinkingGoogle = false, googleLinkSuccess = true) }
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
                userProfileRepository.updateProfileName(_uiState.value.user.id, trimmedName)
                userProfileRepository.updateProfilePhone(_uiState.value.user.id,trimmedPhone)
                val updatedUser = userProfileRepository.observeUser().first()
                _uiState.update {
                    it.copy(
                        user = updatedUser,
                        isSavingProfile = false,
                        profileUpdateSuccess = true
                    )
                }

                if( !(updatedUser.name.isNullOrBlank() || updatedUser.phone.isNullOrBlank()) ){
                    _uiState.update { it.copy( isProfileCompleted = true )}
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