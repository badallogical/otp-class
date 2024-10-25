package com.harekrishna.otpClasses.ui.registeration

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
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
fun CallingListScreen(
    date: String,
    viewModel: CallingListViewModel = viewModel(factory = CallingListViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Log.d("Calling Screen ", date)
        viewModel.getCallingRegistrations(date)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 16.dp)
    ) {
        // Header Section
        TopSection(
            date = date,
            onShareClick = { viewModel.sendCallingReportMsg(context, date) }
        )

        // Registration List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.registrations) { registrationReport ->
                StudentListItem(
                    student = registrationReport,
                    onStudentUpdated = { updatedReport ->
                        viewModel.updateStudentStatus(
                            updatedReport.phone,
                            updatedReport.status,
                            updatedReport.isInvited,
                            updatedReport.feedback
                        )
                    },
                    onMessageIconClicked = { report ->
                        viewModel.sendWhatsAppMessage(context, report.phone, report.name)
                    }
                )
            }
        }
    }
}

@Composable
private fun TopSection(
    date: String,
    onShareClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Registrations",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Student Info and Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Student Details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = student.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionButton(
                        icon = Icons.AutoMirrored.Filled.Send,
                        description = "Send Message",
                        onClick = {
                            onStudentUpdated(student.copy(isInvited = true))
                            onMessageIconClicked(student)
                        },
                        isActive = student.isInvited
                    )

                    Spacer( modifier = Modifier.width(4.dp))

                    ActionButton(
                        icon = Icons.Default.Phone,
                        description = "Call",
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${student.phone}")
                            }
                            context.startActivity(intent)
                            showDialog = true
                        }
                    )
                }
            }

            // Status Chip
            AssistChip(
                onClick = { showDialog = true },
                label = {
                    Text(
                        text = student.status.split(",").firstOrNull()?.trim() ?: student.status,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier.width(120.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (student.isInvited)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    labelColor = if (student.isInvited)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
            )

            // Feedback Section
            if (student.feedback.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "\"${student.feedback}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic,
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
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

@Composable
private fun ActionButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    isActive: Boolean = false
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(32.dp)
            .background(
                color = if (isActive)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (isActive)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
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
