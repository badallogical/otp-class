package com.harekrishna.otpClasses.ui.registeration

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
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.harekrishna.otpClasses.R
import com.harekrishna.otpClasses.data.models.RegistrationStatus

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistrationScreen(
    navController: NavController,
    viewModel: RegistrationViewModel = viewModel(factory = RegistrationViewModel.Factory)
) {
    // Collect state from ViewModel
    val registrations by viewModel.registrations.collectAsState()
    val syncing by viewModel.syncing.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getRegistration()
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.padding(16.dp),
                onClick = { navController.navigate("form") },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Registration") },
                text = { Text("New Registration") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            HeaderSection(viewModel, syncing)
            Spacer(modifier = Modifier.height(8.dp))
            RegistrationListView(registrations, navController)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeaderSection(viewModel: RegistrationViewModel, syncing: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Registrations",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(3f),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Sync Icon Button with Tooltip
        IconButton(
            onClick = { if (!syncing) viewModel.syncRegistrations() },
            modifier = Modifier
                .size(40.dp)
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
    if (registrations.isEmpty()) {
        // Display empty state message if the list is empty
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No registrations available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(registrations) { registration ->
                RegistrationItem(data = registration, navController = navController)
                Spacer(modifier = Modifier.height(8.dp)) // Add spacing between items
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistrationItem(data: RegistrationStatus, navController: NavController) {
    val formattedDate = LocalDate.parse(data.date)
        .format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { navController.navigate("calling_screen/${data.date}") },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Registrations: ${data.counts}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (data.synced) Color.Green else Color.Red),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(if (data.synced) R.drawable.baseline_done_outline_24 else R.drawable.baseline_sync_24),
                    contentDescription = if (data.synced) "Synced" else "Not Synced",
                    modifier = Modifier.size(10.dp),
                    tint = Color.White
                )
            }
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
