package com.example.otp_class_app.screens

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.otp_class_app.R
import com.example.otp_class_app.api.ApiService
import com.example.otp_class_app.api.ApiService.syncAttendance
import com.example.otp_class_app.api.AttendanceDataStore
import com.example.otp_class_app.models.AttendanceDTO
import com.example.otp_class_app.ui.theme.Otp_class_appTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceViewScreen(context: Context) {
    val coroutineScope = rememberCoroutineScope()
    var attendanceMap by remember {
        mutableStateOf(emptyMap<String, List<AttendanceDTO>>())
    }
    var isSyncing by remember { mutableStateOf(false) }
    var syncResult by remember { mutableStateOf<Result?>(null) }

    val context = LocalContext.current.applicationContext

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            AttendanceDataStore.getAttendanceMap().collect { data ->
                attendanceMap = data
            }
        }
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
                    CircularProgressIndicator()
                }
            } else if (attendanceMap.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No attendance records found.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(attendanceMap.keys.toList()) { date ->
                        val attendanceList = attendanceMap[date] ?: emptyList()
                        AttendanceCard(date, attendanceList.size)
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
fun AttendanceCard(date: String, count: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Date: $date", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Count: $count", style = MaterialTheme.typography.bodySmall)
        }
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
