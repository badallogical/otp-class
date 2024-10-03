package com.example.otp_class_app.ui.registeration

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.otp_class_app.data.local.db.dao.Registration
import com.example.otp_class_app.data.local.db.dao.RegistrationCount

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistrationScreen(navController: NavController, viewModel: RegistrationViewModel = viewModel( factory = RegistrationViewModel.Factory)) {

    val registrations by viewModel.registrations.collectAsState()

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
fun RegistrationListView(registrations: List<RegistrationCount>, navController: NavController) {
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
            .clickable { navController.navigate("calling_screen/${date}") }, // Open dialog on click
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


// Example preview for this composable
@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun PreviewRegistrationScreen() {

}
