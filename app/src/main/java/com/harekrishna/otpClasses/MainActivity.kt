package com.harekrishna.otpClasses

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.BuildConfig
import com.google.gson.Gson
import com.harekrishna.otpClasses.data.api.AttendanceDataStore.getUserData
import com.harekrishna.otpClasses.data.models.ReportDTO
import com.harekrishna.otpClasses.screens.StudentFormScreen
import com.harekrishna.otpClasses.ui.attendance.AttendanceViewScreen
import com.harekrishna.otpClasses.ui.auth.UserData
import com.harekrishna.otpClasses.ui.auth.admin.AdminPanelScreen
import com.harekrishna.otpClasses.ui.dashboard.DashboardScreen
import com.harekrishna.otpClasses.ui.dashboard.SettingsScreen
import com.harekrishna.otpClasses.ui.dashboard.WelcomeScreen
import com.harekrishna.otpClasses.ui.followup.EditReportScreen
import com.harekrishna.otpClasses.ui.followup.FollowUpScreen
import com.harekrishna.otpClasses.ui.registeration.CallingListScreen
import com.harekrishna.otpClasses.ui.registeration.RegistrationScreen
import com.harekrishna.otpClasses.ui.screens.AttendanceScreen
import com.harekrishna.otpClasses.ui.theme.Otp_class_appTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Otp_class_appTheme { // Wrap content in AppTheme
                MainNavHost()
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
    var isUserRegistered by remember { mutableStateOf(false) }

    // Use LaunchedEffect to check for internet connectivity when the screen is opened
    LaunchedEffect(Unit) {

        isConnected = checkInternetConnection(context)
        val isRegistered = checkIfUserIsRegistered() // Call your function to check registration
        startDestination = if (isRegistered) "dashboard" else "welcome"
        isUserRegistered = true
    }


    if (isUserRegistered) {
        if (isConnected) {
            // Show the main content if connected
            NavHost(navController = navController, startDestination = startDestination) {
                composable("dashboard") { DashboardScreen(navController) }
                composable("settings") { SettingsScreen(navController) }
                composable("about") { AboutScreen() }
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
                composable("form") { StudentFormScreen(editId = null) }
                composable("form_edit/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true })
                ){ backStackEntry ->
                    val editId = backStackEntry.arguments?.getString("id")
                    StudentFormScreen(editId)
                }

                composable("calling_screen/{date}") { backStackEntry ->
                    // Retrieve the date from the backStackEntry arguments
                    val date = backStackEntry.arguments?.getString("date") ?: ""

                    // Pass the date to your screen
                    CallingListScreen(date = date,navController)
                }
                composable(
                    route = "admin_panel/{user}",
                    arguments = listOf(navArgument("user") { type = NavType.StringType })){ backStackEntry ->
                    Log.d("AdminPanel", "openning")
                    val gson = Gson()
                    val userJson = backStackEntry.arguments?.getString("user")

                    Log.d("AdminPanel", userJson.toString())

                    val user = if (userJson != null) {
                        gson.fromJson(userJson, UserData::class.java)
                    } else {
                        UserData("", "", "")
                    }

                    AdminPanelScreen(user)
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



// AboutScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo/Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center // Centers content inside the Box
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iyflogo), // Use painterResource for drawable resources
                    contentDescription = "App Logo",
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // App Title
            Text(
                text = "OTP Registration",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // App Objective
            Text(
                text = "Our mission is to provide a simple and intuitive platform for managing registrations and fostering healthy connections with our youth. We believe in creating user-friendly experiences that make your life easier.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Justify, // Center-align the text
                modifier = Modifier
                    .fillMaxWidth() // Ensures the text spans the full width for proper centering
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Adds consistent spacing around the text
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Version Info
            Text(
                text = "Version "+ stringResource(R.string.version_name),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            // Copyright Info
            Text(
                text = "© 2024 IYF. All rights reserved.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
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
