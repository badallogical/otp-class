package com.harekrishna.otpClasses.ui.login

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.harekrishna.otpClasses.core.utils.NetworkChecker
import com.harekrishna.otpClasses.data.models.User
import com.harekrishna.otpClasses.data.sources.repos.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

// ─────────────────────────────────────────────────────────────────────────────
// UI State models
// ─────────────────────────────────────────────────────────────────────────────

sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    data class Success(val userId: String) : LoginState
    data class Error(val message: String) : LoginState
}

enum class LoadingSource { NONE, GOOGLE, ANONYMOUS }

data class LoginUiState(
    val loginState: LoginState = LoginState.Idle,
    val loadingSource: LoadingSource = LoadingSource.NONE,
    val isOffline: Boolean = false
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    // Replace with your actual Web Client ID string resource reference:
    //   context.getString(R.string.default_web_client_id)
    private val webClientId: String
        get() = context.getString(
            context.resources.getIdentifier(
                "web_client_id", "string", context.packageName
            )
        )

    init {
        checkNetworkAndSession()
    }

    // ── Session & network ─────────────────────────────────────────────────────

    private fun checkNetworkAndSession() {
        _uiState.update { it.copy(isOffline = !NetworkChecker.isInternetAvailable(context)) }
        // If Firebase already has a valid session (Google or anonymous), skip login
        val current = authRepository.getCurrentUser()
        if (current != null) {
            _uiState.update { it.copy(loginState = LoginState.Success(current.id)) }
        }
    }

    fun retryConnectivity() {
        _uiState.update {
            it.copy(isOffline =  !NetworkChecker.isInternetAvailable(context), loginState = LoginState.Idle)
        }
    }

    fun clearError() {
        if (_uiState.value.loginState is LoginState.Error) {
            _uiState.update { it.copy(loginState = LoginState.Idle) }
        }
    }

    // ── Google sign-in via Credential Manager ─────────────────────────────────

    /**
     * Called from the Composable. Fetches a Google ID token via Credential Manager
     * and hands it straight to Firebase — no Activity result needed.
     */
    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(loginState = LoginState.Loading, loadingSource = LoadingSource.GOOGLE)
            }

            val idToken = fetchGoogleIdToken(context, webClientId)
            if (idToken != null) {
                authRepository.signInWithGoogle(idToken)
                    .onSuccess { user ->
                        _uiState.update {
                            it.copy(
                                loginState = LoginState.Success(user.id),
                                loadingSource = LoadingSource.NONE
                            )
                        }
                    }
                    .onFailure { e ->
                        e.message?.let { Log.d("Login", it) }
                        _uiState.update {
                            it.copy(
                                loginState = LoginState.Error(
                                    e.localizedMessage ?: "Google sign-in failed"
                                ),
                                loadingSource = LoadingSource.NONE
                            )
                        }
                    }
            } else {
                // Credential Manager dismissed or no accounts — fall back gracefully
                _uiState.update {
                    it.copy(
                        loginState = LoginState.Error("Sign-in cancelled. Try again or continue offline."),
                        loadingSource = LoadingSource.NONE
                    )
                }
            }
        }
    }

    // ── Anonymous / offline sign-in ───────────────────────────────────────────

    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(loginState = LoginState.Loading, loadingSource = LoadingSource.ANONYMOUS)
            }
            authRepository.signInAnonymously()
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            loginState = LoginState.Success(user.id),
                            loadingSource = LoadingSource.NONE
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            loginState = LoginState.Error(
                                e.localizedMessage ?: "Could not create guest session"
                            ),
                            loadingSource = LoadingSource.NONE
                        )
                    }
                }
        }
    }

    fun getCurrentUser() : User? {
        return authRepository.getCurrentUser()
    }

}

// ─────────────────────────────────────────────────────────────────────────────
// Credential Manager helper — top-level suspend fun, easy to unit-test
// ─────────────────────────────────────────────────────────────────────────────

suspend fun fetchGoogleIdToken(context: Context, webClientId: String): String? {
    return try {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)   // show all accounts, not just pre-authorised
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)             // silently selects if only one account
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(context, request)
        val credential = result.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } else {
            null
        }
    } catch (e: GetCredentialException) {
        // User cancelled, no accounts, or Play Services unavailable — all handled upstream
        null
    } catch (e: Exception) {
        null
    }
}