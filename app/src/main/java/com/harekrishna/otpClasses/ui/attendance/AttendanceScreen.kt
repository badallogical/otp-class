package com.harekrishna.otpClasses.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.harekrishna.otpClasses.R
import com.harekrishna.otpClasses.data.models.StudentPOJO
import com.harekrishna.otpClasses.ui.attendance.AttendanceUiState
import com.harekrishna.otpClasses.ui.attendance.AttendanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AttendanceScreen(navController: NavController, viewModel: AttendanceViewModel = viewModel(factory = AttendanceViewModel.Factory)) {
    val uiState by viewModel.uiState.collectAsState()

    // Fetch students from API and set the initial list
    LaunchedEffect(Unit) {
        if (uiState.students.isEmpty()) {
            viewModel.fetchStudents()
            Log.d("Attendance", "Launched Called")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Row
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

            // Refresh Icon
            Icon(
                painter = painterResource(id = R.drawable.baseline_refresh_24),
                contentDescription = "Refresh Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .weight(0.5f)
                    .clickable {
                        viewModel.onRefresh() // Fetch students again when refresh icon is clicked
                    }
            )

            // Save & Sync Icon
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

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { newQuery ->
                viewModel.onSearchQueryChanged(newQuery) // Update the filtered list on search query change
            },
            label = { Text("Search by phone number") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = MaterialTheme.shapes.medium, // Round corners
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_search_24),
                    contentDescription = "Search Icon",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), // Set the IME action to "Done"
            keyboardActions = KeyboardActions(
                onDone = {
                    viewModel.onSearchQueryChanged(uiState.searchQuery) // Action when the "Done" button is pressed on the keyboard
                }
            ),
            singleLine = true, // Make the text field single-lined
            maxLines = 1 // Limit the text input to one line
        )

        // Loading State
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.filteredStudents.isEmpty()) {
            // No Students Found
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
                Button(onClick = { viewModel.onClickQuickRegisteration() }) {
                    Text("Quick Registration")
                }
            }
        } else {
            // List of Students
            LazyColumn {
                items(uiState.filteredStudents) { student ->
                    StudentItem(student = student) {
                        viewModel.onStudentItemClicked(student)
                    }
                }
            }
        }
    }

    // Dialog to mark attendance
    if (uiState.showDialog && uiState.selectedStudent != null) {
        AttendanceDialog(
            uiState = uiState,
            onSubmit = { student ->  // Correctly pass the student to the onSubmit lambda
                viewModel.postAttendance(student)  // Pass the student to postAttendance function
            },
            onDismiss = {
                viewModel.onDismissAttendanceDialog()  // Dismiss the dialog and clear the selected student
            }
        )
    }

    // Quick Registration Dialog
    if (uiState.showRegistrationDialog) {
        QuickRegistrationDialog(
            uiState,
            onDismiss = { viewModel.onDismissRegisterationDialog() },
            onRegister = { name, phone ->
                viewModel.onRegisterStudent(name, phone)
            }
        )
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AttendanceDialog(uiState: AttendanceUiState, onSubmit: (StudentPOJO) -> Unit, onDismiss: () -> Unit) {
    val student = uiState.selectedStudent
    val currentDate = "2024-01-07"

    if (currentDate == "") {
        // no class today
        NoClassesDialog()
        return
    }

    if (uiState.showCongratsAfterPosting) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("Gauranga ${student?.name} Prabhu Ji 🙏") },
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
                    if (student != null) {
                        Text("Name: ${student.name}")
                    }
                    if (student != null) {
                        Text("Phone: ${student.phone}")
                    }
                    if (student != null) {
                        Text("Batch: ${student.batch}")
                    }
                    if (student != null) {
                        Text("Facilitator: ${student.facilitator}")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!uiState.isPostingAttendance) {  // Prevent multiple submissions
                            if (student != null) {
                                onSubmit(student)
                            }
                        }
                    },
                    enabled = !uiState.isPostingAttendance // Disable the button while submitting
                ) {
                    if (uiState.isPostingAttendance) {
                        CircularProgressIndicator(
                            color = Color.White, // You can set the color based on your theme
                            modifier = Modifier.size(16.dp) // Adjust size as needed
                        )
                    } else {
                        Text("Hari Bol")
                    }
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

@Composable
fun NoClassesDialog() {
    // Remember the dialog state
    val openDialog = remember { mutableStateOf(true) }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = {
                // No title for this dialog
            },
            text = {
                // Message content in the dialog
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Today is No classes Prabhu Ji")
                    Text(text = "Hare Krishna 🙏")
                }
            },
            confirmButton = {
                // Button to dismiss the dialog
                TextButton(
                    onClick = { openDialog.value = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
}


@Composable
fun QuickRegistrationDialog(uiState: AttendanceUiState, onDismiss: () -> Unit, onRegister: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

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
                if (isClicked) {
                    Text(
                        text = "Phone number should be 10 digits",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (phone.length == 10) {
                        onRegister(name, phone)
                        onDismiss()
                    } else {
                        isClicked = true
                    }
                }
            ) {
                Text("Register")
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
@Composable
@Preview(showBackground = true)
fun AttendancePreview() {
    MaterialTheme {
        AttendanceScreen(navController = rememberNavController())
    }
}


@Composable
fun StudentItem(
    student: StudentPOJO,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular initials or profile placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.first().uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Student's name and phone details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Phone: ${student.phone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

//            // Attendance status or indicator icon
//            Icon(
//                painter = painterResource(id = R.drawable.baseline_done_outline_24),
//                contentDescription = "Attendance Marked",
//                tint = MaterialTheme.colorScheme.secondary
//            )
        }
    }
}

