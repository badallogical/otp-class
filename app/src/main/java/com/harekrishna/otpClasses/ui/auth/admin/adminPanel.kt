package com.harekrishna.otpClasses.ui.auth.admin

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.harekrishna.otpClasses.MyApplication.Companion.toCamelCase
import com.harekrishna.otpClasses.ui.auth.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen( userdata : UserData ) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(0) }


    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home") },
                    selected = selectedItem == 0,
                    onClick = {
                        selectedItem = 0
                      navController.navigate("admin_home")
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, "Sadhna Score") },
                    label = { Text("Sadhna") },
                    selected = selectedItem == 1,
                    onClick = {
                        selectedItem = 1
                       navController.navigate("admin_sadhna")
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Phone, "Calls") },
                    label = { Text("Distribution") },
                    selected = selectedItem == 2,
                    onClick = {
                        selectedItem = 2
                       navController.navigate("admin_distribution")
                    }
                )
            }
        }
    ) { paddingValues ->
        Log.d("AdminPanel", "Opened the screen what about the home screen.")
        NavHost(
            navController = navController,
            startDestination = "admin_home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("admin_home") { HomeScreen(userdata) }
            composable("admin_sadhna") { SadhnaScoreScreen() }
            composable("admin_distribution") { CallDistributionScreen() }
        }
    }
}

@Composable
fun HomeScreen(userdata: UserData) {
    Log.d("AdminPanel", "Home Screen")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        userdata.username?.let { userdata.profilePictureUrl?.let { it1 -> ProfileSection(it1, it, userdata.userId) } }
        Text(
            text = "OTP Class Attendance",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AttendanceList()
    }
}

@Composable
fun ProfileSection(profileImageUrl: String, username: String, userEmail: String) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Profile Image
            AsyncImage(
                model = profileImageUrl,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(0.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(4.dp))

            // User Name
            Text(
                text = "Hare Krishna, ${username.toCamelCase()}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
}


@Composable
fun AttendanceList() {
    LazyColumn {
        items(sampleAttendanceData) { student ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Classes: ${student.totalClasses}")
                        Text("Last Class: ${student.lastClass}")
                    }
                }
            }
        }
    }
}

@Composable
fun SadhnaScoreScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Monthly Sadhna Scores",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Facilitator Filter Dropdown
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Select Facilitator") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        SadhnaScoreList()
    }
}

@Composable
fun SadhnaScoreList() {
    LazyColumn {
        items(sampleSadhnaData) { devotee ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = devotee.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Score: ${devotee.score}")
                        Text("Month: ${devotee.month}")
                    }
                }
            }
        }
    }
}

@Composable
fun CallDistributionScreen() {
    var showAssignDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Call Distribution",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(onClick = { showAssignDialog = true }) {
                Text("Assign Calls")
            }
        }

        CallList()
    }

    if (showAssignDialog) {
        AssignCallsDialog(onDismiss = { showAssignDialog = false })
    }
}

@Composable
fun CallList() {
    LazyColumn {
        items(sampleCallData) { student ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(student.number)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Last Class: ${student.lastClass}")
                        Text("Total: ${student.totalClasses}")
                    }
                    if (student.assignedTo.isNotEmpty()) {
                        Text(
                            "Assigned to: ${student.assignedTo}",
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AssignCallsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Calls") },
        text = {
            Column {
                Text("Select devotees and their call capacity:")
                // Add devotee selection and capacity inputs here
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Assign")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Sample data classes and data
data class AttendanceRecord(
    val name: String,
    val totalClasses: Int,
    val lastClass: String
)

data class SadhnaScore(
    val name: String,
    val score: Int,
    val month: String
)

data class CallRecord(
    val name: String,
    val number: String,
    val lastClass: String,
    val totalClasses: Int,
    val assignedTo: String = ""
)

val sampleAttendanceData = listOf(
    AttendanceRecord("John Doe", 5, "2024-01-15"),
    AttendanceRecord("Jane Smith", 4, "2024-01-14")
)

val sampleSadhnaData = listOf(
    SadhnaScore("John Doe", 85, "January"),
    SadhnaScore("Jane Smith", 90, "January")
)

val sampleCallData = listOf(
    CallRecord("John Doe", "1234567890", "2024-01-15", 5, "Devotee 1"),
    CallRecord("Jane Smith", "0987654321", "2024-01-14", 4)
)

@Preview
@Composable
fun preview(){
    AdminPanelScreen(UserData("badallearn@gmail.com","badal", " "))
}