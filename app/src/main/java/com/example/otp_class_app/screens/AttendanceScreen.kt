package com.example.otp_class_app.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.otp_class_app.api.ApiService
import com.example.otp_class_app.models.StudentPOJO
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen() {
    var students by remember { mutableStateOf<List<StudentPOJO>>(emptyList()) }
    var filteredStudents by remember { mutableStateOf<List<StudentPOJO>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStudent by remember { mutableStateOf<StudentPOJO?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Fetch students on screen load
    LaunchedEffect(Unit) {
        scope.launch {
            ApiService.getStudents { studentList ->
                students = studentList ?: emptyList()
                filteredStudents = students
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Attendance") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search Bar
            BasicTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    filteredStudents = students.filter { it.phone.contains(query, ignoreCase = true) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Student List
            LazyColumn {
                items(filteredStudents) { student ->
                    StudentItem(student) {
                        selectedStudent = student
                        showDialog = true
                    }
                }
            }
        }
    }

    // Student Details Dialog
    if (showDialog && selectedStudent != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Student Details") },
            text = {
                Column {
                    Text("Name: ${selectedStudent?.name}")
                    Text("Phone: ${selectedStudent?.phone}")
                    // Add other details here
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val studentId = "${selectedStudent?.name?.replace(" ", "")}${selectedStudent?.phone}"
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    scope.launch {
                        ApiService.postAttendance(
                            studentId = studentId,
                            date = date,
                            onSuccess = {
                                showDialog = false
                                showConfirmationDialog = true
                            },
                            onError = { e ->
                                // Handle error
                                e.printStackTrace()
                            }
                        )
                    }
                }) {
                    Text("Mark Present")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation Dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Congratulations") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Check, contentDescription = "Success", tint = Color.Green)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gaurange ${selectedStudent?.name} Prabhu Ji")
                }
            },
            confirmButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun StudentItem(student: StudentPOJO, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation =  CardDefaults.cardElevation()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Name: ${student.name}", style = MaterialTheme.typography.headlineSmall)
            Text(text = "Phone: ${student.phone}", style = MaterialTheme.typography.bodySmall)
            // Add other student details here
        }
    }
}
