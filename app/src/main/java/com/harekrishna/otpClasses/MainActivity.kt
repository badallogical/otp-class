package com.harekrishna.otpClasses

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.harekrishna.otpClasses.data.api.AttendanceDataStore.getUserData
import com.harekrishna.otpClasses.data.models.ReportDTO
import com.harekrishna.otpClasses.screens.StudentFormScreen
import com.harekrishna.otpClasses.ui.attendance.AttendanceViewScreen
import com.harekrishna.otpClasses.ui.dashboard.DashboardScreen
import com.harekrishna.otpClasses.ui.dashboard.WelcomeScreen
import com.harekrishna.otpClasses.ui.followup.EditReportScreen
import com.harekrishna.otpClasses.ui.followup.FollowUpScreen
import com.harekrishna.otpClasses.ui.registeration.CallingListScreen
import com.harekrishna.otpClasses.ui.registeration.RegistrationScreen
import com.harekrishna.otpClasses.ui.screens.AttendanceScreen
import com.harekrishna.otpClasses.ui.theme.Otp_class_appTheme
import kotlinx.coroutines.flow.first


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Otp_class_appTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavHost()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavHost(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    var isConnected by remember { mutableStateOf(checkInternetConnection(context)) }

    var startDestination by remember { mutableStateOf("welcome") } // Default to welcome
    var isUserRegistered by remember { mutableStateOf(false)}

    // Use LaunchedEffect to check for internet connectivity when the screen is opened
    LaunchedEffect(Unit) {

        isConnected = checkInternetConnection(context)
        val isRegistered = checkIfUserIsRegistered() // Call your function to check registration
        startDestination = if (isRegistered) "dashboard" else "welcome"
        isUserRegistered = true
    }


    if( isUserRegistered ) {
        if (isConnected) {
            // Show the main content if connected
            NavHost(navController = navController, startDestination = startDestination) {
                composable("dashboard") { DashboardScreen(navController) }
                composable("welcome") { WelcomeScreen(navController) }
                composable("registration") { RegistrationScreen(navController) }
                composable("attendance") { AttendanceScreen(navController) }
                composable("followup") { FollowUpScreen() }
                composable("attendance_view") { AttendanceViewScreen(context) }
                composable("edit_report/{report}") { backStackEntry ->
                    val reportJson = backStackEntry.arguments?.getString("report")
                    val report = reportJson?.let {
                        Gson().fromJson(it, ReportDTO::class.java)
                    }
                    if (report != null) {
                        EditReportScreen(report, onCancel = {
                            navController.popBackStack()
                        })
                    }
                }
                composable("form") { StudentFormScreen() }
                composable("calling_screen/{date}") { backStackEntry ->
                    // Retrieve the date from the backStackEntry arguments
                    val date = backStackEntry.arguments?.getString("date") ?: ""

                    // Pass the date to your screen
                    CallingListScreen(date = date)
                }
            }
        } else {
            // Show NoInternetScreen if not connected
            NoInternetScreen(onRetry = {
                // Retry connection when the refresh icon is clicked
                isConnected = MyApplication.checkInternetConnection()
            })
        }
    }
}

suspend fun checkIfUserIsRegistered(): Boolean {
   return getUserData().first().let { (name, phone) ->
        if (name != null && phone != null) {
            // User is registered
            true
        } else {
            // Handle the case where no user data is found
            println("User data not found in DataStore.")
            false
        }
    }
}

@Composable
fun NoInternetScreen(onRetry: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No internet connection", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    }
}

fun checkInternetConnection(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        else -> false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview(showBackground = true)
fun MainPreview() {
    Otp_class_appTheme {
        DashboardScreen(navController = rememberNavController())
    }
}
