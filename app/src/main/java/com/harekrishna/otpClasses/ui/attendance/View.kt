package com.harekrishna.otpClasses.ui.attendance

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.harekrishna.otpClasses.R
import com.harekrishna.otpClasses.data.api.ApiService.syncAttendance
import com.harekrishna.otpClasses.data.api.AttendanceDataStore
import com.harekrishna.otpClasses.data.models.AttendanceDTO
import com.harekrishna.otpClasses.data.models.AttendancePOJO
import com.harekrishna.otpClasses.data.models.toDTO
import com.harekrishna.otpClasses.ui.theme.Otp_class_appTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceViewScreen(context: Context) {
    var attendanceMap by remember { mutableStateOf(emptyMap<String, List<AttendanceDTO>>()) }
    var attendanceMapPOJO by remember { mutableStateOf(emptyMap<String, List<AttendancePOJO>>()) }
    var showSyncingDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val fetchAttendances: () -> Unit = {
        CoroutineScope(Dispatchers.IO).launch {
            AttendanceDataStore.getAttendanceMap().collect { pojoMap -> // ✅ Collect continuously
                Log.d("Attendance", pojoMap.toString())

                attendanceMapPOJO = pojoMap

                attendanceMap = if (pojoMap.isNullOrEmpty()) {
                    emptyMap()  // ✅ Set empty if there's no data
                } else {
                    pojoMap.mapValues { (_, pojoList) ->
                        pojoList.mapNotNull { pojo -> pojo.toDTO() }  // ✅ Convert safely
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchAttendances()
    }


    @Composable
    fun SyncAttendanceDialog(onDismiss: () -> Unit) {
        val coroutineScope = rememberCoroutineScope()
        var currentDay by remember { mutableStateOf(1) }
        var progress by remember { mutableStateOf(0) }
        var totalAttendances by remember { mutableStateOf(0) }
        var savedAttendances by remember { mutableStateOf(0) }
        var isSyncComplete by remember { mutableStateOf(false) }
        var isFailed by remember { mutableStateOf(false) }

        val totalDays = attendanceMap.size // Total number of days (dates) to sync
        totalAttendances = attendanceMap.values.sumOf { it.size } // Total attendance count

        // LaunchedEffect to start syncing when dialog is shown
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                val syncResult = syncAttendance(attendanceMap) { count ->
                    if (count == -1) {
                        isSyncComplete = true
                        isFailed = true
                    }
                    savedAttendances += count
                }

                isSyncComplete = syncResult
            }
        }

        // Change color to green when sync is complete
        val progressColor =
            if (isSyncComplete) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier
                .size(350.dp)
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Title
                Text(
                    text = "Syncing Attendance",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (isSyncComplete) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!isSyncComplete) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(50.dp), // Adjust size as needed
                        color = Color.Blue, // Customize color
                        strokeWidth = 4.dp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display the day progress
                Text(
                    text = if (!isSyncComplete) "Day $currentDay of $totalDays" else "All Days Synced!",
                    style = MaterialTheme.typography.labelSmall,
                    color = progressColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Display the current attendance progress
                Text(
                    text = if (!isSyncComplete) "Attendances saved: $savedAttendances / $totalAttendances"
                    else if (isFailed) "Sync Failed " else "All Attendance Synced!",
                    style = MaterialTheme.typography.labelMedium,
                    color = progressColor
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Close button
                Button(
                    onClick = { onDismiss() },  // Call onDismiss() when clicked
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSyncComplete) progressColor else MaterialTheme.colorScheme.primary,  // Set background color
                        contentColor = Color.White  // Set text color for better visibility
                    ),
                    enabled = isSyncComplete  // Enable button only when sync is complete
                ) {
                    Text(text = if (isSyncComplete) "Close" else "Syncing...")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance Records") },
                actions = {
                    IconButton(onClick = {
                        showSyncingDialog = true // Show the dialog when clicked
                    }) {
                        Icon(
                            painterResource(id = R.drawable.baseline_sync_24),
                            contentDescription = "Sync"
                        )
                    }
                }
            )
        },
        content = { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (attendanceMapPOJO.isEmpty()) {
                    // if Empty
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No attendance records found.")
                    }
                } else {
                    // show list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        items(attendanceMapPOJO.keys.toList()) { date ->
                            val attendanceList = attendanceMapPOJO[date] ?: emptyList()
                            AttendanceCard(date, attendanceList)
                        }
                    }
                }

                // Show the SyncAttendanceDialog overlay if the sync button is clicked
                if (showSyncingDialog) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        SyncAttendanceDialog(onDismiss = {
                            showSyncingDialog = false
                        })
                    }
                }
            }
        }
    )
}


data class Result(val success: Boolean, val message: String?)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AttendanceCard(date: String, list: List<AttendancePOJO>) {
    var showDialog by remember { mutableStateOf(false) }
    var updatedList by remember { mutableStateOf(list) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(),
        onClick = {
            showDialog = true
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Date: $date", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Count: ${updatedList.size}", style = MaterialTheme.typography.bodySmall)
        }
    }

    if (showDialog) {
        val coroutineScope = rememberCoroutineScope()
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Attendance List") },
            text = {
                Box(
                    modifier = Modifier
                        .height(350.dp) // Fixed height for the list
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(updatedList) { index, attendance ->
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            val date = LocalDate.parse(attendance.regDate, formatter)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = attendance.name)
                                    Text(text = attendance.studentId)
                                    Text(text = "Register on $date")
                                }

                                IconButton(onClick = {
                                    updatedList =
                                        updatedList.toMutableList().also { it.removeAt(index) }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete"
                                    )
                                }
                            }
                            Divider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            AttendanceDataStore.updateAttendance(date, updatedList)
                        }
                        showDialog = false // ✅ Ensures UI update runs on Main thread
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )

    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview(showBackground = true)
fun ViewPreview() {
    Otp_class_appTheme {
        AttendanceViewScreen(LocalContext.current)
    }
}
