package com.harekrishna.otpClasses.ui.dashboard

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harekrishna.otpClasses.core.utils.NetworkChecker
import com.harekrishna.otpClasses.data.models.UserEntity
import com.harekrishna.otpClasses.data.sources.repos.AuthRepository
import com.harekrishna.otpClasses.data.sources.repos.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppStartViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val userProfileRepo: UserProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppStartState())
    val uiState: StateFlow<AppStartState> = _uiState

    init {
        checkAppStartState()
    }

    private fun checkAppStartState() {
        viewModelScope.launch {

            val isConnected = NetworkChecker.isInternetAvailable(context)

            // User is authenticated in Firebase
            val isFirebaseAuthenticated = authRepository.getCurrentUser() != null

            var currentUser : UserEntity = UserEntity()
            var isProfileCompleted : Boolean = false
            if( isFirebaseAuthenticated ){



                currentUser = userProfileRepo.observeUser().first()
                isProfileCompleted = !( currentUser.name.isNullOrEmpty() || currentUser.phone.isNullOrEmpty())
            }



            val destination = when {
                isFirebaseAuthenticated && isProfileCompleted -> "dashboard"
                isFirebaseAuthenticated -> "profile" // Adjust if profile filling screen exists
                else -> "login"
            }

            Log.d("login", "Firebase Authenticated  : ${isFirebaseAuthenticated} )")

            Log.d("login", "Current user : ${currentUser?.name} + ${currentUser?.phone} Profile , ${isProfileCompleted}   destination ${destination}")

            _uiState.value = AppStartState(
                isReady = true,
                isConnected = isConnected,
                startDestination = destination
            )
        }
    }

    fun retry() = checkAppStartState()
}


data class AppStartState(
    val isReady: Boolean = false,
    val isConnected: Boolean = false,
    val startDestination: String = ""
)