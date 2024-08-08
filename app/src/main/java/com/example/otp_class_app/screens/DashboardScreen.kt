package com.example.otp_class_app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dashboard") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashboardOption(
                text = "Registration",
                onClick = { navController.navigate("registration") }
            )
            DashboardOption(
                text = "Attendance",
                onClick = { navController.navigate("attendance") }
            )
            DashboardOption(
                text = "Reporting",
                onClick = { navController.navigate("reporting") }
            )
        }
    }
}

@Composable
fun DashboardOption(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}
