package com.example.otp_class_app.ui.registeration

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

data class Student(val name: String, val phone: String, var status: String)

@Composable
fun CallingListScreen(viewModel: CallingListViewModel = viewModel()) {

    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Registrations",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(4f) // Take up as much space as possible
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(uiState.registrations) { registrationReport ->
                StudentListItem(registrationReport) { updatedRegistrationReport ->
                    // Handle when the student status is updated
                    viewModel.updateStudentStatus(updatedRegistrationReport)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListItem(student: RegistrationReport, onStudentUpdated: (RegistrationReport) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = student.name, style = MaterialTheme.typography.titleLarge)
                Text(text = student.phone, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.weight(1f))

            AssistChip(
                onClick = { showDialog = true },
                label = {
                    Text(
                        text = student.status,
                        fontSize = 12.sp, // Make the text smaller
                        style = MaterialTheme.typography.bodySmall // Use a thinner typography style
                    )
                },
                modifier = Modifier
                    .padding(2.dp) // Optional padding to reduce the size of the chip
                    .width(80.dp)
                    .height(28.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone" , modifier = Modifier.clickable(){
                // open Phone calling
                // Create an intent to open the phone dialer
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${student.phone}")
                }
                context.startActivity(intent)
                showDialog = true
            })
        }

    }

    if (showDialog) {
        showCallingStatusDialog(
            student = student,
            onDismiss = { showDialog = false },
            onSave = { updatedStudent ->
                onStudentUpdated(updatedStudent)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun showCallingStatusDialog(
    student: RegistrationReport,
    onDismiss: () -> Unit,
    onSave: (RegistrationReport) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(student.status) }
    var reason by remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Update Calling Status") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedStatus == "Yes",
                        onClick = { selectedStatus = "Yes" }
                    )
                    Text(text = "Yes")
                }

                Row( verticalAlignment = Alignment.CenterVertically ){
                    RadioButton(
                        selected = selectedStatus == "No",
                        onClick = { selectedStatus = "No" })
                    Text(text = "No")
                }
                if (selectedStatus == "No") {
                    TextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text(text = "Reason") }
                    )
                }

                Row( verticalAlignment = Alignment.CenterVertically ){
                    RadioButton(
                        selected = selectedStatus == "Will Try",
                        onClick = { selectedStatus = "Will Try" })
                    Text(text = "Will Try")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                // Save the updated student status
                onSave(student.copy(status = selectedStatus))
            }) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(text = "Cancel")
            }
        }
    )
}

@Preview
@Composable
fun previewCallingScreen(){
    CallingListScreen()
}
