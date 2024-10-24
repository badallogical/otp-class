package com.harekrishna.otpClasses.ui.followup

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore.Downloads
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Shapes
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harekrishna.otpClasses.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harekrishna.otpClasses.data.models.AttendeeItem
import com.harekrishna.otpClasses.data.models.CallingReportPOJO

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FollowUpScreen(viewModel: FollowUpViewModel = viewModel( factory = FollowUpViewModel.Factory )) {

    val uiState by viewModel.uiState.collectAsState()

    val tabs = listOf("Attendee List", "Summary")

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Follow Up",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(4f) // Take up as much space as possible
            )
        }


        when (uiState.selectedTab) {
            0 -> AttendeeListTab( viewModel )  // List of students with attendance > 0
            1 -> SummaryTab()        // Pie chart summary
        }

        TabRow(selectedTabIndex = uiState.selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedTab == index,
                    onClick = { viewModel.onTabSelected(index) },
                    text = { Text(title) }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AttendeeListTab(viewModel: FollowUpViewModel) {

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column {
        // Pass filter and sort logic as lambdas
        FilterAndSortOptions(
            onFilterSelected = { filter ->
                viewModel.filterStudents(filter)
            },
            onSortSelected = { sort ->
                viewModel.sortStudents(sort)
            },
            onFilterDropDownSelected = { viewModel.onFitlerDropDownSelected() },
            onSortDropDownSelected = { viewModel.onSortDropDownSelected() },
            onDismissFilterDropDown = { viewModel.onDismissFilterDropDown() },
            onDismissSortDropDown = { viewModel.onDismissSortDropDown()},
            uiState = uiState
        )

        // Display the list of attendees or an empty state if no students exist
        if (uiState.filteredAttendee.isEmpty()) {
            EmptyState(
                icon = R.drawable.baseline_search_24,  // Replace with your actual icon
                message = "No devotee found with attendance."
            )
        } else {
            // Display the list of students if it's not empty
            LazyColumn {
                items(uiState.filteredAttendee) { student ->
                    AttendeeListItem(student, onStudentUpdated =  { updatedReport: AttendeeItem ->
                        // Handle student status update
                        viewModel.updateStudentStatus(updatedReport.phone, updatedReport.callingStatus,updatedReport.isInvited, updatedReport.isActive,updatedReport.feedback)
                    },  onMessageIconClicked = { report: AttendeeItem ->
                        viewModel.sendWhatsAppMessage(context,report.phone, report.name)
                    }, viewModel)
                }
            }
        }
    }
}




@Composable
fun FilterAndSortOptions(
    onFilterSelected: (String) -> Unit,
    onSortSelected: (String) -> Unit,
    onFilterDropDownSelected: () -> Unit,
    onSortDropDownSelected: () -> Unit,
    onDismissFilterDropDown: () -> Unit,
    onDismissSortDropDown: () -> Unit,
    uiState: FollowUpUiState
) {
    val filterOptions: List<String> = listOf(
    "All",
    "Last Present",
    "Last Absent",
    "Last 4 Weeks Present",
    "Last 4 Weeks Absent"
    )

    val sortOptions: List<String> = listOf("None", "Name", "Date", "Attendance")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // Adjust padding for cleaner layout
        horizontalArrangement = Arrangement.Start, // Align items to the start
        verticalAlignment = Alignment.CenterVertically // Align text and icons vertically
    ) {
        // Filter Dropdown
        Box(
            modifier = Modifier.width(150.dp)
                .clickable { onFilterDropDownSelected() }

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Filter: ${uiState.selectedFilter}", modifier = Modifier.padding(end = 8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Filter Dropdown"
                )
            }

            DropdownMenu(
                expanded = uiState.isFilterDropdownExpanded,
                onDismissRequest = { onDismissFilterDropDown() }
            ) {
                filterOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            onFilterSelected(option)
                        }
                    )
                }
            }
        }

        // Sort Dropdown
        Box(
            modifier = Modifier
                .clickable { onSortDropDownSelected() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Sort: ${uiState.selectedSort}", modifier = Modifier.padding(end = 8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Sort Dropdown"
                )
            }

            DropdownMenu(
                expanded = uiState.isSortDropdownExpanded,
                onDismissRequest = { onDismissSortDropDown() }
            ) {
                sortOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            onSortSelected(option)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Push content to the left, leaving space to the right


    }
}


@Composable
fun EmptyState(icon: Int, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center
        )
    }
}




@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendeeListItem(
    student: AttendeeItem,
    onStudentUpdated: (AttendeeItem) -> Unit,
    onMessageIconClicked: (AttendeeItem) -> Unit,
    viewModel: FollowUpViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Card with rounded corners, padding, and shadow
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.medium
            ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top row: Name, phone, status, and icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: Name and Phone
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = student.phone,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }



                Spacer(modifier = Modifier.width(4.dp))

                // Attendance Count in Circle
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary).border(width = 1.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.attendances.size.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Middle row: Status, Attendance Stars, and Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {




                    // Status Chip centered
                    AssistChip(
                        onClick = { showDialog = true },
                        label = {
                            Text(
                                text = student.callingStatus.split(",").firstOrNull()?.trim()
                                    ?: student.callingStatus,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSecondary,
                                textAlign = TextAlign.Center
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



                ThinSwitch(
                    checked = student.isActive,
                    onCheckedChange = { viewModel.onSwitchChange(student) },
                    modifier = Modifier.padding(8.dp)
                )

            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Attendance Stars
                Row {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(20.dp, 6.dp)
                                .background(
                                    if (index < student.attendances.size ) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                                .padding(4.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                }

                Row(){

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

                    Spacer( modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                onStudentUpdated(student.copy(isInvited = true))
                                onMessageIconClicked(student)
                            }
                    )
                }




            }

            // Feedback Text at the bottom
            Text(
                text = "\"${student.feedback}\"",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Light,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    // Dialog to show calling status
    if (showDialog) {
        showCallingStatusDialog(
            student = student,
            onDismiss = { showDialog = false },
            onSave = { updatedStudent: AttendeeItem ->
                onStudentUpdated(updatedStudent)
                showDialog = false
            }
        )
    }
}



@Composable
fun ThinSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
    val thumbColor = if( checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .width(36.dp)
            .height(16.dp) // Adjust the height to make it thin
            .clip(RoundedCornerShape(8.dp))
            .background(trackColor)
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp) // Padding for the thumb space
    ) {
        Box(
            modifier = Modifier
                .size(12.dp) // Size of the thumb
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun showCallingStatusDialog(
    student: AttendeeItem,
    onDismiss: () -> Unit,
    onSave: (AttendeeItem) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(student.callingStatus) }
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
                // Save the updated student status
                val formattedStatus: String = when (selectedStatus) {
                    "No" -> "No, ${reason.text}"
                    "❗" -> "❗, ${otherReason.text}"
                    else -> selectedStatus
                }

               onSave(student.copy(callingStatus = formattedStatus, feedback = feedback.text.ifEmpty { student.feedback }) )
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



// Sample implementation for the SummaryTab
@Composable
fun SummaryTab() {
    // Implement the pie chart summary or any other content here
    Text("Summary will be displayed here.")
}

@Preview
@Composable
fun PreviewFollowUpScreen() {

    val sampleAttendee= AttendeeItem(
        name = "John Doe",
        phone = "123-456-7890",
        callingStatus = "Called",
        attendances = listOf("2023-12-20", "2023-12-22", "2023-12-27"),
        isActive = true,
        isInvited = false,
        feedback = "Great session!",
        registrationDate = "2023-12-15"
    )

//    AttendeeListItem( sampleAttendee, {},{})

}
