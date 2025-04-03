package com.harekrishna.otpClasses.ui.attendance

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.harekrishna.otpClasses.R
import com.harekrishna.otpClasses.data.api.ApiService.syncAttendance
import com.harekrishna.otpClasses.data.api.AttendanceDataStore
import com.harekrishna.otpClasses.data.models.AttendanceDTO
import com.harekrishna.otpClasses.data.models.AttendancePOJO
import com.harekrishna.otpClasses.data.models.toDTO
import com.harekrishna.otpClasses.ui.theme.Otp_class_appTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Result(val success: Boolean, val message: String?)

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


//    @Composable
//    fun SyncAttendanceDialog(
//        date: String,
//        onDismiss: () -> Unit
//    ) {
//        val attendanceList = attendanceMap[date] ?: emptyList()
//
//        var progress by remember { mutableStateOf(0f) }
//        var savedAttendances by remember { mutableStateOf(0) }
//        var isSyncComplete by remember { mutableStateOf(false) }
//        var isFailed by remember { mutableStateOf(false) }
//        val totalAttendances = attendanceList.size
//
//        // LaunchedEffect to start syncing when dialog appears
//        LaunchedEffect(date, attendanceList) {
//            val syncResult = syncAttendance(date, attendanceList) { count ->
//                if (count == -1) {
//                    isSyncComplete = true
//                    isFailed = true
//                } else {
//                    savedAttendances += count
//                    progress = savedAttendances.toFloat() / totalAttendances
//                }
//            }
//            isSyncComplete = syncResult
//        }
//
//        AlertDialog(
//            onDismissRequest = { onDismiss() },
//            confirmButton = {
//                Button(
//                    onClick = { onDismiss() },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = if (isSyncComplete) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
//                        contentColor = Color.White
//                    ),
//                    enabled = isSyncComplete
//                ) {
//                    Text(text = if (isSyncComplete) "Close" else "Syncing...")
//                }
//            },
//            title = {
//                Text(
//                    text = "Syncing Attendance",
//                    style = MaterialTheme.typography.headlineSmall,
//                    color = if (isSyncComplete) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
//                )
//            },
//            text = {
//                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
//                    if (!isSyncComplete) {
//                        CircularProgressIndicator(
//                            progress = progress, // Updates progress dynamically
//                            color = Color.Blue,
//                            strokeWidth = 4.dp
//                        )
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//
//                    Text(
//                        text = when {
//                            !isSyncComplete -> "Attendances saved: $savedAttendances / $totalAttendances"
//                            isFailed -> "Sync Failed"
//                            else -> "All Attendance Synced!"
//                        },
//                        style = MaterialTheme.typography.labelMedium
//                    )
//                }
//            },
//            shape = RoundedCornerShape(16.dp),
//            modifier = Modifier.padding(16.dp),
//            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false) // Prevent accidental dismiss
//        )
//    }

    @Composable
    fun SyncAttendanceProgress(
        date: String,
        onSyncComplete: () -> Unit
    ) {
        val attendanceList = attendanceMap[date] ?: emptyList()
        var isSyncComplete by remember { mutableStateOf(false) }
        var isFailed by remember { mutableStateOf(false) }
        var showSuccessAnimation by remember { mutableStateOf(false) }


        var savedAttendances by remember { mutableStateOf(0) }
        val totalAttendances = attendanceList.size

        // LaunchedEffect starts sync when this UI appears
        LaunchedEffect(date, attendanceList) {
            val syncResult = syncAttendance(date, attendanceList) { count ->
                if (count == -1) {
                    isSyncComplete = true
                    isFailed = true
                }else{
                    savedAttendances += count
                }
            }
            isSyncComplete = syncResult

            if (!isFailed) {
                showSuccessAnimation = true // ✅ Show green tick animation
                delay(1000) // ✅ Hold green tick for 1 second
                onSyncComplete() // ✅ Auto-dismiss dialog
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth() // ✅ Only take up required width
                .height(150.dp) // ✅ Limit height to avoid covering everything
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = !showSuccessAnimation, // ✅ Hide when green tick appears
                exit = fadeOut(tween(500)) + scaleOut(tween(500)) // ✅ Exit animation
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isSyncComplete) {
                        CircularProgressIndicator( // ✅ Indeterminate mode (no progress)
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(56.dp) // ✅ Set a proper size
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Syncing Attendance...")
                        Text(text = "Progress: $savedAttendances / $totalAttendances")
                    } else if (isFailed) {
                        Text(
                            text = "Sync Failed!",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ✅ Show success animation (Green Tick ✔)
            AnimatedVisibility(
                visible = showSuccessAnimation,
                enter = fadeIn(tween(300)) + scaleIn(tween(300)), // ✅ Green tick appears smoothly
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Sync Successful",
                    tint = Color(0xFF4CAF50), // ✅ Green color
                    modifier = Modifier.size(100.dp) // ✅ Bigger tick mark
                )
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun AttendanceCard(date: String) {
        var showDialog by remember { mutableStateOf(false) }
        var showSyncing by remember { mutableStateOf(false) } // ✅ Controls syncing UI visibility
        val attendanceList = attendanceMapPOJO[date] ?: emptyList()
        var updatedList by remember { mutableStateOf(attendanceList) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(),
            onClick = { showDialog = true }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Date: $date", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Count: ${updatedList.size}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (showDialog) {
            val coroutineScope = rememberCoroutineScope()
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Attendance List")
                        IconButton(onClick = {
                            showSyncing = true // ✅ Show progress when sync starts
                        }) {
                            Icon(
                                painterResource(id = R.drawable.baseline_sync_24),
                                contentDescription = "Sync"
                            )
                        }
                    }
                },
                text = {
                    Column {
                        // ✅ Show sync progress UI when syncing
                        if (showSyncing) {
                            SyncAttendanceProgress(
                                date = date,
                                onSyncComplete = {
                                    showSyncing = false // Hide progress after sync
                                }
                            )
                        }

                        // Attendance List UI
                        Box(
                            modifier = Modifier
                                .height(350.dp)
                                .fillMaxWidth()
                        ) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                                            Text(text = "Registered on $date")
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
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                AttendanceDataStore.updateAttendance(date, updatedList)
                            }
                            showDialog = false // ✅ Ensures UI updates properly
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


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance Records") }
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
                            AttendanceCard(date)
                        }
                    }
                }
            }
        }
    )

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview(showBackground = true)
fun ViewPreview() {
    Otp_class_appTheme {
        AttendanceViewScreen(LocalContext.current)
    }
}
