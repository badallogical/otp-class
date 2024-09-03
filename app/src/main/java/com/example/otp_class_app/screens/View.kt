package com.example.otp_class_app.screens

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.otp_class_app.R
import com.example.otp_class_app.api.ApiService.syncAttendance
import com.example.otp_class_app.api.AttendanceDataStore
import com.example.otp_class_app.models.AttendanceDTO
import com.example.otp_class_app.models.AttendancePOJO
import com.example.otp_class_app.models.toDTO
import com.example.otp_class_app.ui.theme.Otp_class_appTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceViewScreen(context: Context) {
    val coroutineScope = rememberCoroutineScope()
    var attendanceMap by remember {
        mutableStateOf(emptyMap<String, List<AttendanceDTO>>())
    }
    var attendanceMapPOJO by remember {
        mutableStateOf(emptyMap<String, List<AttendancePOJO>>())
    }

    var isSyncing by remember { mutableStateOf(false) }
    var syncResult by remember { mutableStateOf<Result?>(null) }

    val fetchAttendances: () -> Unit = {
        CoroutineScope(Dispatchers.IO).launch {
            AttendanceDataStore.getAttendanceMap().collect { pojoMap ->
                attendanceMapPOJO = pojoMap

                val dtoMap = pojoMap.mapValues { (_, pojoList) ->
                    pojoList.map { pojo -> pojo.toDTO() }
                }
                attendanceMap = dtoMap
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchAttendances()
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance Records") },
                actions = {
                    IconButton(onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            isSyncing = true
                            val result = try {
                                fetchAttendances()
                                val success = syncAttendance(attendanceMap)
                                if( success ) AttendanceDataStore.clearAttendanceData()

                                Result(success, null)
                            } catch (e: Exception) {
                                Result(false, e.message)
                            }

                            isSyncing = false
                            syncResult = result
                        }
                    }) {
                        Icon(painterResource(id = R.drawable.baseline_sync_24), contentDescription = "Sync")
                    }
                }
            )
        },
        content = { padding ->
            if (isSyncing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // Syncing loading
                    CircularProgressIndicator()
                }
            } else if (attendanceMapPOJO.isEmpty()) {
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
                        AttendanceCard(date, attendanceList )
                    }
                }
            }
        }
    )

    // Show a dialog for sync result
    syncResult?.let {
        AlertDialog(
            onDismissRequest = { syncResult = null },
            title = {
                Text(if (it.success) "Sync Successful" else "Sync Failed")
            },
            text = {
                Text(it.message ?: "No additional details available.")
            },
            confirmButton = {
                Button(onClick = { syncResult = null }) {
                    Text("OK")
                }
            }
        )
    }
}

data class Result(val success: Boolean, val message: String?)

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
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Attendance List") },
            text = {
                Column {
                    updatedList.forEachIndexed { index, attendance ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = attendance.name)
                            IconButton(onClick = {
                                updatedList = updatedList.toMutableList().also { it.removeAt(index) }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    // Save the updated list to DataStore
                    CoroutineScope(Dispatchers.Main).launch {
                        AttendanceDataStore.updateAttendance(updatedList)
                        showDialog = false
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
        AttendanceViewScreen(LocalContext.current )
    }
}
