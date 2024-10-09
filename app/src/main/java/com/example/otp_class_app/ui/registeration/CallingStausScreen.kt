package com.example.otp_class_app.ui.registeration

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.otp_class_app.MyApplication
import com.example.otp_class_app.data.models.CallingReportPOJO

data class Student(val name: String, val phone: String, var status: String)

@Composable
fun CallingListScreen(date : String, viewModel: CallingListViewModel = viewModel(factory = CallingListViewModel.Factory)) {

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit){
        Log.d("Calling Screen ", date)
        viewModel.getCallingRegistrations(date)
    }

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

            // Share button to share message to WhatsApp
            IconButton(
                onClick = {
                   viewModel.sendCallingReportMsg(context,date);
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share",
                    tint = Color.Gray
                )
            }

        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(uiState.registrations) { registrationReport ->
                StudentListItem(registrationReport) { updatedRegistrationReport : CallingReportPOJO ->
                    // Handle when the student status is updated
                    viewModel.updateStudentStatus(updatedRegistrationReport.phone, updatedRegistrationReport.status)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListItem(student: CallingReportPOJO, onStudentUpdated: (CallingReportPOJO) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Card with rounded corners, padding, and shadow
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Adds a subtle shadow
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Subtle background color
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start // Align items at the start of the row
        ) {
            // Left side: Name and Phone
            Column(
                modifier = Modifier.weight(1f), // Allow the name column to take up most of the space
                verticalArrangement = Arrangement.spacedBy(4.dp) // Space between name and phone
            ) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface, // Use onSurface color for better contrast
                    maxLines = 1, // Limit to one line
                    overflow = TextOverflow.Ellipsis // Add ellipsis if the name is too long
                )
                Text(
                    text = student.phone,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), // Slightly faded for phone number
                    maxLines = 1, // Limit to one line for phone
                    overflow = TextOverflow.Ellipsis // Add ellipsis if the phone number is too long
                )
            }

            // Status Chip
            AssistChip(
                onClick = { showDialog = true },
                label = {
                    Text(
                        text = student.status.split(",").firstOrNull()?.trim() ?: student.status,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                },
                modifier = Modifier
                    .padding(4.dp)
                    .width(80.dp)
                    .height(32.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Phone icon with green tint, fixed width
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Phone",
                tint = MaterialTheme.colorScheme.primary, // Green tint for the phone icon
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${student.phone}")
                        }
                        context.startActivity(intent)
                        showDialog = true
                    }
            )
        }
    }

    // Dialog to show calling status (when showDialog is true)
    if (showDialog) {
        showCallingStatusDialog(
            student = student,
            onDismiss = { showDialog = false },
            onSave = { updatedStudent: CallingReportPOJO ->
                onStudentUpdated(updatedStudent)
                showDialog = false
            }
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun showCallingStatusDialog(
    student: CallingReportPOJO,
    onDismiss: () -> Unit,
    onSave: (CallingReportPOJO) -> Unit
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
                onSave(student.copy(status = if( selectedStatus == "No") "No,${reason.text}" else selectedStatus ) )
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
    CallingListScreen("2024-3-3")
}
