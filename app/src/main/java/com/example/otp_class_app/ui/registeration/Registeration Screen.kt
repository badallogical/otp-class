package com.example.otp_class_app.ui.registeration

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import androidx.lifecycle.viewmodel.compose.viewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistrationScreen(navController: NavController, viewModel: RegistrationViewModel = viewModel()) {

    val registrations by viewModel.registrations.collectAsState()

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
            }
            // Main content
            Box(modifier = Modifier.padding(paddingValues)) {
                RegistrationListView(registrations, navController)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistrationListView(registrations :List<Pair<String,Int>>, navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        registrations.forEach { (date,count) ->

            item {
                RegistrationItem(
                    date = date,
                    count = count,
                    navController = navController
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistrationItem(date: String, count : Int, navController: NavController) {

    val formattedDate = LocalDate.parse(date)
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)) // Format the date

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { navController.navigate("calling_screen") }, // Open dialog on click
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
                Text(text = "Date: $formattedDate", style = MaterialTheme.typography.bodySmall)
                Text(text = "Registrations: $count", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun RegistrationDialog(date: String, registrations: List<Registration>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Title with formatted date
                Text(
                    text = date,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // List of registrations (name and phone)
                registrations.forEach { registration ->
                    RegistrationTile(registration = registration)
                }

                // Close button
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun RegistrationTile(registration: Registration) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "Name: ${registration.name}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Phone: ${registration.phone}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

// Define the Registration data class
data class Registration(val name: String, val phone: String, val isSynced: Boolean)

// Example preview for this composable
@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun PreviewRegistrationScreen() {
    val mockData = mapOf(
        "2024-09-23" to listOf(
            Registration("John Doe", "1234567890", true),
            Registration("Jane Smith", "0987654321", false)
        ),
        "2024-09-22" to listOf(
            Registration("Alice Brown", "1231231231", true)
        )
    )

}
