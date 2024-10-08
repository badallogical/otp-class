package com.example.otp_class_app.ui.registeration

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.otp_class_app.R
import com.example.otp_class_app.data.models.RegistrationStatus

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistrationScreen(
    navController: NavController,
    viewModel: RegistrationViewModel = viewModel(factory = RegistrationViewModel.Factory)
) {
    // Collect state from ViewModel
    val registrations by viewModel.registrations.collectAsState()
    val syncing by viewModel.syncing.collectAsState()

    LaunchedEffect(Unit){
        viewModel.getRegistration()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(16.dp),
                onClick = { navController.navigate("form") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Registration")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(16.dp)) {
            HeaderSection(viewModel, syncing)

            // Main content
            Box(modifier = Modifier.padding(paddingValues)) {
                RegistrationListView(registrations, navController)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeaderSection(viewModel: RegistrationViewModel, syncing: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Registrations",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(4f) // Take up as much space as possible
        )

        // Sync Icon Button
        IconButton(
            onClick = {
                // Trigger the sync action only if not syncing
                if (!syncing) {
                    viewModel.syncRegistrations() // Sync registrations
                }
            },
            modifier = Modifier.padding(start = 16.dp) // Add space between text and icon
        ) {
            if (syncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.baseline_sync_24),
                    contentDescription = "Sync",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistrationListView(registrations: List<RegistrationStatus>, navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(registrations) { registration ->
            RegistrationItem(data = registration, navController = navController)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistrationItem(data: RegistrationStatus, navController: NavController) {
    val formattedDate = LocalDate.parse(data.date)
        .format(DateTimeFormatter.ofPattern("EEE, MMMM dd, yyyy")) // Format date

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { navController.navigate("calling_screen/${data.date}") }, // Navigate on click
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Date: $formattedDate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Registrations: ${data.counts}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            // Sync status indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (data.synced) Color.Green else Color.Red)
            )
        }
    }
}



// Example preview for this composable
@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun PreviewRegistrationScreen() {
    RegistrationItem(RegistrationStatus("2024-12-01", 2), rememberNavController())
}
