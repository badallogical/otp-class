package com.example.otp_class_app.ui.screens

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.otp_class_app.api.ApiService
import com.example.otp_class_app.models.StudentPOJO
import com.example.otp_class_app.screens.StudentFormScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AttendanceScreen() {
    var students by remember { mutableStateOf<List<StudentPOJO>?>(null) }
    var filteredStudents by remember { mutableStateOf<List<StudentPOJO>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<StudentPOJO?>(null) }

    // Function to filter students based on the search query
    fun filterStudents(query: String, studentList: List<StudentPOJO>?) {
        filteredStudents = studentList?.filter {
            it.phone.contains(query, ignoreCase = true) ?: false
        } ?: emptyList()
    }

    // Fetch students from API and set the initial list
    LaunchedEffect(Unit) {
        val fetchedStudents = fetchStudentsFromApi()
        students = fetchedStudents
        isLoading = false
        filterStudents(searchQuery, students)
        Log.d("Attendnace screen", "Students fetched " + students.toString())
    }

    // Update filtered students whenever the search query changes
    LaunchedEffect(searchQuery) {
        filterStudents(searchQuery, students)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            value = searchQuery,
            onValueChange = { newQuery ->
                searchQuery = newQuery
                filterStudents(searchQuery, students) // Update the filtered list on search query change
            },
            label = { Text("Search by phone number") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = MaterialTheme.shapes.medium // Round corners
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(filteredStudents) { student ->
                    StudentItem(student = student) {
                        selectedStudent = student
                        showDialog = true
                    }
                }
            }
        }
    }

    // Dialog to mark attendance
    if (showDialog && selectedStudent != null) {
        AttendanceDialog(student = selectedStudent!!, onDismiss = { showDialog = false })
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AttendanceDialog(student: StudentPOJO, onDismiss: () -> Unit) {
    var isSubmitting by remember { mutableStateOf(false) }
    var showCongrats by remember { mutableStateOf(false) }

    if (showCongrats) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("Gauranga ${student.name} Prabhu Ji 🙏") },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Hari Bol")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("Mark Attendance") },
            text = {
                Column {
                    Text("Name: ${student.name}")
                    Text("Phone: ${student.phone}")
                    Text("Batch: ${student.batch}")
                    Text("Facilitator: ${student.facilitator}")
                   if (isSubmitting) {
                    // Center the CircularProgressIndicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                }
            },
            confirmButton = {
                Button(onClick = {
                    isSubmitting = true
                    CoroutineScope(Dispatchers.Main).launch {
                        //val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        val currentDate = "2024-01-07"
                        val success = ApiService.postAttendance(studentId = student.name + student.phone, date = currentDate)
                        isSubmitting = false
                        if (success) {
                            showCongrats = true
                        }
                    }
                }) {
                    Text("Hari Bol")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

suspend fun fetchStudentsFromApi(): List<StudentPOJO>? {
    return withContext(Dispatchers.IO) {
        // Simulate a network call
        ApiService.getStudents()
    }
}

@Composable
fun StudentItem(student: StudentPOJO, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = student.name, style = MaterialTheme.typography.headlineSmall)
            Text(text = student.phone, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview(showBackground = true)
fun AttendancePreview() {
    MaterialTheme {
        AttendanceScreen()
    }
}
