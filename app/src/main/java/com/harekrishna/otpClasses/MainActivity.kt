package com.harekrishna.otpClasses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.harekrishna.otpClasses.navigation.MainNavHost
import com.harekrishna.otpClasses.ui.dashboard.AppStartViewModel
import com.harekrishna.otpClasses.ui.dashboard.DashboardScreen
import com.harekrishna.otpClasses.ui.theme.Otp_class_appTheme
import com.harekrishna.otpClasses.ui.utils.NoInternetScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            Otp_class_appTheme (themeMode = themeMode) { // Wrap content in AppTheme
                AppStartEntry()
            }
        }
    }
}

@Composable
fun AppStartEntry(
    vm: AppStartViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val navController = rememberNavController()

    // while app is starting, i.e configuring navigation
    if (!state.isReady)
        return

    if (!state.isConnected) {
        NoInternetScreen { vm.retry() }
        return
    }

    MainNavHost(
        navController = navController,
        startDestination = state.startDestination
    )
}




@Composable
@Preview(showBackground = true)
fun MainPreview() {
    Otp_class_appTheme {
        DashboardScreen(navController = rememberNavController())
    }
}
