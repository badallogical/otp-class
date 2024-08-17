package com.example.otp_class_app.ui.screens

import android.content.Context
import android.os.Build
import android.util.Log
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.otp_class_app.R
import com.example.otp_class_app.api.ApiService
import com.example.otp_class_app.api.AttendanceDataStore
import com.example.otp_class_app.models.AttendanceDTO
import com.example.otp_class_app.models.StudentDTO
import com.example.otp_class_app.models.StudentPOJO
import com.example.otp_class_app.ui.theme.OrangeLightColorScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AttendanceScreen(navController: NavController) {
    var students by remember { mutableStateOf<List<StudentPOJO>?>(null) }
    var filteredStudents by remember { mutableStateOf<List<StudentPOJO>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<StudentPOJO?>(null) }
    var showRegistrationDialog by remember { mutableStateOf(false) }


    // Declare the map to store attendance data
    val attendanceMap = mutableMapOf<String, MutableList<AttendanceDTO>>()

    // Create an instance of DataStore with the context
    val appContext = LocalContext.current.applicationContext

    // Function to filter students based on the search query
    fun filterStudents(query: String, studentList: List<StudentPOJO>?) {
        filteredStudents = studentList?.filter {
            it.phone.contains(query, ignoreCase = true) ?: false
        } ?: emptyList()
    }

    fun fetchAndFilterStudents() {
        CoroutineScope(Dispatchers.Main).launch {
            isLoading = true
            val fetchedStudents = fetchStudentsFromApi()
            students = fetchedStudents
            isLoading = false
            filterStudents(searchQuery, students)
            Log.d("Attendnace screen", "Students fetched " + students.toString())
        }
    }

    // Fetch students from API and set the initial list
    LaunchedEffect(Unit) {
        fetchAndFilterStudents()
    }

    // Update filtered students whenever the search query changes
    LaunchedEffect(searchQuery) {
        filterStudents(searchQuery, students)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Attendance",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(4f) // Take up as much space as possible
            )


            Icon(
                painter = painterResource(id = R.drawable.baseline_refresh_24), // Replace with your refresh icon resource
                contentDescription = "Refresh Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .weight(0.5f)
                    .clickable {
                        fetchAndFilterStudents() // Fetch students again when refresh icon is clicked
                    }
            )
            Icon(
                painter = painterResource(id = R.drawable.baseline_view_list_24),
                contentDescription = "Save & Sync",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .weight(1f)
                    .clickable {
                        navController.navigate("attendance_view")
                    }
            )
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newQuery ->
                searchQuery = newQuery
                filterStudents(
                    searchQuery,
                    students
                ) // Update the filtered list on search query change
            },
            label = { Text("Search by phone number") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = MaterialTheme.shapes.medium, // Round corners
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_search_24), // Replace with your search icon resource
                    contentDescription = "Search Icon",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.DarkGray, // Dark border when focused
                unfocusedBorderColor = Color.LightGray, // Light border when not focused
                cursorColor = MaterialTheme.colorScheme.primary // Cursor color
            ),
            singleLine = true, // Make the text field single-lined
            maxLines = 1 // Limit the text input to one line
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (filteredStudents.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "No Student found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { showRegistrationDialog = true }) {
                    Text("Quick Registration")
                }
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
        AttendanceDialog(student = selectedStudent!!,appContext, onDismiss = { showDialog = false })
    }

    // Quick Registration Dialog
    if (showRegistrationDialog) {
        QuickRegistrationDialog(
            onDismiss = { showRegistrationDialog = false },
            onRegister = { name, phone ->
                CoroutineScope(Dispatchers.Main).launch {
                    registerStudent(name, phone)
                    showRegistrationDialog = false
                }
            })
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AttendanceDialog(student: StudentPOJO, context: Context, onDismiss: () -> Unit) {
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
                    val currentDate = "2024-01-07"
                    val attendance = AttendanceDTO(student.phone, currentDate);
                    CoroutineScope(Dispatchers.Main).launch {
                        //val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        AttendanceDataStore.saveAttendance(attendance)
                        delay(500)
                        val success = true;
                        //val success = ApiService.postAttendance(attendance)
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

@Composable
fun QuickRegistrationDialog(onDismiss: () -> Unit, onRegister: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isClicked by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Quick Registration") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )
                if (isSubmitting) {
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
            Button(
                onClick = {
                    isClicked = !isClicked
                    isSubmitting = true
                    onRegister(name, phone)
                    isSubmitting = false
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isClicked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
            ) {
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

@RequiresApi(Build.VERSION_CODES.O)
suspend fun registerStudent(name: String, phone: String) {
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val student = StudentDTO(name, phone, "NA", "NA", "NA", "NA", currentDate)
    withContext(Dispatchers.IO) {
        // Simulate a network call to register the student
        ApiService.registerStudent(student)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview(showBackground = true)
fun AttendancePreview() {
    MaterialTheme {
        AttendanceScreen(navController = rememberNavController())
    }
}
