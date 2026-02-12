package com.harekrishna.otpClasses.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harekrishna.otpClasses.core.utils.NetworkChecker
import com.harekrishna.otpClasses.data.sources.repos.UserPreferencesRepository
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
    private val userPrefRepo: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppStartState())
    val uiState: StateFlow<AppStartState> = _uiState

    init {
        checkAppStartState()
    }

    private fun checkAppStartState() {
        viewModelScope.launch {

            val isConnected = NetworkChecker.isInternetAvailable(context)

            val isRegistered = userPrefRepo.getUserData()
                .first()
                .let { (name, phone) ->
                    !name.isNullOrEmpty() && !phone.isNullOrEmpty()
                }

            _uiState.value = AppStartState(
                isReady = true,
                isConnected = isConnected,
                startDestination = if (isRegistered) "dashboard" else "welcome"
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