package com.harekrishna.otpClasses.ui.login

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harekrishna.otpClasses.ui.theme.Otp_class_appTheme
import com.harekrishna.otpClasses.ui.theme.ThemeMode
import com.harekrishna.otpClasses.R
// ─────────────────────────────────────────────────────────────────────────────
// Entry point — wires ViewModel, handles navigation
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    onLoginSuccess: (route: String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loginState) {
        if (state.loginState is LoginState.Success) {
            val success = state.loginState as LoginState.Success
            onLoginSuccess(success.route)
        }
    }

    LoginScreenContent(
        state = state,
        onGoogleSignIn = viewModel::signInWithGoogle,
        onGuestSignIn = viewModel::signInAnonymously,
        onRetry = viewModel::retryConnectivity,
        onClearError = viewModel::clearError
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Pure UI — no ViewModel, easy to preview
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LoginScreenContent(
    state: LoginUiState,
    onGoogleSignIn: () -> Unit,
    onGuestSignIn: () -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val isLoading = state.loginState is LoginState.Loading
    val errorMessage = (state.loginState as? LoginState.Error)?.message

    // Stagger entrance
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Warm saffron wash behind the header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.52f)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to colors.primary.copy(alpha = 0.10f),
                            0.7f to colors.primary.copy(alpha = 0.03f),
                            1.0f to Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(40.dp))

            // ── Offline banner ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = state.isOffline,
                enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { -32 },
                exit = fadeOut(tween(250))
            ) {
                OfflineBanner(onRetry = onRetry)
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(if (state.isOffline) 12.dp else 0.dp))

            // ── LOGO mark ───────────────────────────────────────────────────
            AnimatedVisibility(
                visible = entered,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -48 }
            ) {
                AppLogo()
            }

            Spacer(Modifier.height(28.dp))

            // ── Wordmark ─────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = entered,
                enter = fadeIn(tween(600, delayMillis = 120))
            ) {
                Wordmark()
            }

            Spacer(Modifier.weight(1f))

            // ── Card ─────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = entered,
                enter = fadeIn(tween(600, delayMillis = 250)) +
                        slideInVertically(tween(550, delayMillis = 250)) { 64 }
            ) {
                SignInCard(
                    isOffline = state.isOffline,
                    isLoading = isLoading,
                    loadingSource = state.loadingSource,
                    onGoogleSignIn = onGoogleSignIn,
                    onGuestSignIn = onGuestSignIn
                )
            }

            // ── Error chip ───────────────────────────────────────────────────
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(tween(280)) + slideInVertically(tween(280)) { 16 },
                exit = fadeOut(tween(200))
            ) {
                if (errorMessage != null) {
                    Spacer(Modifier.height(12.dp))
                    Log.d("Login", errorMessage)
                    ErrorChip(message = errorMessage, onDismiss = onClearError)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Legal ────────────────────────────────────────────────────────
            Text(
                text = "By continuing you agree to our Terms & Privacy Policy",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.50f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Lotus brand mark
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AppLogo() {
    val colors = MaterialTheme.colorScheme
    Box(contentAlignment = Alignment.Center) {
        // Outer diffuse ring
        Box(
            modifier = Modifier
                .size(108.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to colors.primary.copy(alpha = 0.18f),
                            0.6f to colors.primary.copy(alpha = 0.06f),
                            1.0f to Color.Transparent
                        )
                    )
                )
        )
        // Icon disc
        Box(
            modifier = Modifier
                .size(82.dp)
                .shadow(elevation = 6.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(colors.primary),
            contentAlignment = Alignment.Center
        ) {
            // Replace with: Icon(painterResource(R.drawable.ic_lotus), contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
            Image(
                painter = painterResource(R.drawable.iyf),
                contentDescription = "App Logo",
                modifier = Modifier.size(96.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Wordmark
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Wordmark() {
    val colors = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Hare Krishna",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            ),
            color = colors.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "OTP Classes",
            style = MaterialTheme.typography.titleSmall.copy(
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Normal
            ),
            color = colors.onBackground.copy(alpha = 0.45f)
        )
        Spacer(Modifier.height(12.dp))
        // Decorative rule
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(colors.primary.copy(alpha = 0.40f))
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Offline banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OfflineBanner(onRetry: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.errorContainer,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.WifiOff,
                contentDescription = null,
                tint = colors.onErrorContainer,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "No internet — you can still join as a guest",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onRetry,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Retry connection",
                    tint = colors.onErrorContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sign-in card — two distinct options
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SignInCard(
    isOffline: Boolean,
    isLoading: Boolean,
    loadingSource: LoadingSource,
    onGoogleSignIn: () -> Unit,
    onGuestSignIn: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = colors.primary.copy(alpha = 0.06f),
                spotColor = colors.primary.copy(alpha = 0.09f)
            ),
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome, Prabhu 🙏",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Choose how you'd like to continue",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // ── Google (Credential Manager) ──────────────────────────────────
            OptionButton(
                label = "Sign in with Google",
                sublabel = if (isOffline) "Unavailable — no internet connection"
                else "Full access · Your progress is saved",
                isLoading = loadingSource == LoadingSource.GOOGLE,
                enabled = !isLoading && !isOffline,
                isPrimary = true,
                leadingIcon = {
                    // Swap for: Icon(painterResource(R.drawable.ic_google), null, tint = colors.onPrimary)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.92f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.primary
                        )
                    }
                },
                onClick = onGoogleSignIn
            )

            Spacer(Modifier.height(12.dp))

            // ── Divider ──────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(Modifier.weight(1f), color = colors.outlineVariant)
                Text(
                    text = "  or  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.50f)
                )
                HorizontalDivider(Modifier.weight(1f), color = colors.outlineVariant)
            }

            Spacer(Modifier.height(12.dp))

            // ── Guest / offline ──────────────────────────────────────────────
            OptionButton(
                label = if (isOffline) "Join as guest (offline)" else "Continue as guest",
                sublabel = if (isOffline) "Anonymous session · Works without internet"
                else "No account needed · Limited access",
                isLoading = loadingSource == LoadingSource.ANONYMOUS,
                enabled = !isLoading,
                isPrimary = false,
                isOfflineHighlight = isOffline,
                leadingIcon = {
                    Icon(
                        imageVector = if (isOffline) Icons.Outlined.CloudOff else Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = when {
                            isOffline -> colors.tertiary
                            else -> colors.onSurfaceVariant
                        }
                    )
                },
                onClick = onGuestSignIn
            )

            if (isOffline) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Guest data syncs automatically when you reconnect",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// OptionButton — shared by both sign-in paths
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OptionButton(
    label: String,
    sublabel: String,
    isLoading: Boolean,
    enabled: Boolean,
    isPrimary: Boolean,
    isOfflineHighlight: Boolean = false,
    leadingIcon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    val containerColor = when {
        isPrimary -> colors.primary
        isOfflineHighlight -> colors.tertiaryContainer
        else -> Color.Transparent
    }
    val contentColor = when {
        isPrimary -> colors.onPrimary
        isOfflineHighlight -> colors.onTertiaryContainer
        else -> colors.onSurface
    }
    val borderColor = when {
        isPrimary -> Color.Transparent
        isOfflineHighlight -> colors.tertiary.copy(alpha = 0.45f)
        else -> colors.outline.copy(alpha = 0.40f)
    }

    val alpha by animateFloatAsState(
        targetValue = if (!enabled && !isLoading) 0.36f else 1f,
        animationSpec = tween(200),
        label = "optionAlpha"
    )

    Surface(
        onClick = { if (enabled && !isLoading) onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .alpha(alpha),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = if (borderColor != Color.Transparent)
            androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
        else null,
        enabled = enabled && !isLoading
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "iconOrSpinner"
            ) { loading ->
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = contentColor,
                        strokeWidth = 2.2.dp
                    )
                } else {
                    leadingIcon()
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = contentColor
                )
                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.65f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Error chip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorChip(message: String, onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.labelMedium,
                color = colors.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "Dismiss",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.error
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews — one for every meaningful state
// ─────────────────────────────────────────────────────────────────────────────

@Preview(name = "Idle · Light", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewIdleLight() {
    Otp_class_appTheme(ThemeMode.LIGHT) {
        LoginScreenContent(
            state = LoginUiState(),
            onGoogleSignIn = {}, onGuestSignIn = {},
            onRetry = {}, onClearError = {}
        )
    }
}

@Preview(
    name = "Idle · Dark", showBackground = true, widthDp = 380, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewIdleDark() {
    Otp_class_appTheme(ThemeMode.DARK) {
        LoginScreenContent(
            state = LoginUiState(),
            onGoogleSignIn = {}, onGuestSignIn = {},
            onRetry = {}, onClearError = {}
        )
    }
}

@Preview(name = "Offline · Light", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewOfflineLight() {
    Otp_class_appTheme(ThemeMode.LIGHT) {
        LoginScreenContent(
            state = LoginUiState(isOffline = true),
            onGoogleSignIn = {}, onGuestSignIn = {},
            onRetry = {}, onClearError = {}
        )
    }
}

@Preview(
    name = "Offline · Dark", showBackground = true, widthDp = 380, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewOfflineDark() {
    Otp_class_appTheme(ThemeMode.DARK) {
        LoginScreenContent(
            state = LoginUiState(isOffline = true),
            onGoogleSignIn = {}, onGuestSignIn = {},
            onRetry = {}, onClearError = {}
        )
    }
}

@Preview(name = "Loading Google · Light", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewLoadingGoogle() {
    Otp_class_appTheme(ThemeMode.LIGHT) {
        LoginScreenContent(
            state = LoginUiState(
                loginState = LoginState.Loading,
                loadingSource = LoadingSource.GOOGLE
            ),
            onGoogleSignIn = {}, onGuestSignIn = {},
            onRetry = {}, onClearError = {}
        )
    }
}

@Preview(name = "Loading Guest · Offline", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewLoadingGuest() {
    Otp_class_appTheme(ThemeMode.LIGHT) {
        LoginScreenContent(
            state = LoginUiState(
                loginState = LoginState.Loading,
                loadingSource = LoadingSource.ANONYMOUS,
                isOffline = true
            ),
            onGoogleSignIn = {}, onGuestSignIn = {},
            onRetry = {}, onClearError = {}
        )
    }
}

@Preview(name = "Error · Light", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewError() {
    Otp_class_appTheme(ThemeMode.LIGHT) {
        LoginScreenContent(
            state = LoginUiState(
                loginState = LoginState.Error("Sign-in cancelled. Try again or continue offline.")
            ),
            onGoogleSignIn = {}, onGuestSignIn = {},
            onRetry = {}, onClearError = {}
        )
    }
}