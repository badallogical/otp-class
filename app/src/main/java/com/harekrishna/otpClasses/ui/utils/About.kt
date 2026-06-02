package com.harekrishna.otpClasses.ui.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.harekrishna.otpClasses.R

// AboutScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Background color from the theme
                    titleContentColor = MaterialTheme.colorScheme.onPrimary // Title text color from the theme
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
                text = "© ${stringResource(R.string.year)} IYF. All rights reserved.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
