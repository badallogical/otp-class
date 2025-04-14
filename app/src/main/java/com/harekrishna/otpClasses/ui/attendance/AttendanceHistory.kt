package com.harekrishna.otpClasses.ui.attendance

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.harekrishna.otpClasses.R
import com.harekrishna.otpClasses.ui.theme.Otp_class_appTheme
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHistoryScreen(
    context: Context,
    navController: NavController,
    viewModel: AttendanceViewModel
) {
    val uiState by viewModel.attendanceHistoryUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance Records") },
                actions = {
                    if (uiState.isLoadingRemoteAttendance) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(24.dp), // Smaller size
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_refresh_24),
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(24.dp)
                                .clickable {
                                    viewModel.onRefreshAttendanceHistory()
                                }
                        )
                    }
                }
            )
        }
        ,
        content = { padding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(padding)) {
                if (uiState.historyList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No attendance records found.")
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.historyList) { (date, count) ->
                            AttendanceSummaryCard(date = date, count = count, onClick = {
                                Log.d("click", "clicked")
                                    navController.navigate("attendance_details/$date")
                            })
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun AttendanceSummaryCard(date: String, count: Int, onClick : () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Date: $date", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Attendance Count: $count", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun AttendanceHistoryPreview() {
    Otp_class_appTheme {
//        AttendanceHistoryScreen(LocalContext.current)
    }
}
