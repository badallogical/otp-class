package com.example.otp_class_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.otp_class_app.screens.DashboardScreen
import com.example.otp_class_app.screens.ReportingScreen
import com.example.otp_class_app.screens.StudentFormScreen
import com.example.otp_class_app.ui.screens.AttendanceScreen
import com.example.otp_class_app.ui.theme.Otp_class_appTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Otp_class_appTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavHost()
                }
            }
        }
    }
}

@Composable
fun MainNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") { DashboardScreen(navController) }
        composable("registration") { StudentFormScreen() }
        composable("attendance") { AttendanceScreen() }
        composable("reporting") { ReportingScreen() }
    }
}
