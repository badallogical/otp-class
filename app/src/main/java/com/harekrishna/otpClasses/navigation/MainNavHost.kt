package com.harekrishna.otpClasses.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.harekrishna.otpClasses.screens.StudentFormScreen
import com.harekrishna.otpClasses.ui.attendance.AttendanceDetailsScreen
import com.harekrishna.otpClasses.ui.attendance.AttendanceHistoryScreen
import com.harekrishna.otpClasses.ui.dashboard.DashboardScreen
import com.harekrishna.otpClasses.ui.settings.SettingsScreen
import com.harekrishna.otpClasses.ui.dashboard.WelcomeScreen
import com.harekrishna.otpClasses.ui.followup.FollowUpScreen
import com.harekrishna.otpClasses.ui.registeration.CallingListScreen
import com.harekrishna.otpClasses.ui.registeration.RegistrationScreen
import com.harekrishna.otpClasses.ui.screens.AttendanceScreen
import com.harekrishna.otpClasses.ui.utils.AboutScreen

@Composable
fun MainNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {

        composable("dashboard") { DashboardScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("about") { AboutScreen() }
        composable("welcome") { WelcomeScreen(navController) }
        composable("registration") { RegistrationScreen(navController) }



        composable("form") { StudentFormScreen(null) }
        composable("form_edit/{id}") {
            StudentFormScreen(it.arguments?.getString("id"))
        }
        composable("calling_screen/{date}") {
            CallingListScreen(it.arguments?.getString("date") ?: "", navController)
        }

        composable("attendance") { AttendanceScreen(navController) }
        composable("attendance_view") {
            AttendanceHistoryScreen(navController) }
        composable("attendance_details/{date}") {
            AttendanceDetailsScreen(it.arguments?.getString("date") ?: "")
        }

        composable("followup") { FollowUpScreen() }


//        composable("edit_report/{report}") { backStackEntry ->
//            val reportJson = backStackEntry.arguments?.getString("report")
//            val report = reportJson?.let {
//                Gson().fromJson(it, ReportDTO::class.java)
//            }
//            if (report != null) {
//                EditReportScreen(report, onCancel = {
//                    navController.popBackStack()
//                })
//            }
//        }

//        composable(
//            route = "admin_panel/{user}",
//            arguments = listOf(navArgument("user") { type = NavType.StringType })){ backStackEntry ->
//            Log.d("AdminPanel", "openning")
//            val gson = Gson()
//            val userJson = backStackEntry.arguments?.getString("user")
//
//            Log.d("AdminPanel", userJson.toString())
//
//            val user = if (userJson != null) {
//                gson.fromJson(userJson, UserData::class.java)
//            } else {
//                UserData("", "", "")
//            }
//
//            AdminPanelScreen(user)
//        }
    }
}
