package com.harekrishna.otpClasses.ui.registeration

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harekrishna.otpClasses.data.models.CallingReportPOJO

data class Student(val name: String, val phone: String, var status: String)

@RequiresApi(Build.VERSION_CODES.O)
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
                StudentListItem(
                    student = registrationReport,
                    onStudentUpdated = { updatedReport: CallingReportPOJO ->
                        // Handle student status update
                        viewModel.updateStudentStatus(updatedReport.phone, updatedReport.status,updatedReport.isInvited, updatedReport.feedback)
                    },
                    onMessageIconClicked = { report: CallingReportPOJO ->
                        viewModel.sendWhatsAppMessage(context,report.phone, report.name)
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListItem(
    student: CallingReportPOJO,
    onStudentUpdated: (CallingReportPOJO) -> Unit,
    onMessageIconClicked: (CallingReportPOJO) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Card with rounded corners, padding, and shadow
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .border(
                width = 1.dp, // Thin border width
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), // Subtle border color
                shape = MaterialTheme.shapes.medium // Rounded corners for the border
            ), // Increased horizontal padding for better alignment
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Adds a subtle shadow
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Subtle background color
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Space between rows
        ) {
            // Top row: Name, phone, status, and icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // Space out items evenly
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
                            color = if (student.isInvited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary,
                            textAlign = TextAlign.Center // Centers the text horizontally
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

                // Send message and Phone icons
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send, // Use an appropriate send message icon
                        contentDescription = "Send Message",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                onStudentUpdated(student.copy(isInvited = true))
                                onMessageIconClicked(student)
                            }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = MaterialTheme.colorScheme.primary,
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

            // Feedback Text: Full width, italic, and more space
            Text(
                text = "\"${student.feedback.ifEmpty { "No Feedback Yet" }}\"",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Light,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp // Slightly smaller for feedback
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), // Slightly faded for feedback
                modifier = Modifier.fillMaxWidth(), // Feedback takes full width
                maxLines = 3, // Allow up to 3 lines of feedback
                overflow = TextOverflow.Ellipsis // Add ellipsis if feedback is too long
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
    var otherReason by remember { mutableStateOf(TextFieldValue("")) }
    var feedback by remember { mutableStateOf(TextFieldValue("")) }  // New feedback variable

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

                Row( verticalAlignment = Alignment.CenterVertically ){
                    RadioButton(
                        selected = selectedStatus == "❗",
                        onClick = { selectedStatus = "❗" })
                    Text(text = "❗")
                }
                if (selectedStatus == "❗") {
                    TextField(
                        value = otherReason,
                        onValueChange = { otherReason = it },
                        label = { Text(text = "Reason") }
                    )
                }

                // Feedback section
                TextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text(text = "Feedback") },  // Feedback label
                    placeholder = { Text(text = "Enter your feedback...") }  // Optional placeholder
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                // Save the updated student status
                val formattedStatus: String = when (selectedStatus) {
                    "No" -> "No, ${reason.text}"
                    "❗" -> "❗, ${otherReason.text}"
                    else -> selectedStatus
                }


                onSave(student.copy(status = formattedStatus, feedback = feedback.text ) )
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
fun previewCallingListItem(){
    StudentListItem(CallingReportPOJO("9532945033","Rohit","Will try",4,"2024-03-01",true,true,"I am busy in my studies, not find time.", "poor", "he is materialistic goals."), {}, {})
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun previewCallingScreen(){
    CallingListScreen("2024-3-3",viewModel() )
}
