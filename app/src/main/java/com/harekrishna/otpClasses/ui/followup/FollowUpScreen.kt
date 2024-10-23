package com.harekrishna.otpClasses.ui.followup

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harekrishna.otpClasses.R

@Composable
fun FollowUpScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Attendee List", "Summary")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> AttendeeListTab()  // List of students with attendance > 0
            1 -> SummaryTab()        // Pie chart summary
        }
    }
}

@Composable
fun AttendeeListTab() {
    // State to hold the list of students
    val students = remember { mutableStateOf(mutableListOf<StudentDTOO>()) }

    // State to hold the filtered and sorted list of students
    var filteredStudents by remember { mutableStateOf(students.value) }
    var sortedStudents by remember { mutableStateOf(students.value) }

    // Update the contents of the `students` list with filtered and sorted students
    LaunchedEffect(Unit) {
        // Load the student list (you can replace this with your actual data loading logic)
        students.value = getFilteredAndSortedStudents().toMutableList()
        filteredStudents = students.value
        sortedStudents = filteredStudents
    }

    Column {
        // Pass filter and sort logic as lambdas
        FilterAndSortOptions(
            onFilterSelected = { filter ->
                filteredStudents = filterStudents(filter, students.value)
                sortedStudents =
                    sortStudents("None", filteredStudents) // Apply sorting after filtering
            },
            onSortSelected = { sort ->
                sortedStudents = sortStudents(sort, filteredStudents) // Sort after filtering
            }
        )

        // Display the list of attendees or an empty state if no students exist
        if (sortedStudents.isEmpty()) {
            EmptyState(
                icon = R.drawable.baseline_search_24,  // Replace with your actual icon
                message = "No students found with attendance."
            )
        } else {
            // Display the list of students if it's not empty
            LazyColumn {
                items(sortedStudents) { student ->
                    AttendeeListItem(student, {}, {})
                }
            }
        }
    }
}

// Filter logic function
fun filterStudents(filter: String, students: List<StudentDTOO>): MutableList<StudentDTOO> {
    return when (filter) {
        "Present" -> students.filter { it.attendanceStatus == "Present" }.toMutableList()
        "Absent" -> students.filter { it.attendanceStatus == "Absent" }.toMutableList()
        "Late" -> students.filter { it.attendanceStatus == "Late" }.toMutableList()
        else -> students.toMutableList() // "All"
    }
}

// Sort logic function
fun sortStudents(sort: String, filteredStudents: List<StudentDTOO>): MutableList<StudentDTOO> {
    return when (sort) {
        "Name" -> filteredStudents.sortedBy { it.name }.toMutableList()
        "Date" -> filteredStudents.sortedBy { it.attendanceCount }.toMutableList()
        "Attendance" -> filteredStudents.sortedBy { it.attendanceCount }.toMutableList()
        else -> filteredStudents.toMutableList() // "None"
    }
}


@Composable
fun FilterAndSortOptions(
    onFilterSelected: (String) -> Unit,
    onSortSelected: (String) -> Unit,
    filterOptions: List<String> = listOf(
        "All",
        "Last Present",
        "Last Absent",
        "Last 4 Weeks Present",
        "Last 4 Weeks Absent"
    ),
    sortOptions: List<String> = listOf("None", "Name", "Date", "Attendance")
) {
    // States for expanded dropdowns
    var isFilterDropdownExpanded by remember { mutableStateOf(false) }
    var isSortDropdownExpanded by remember { mutableStateOf(false) }

    // States for selected options
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedSort by remember { mutableStateOf("None") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // Adjust padding for cleaner layout
        horizontalArrangement = Arrangement.Start, // Align items to the start
        verticalAlignment = Alignment.CenterVertically // Align text and icons vertically
    ) {
        // Filter Dropdown
        Box(
            modifier = Modifier
                .clickable { isFilterDropdownExpanded = true }
                .padding(end = 16.dp) // Add some spacing between filter and sort
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Filter: $selectedFilter", modifier = Modifier.padding(end = 8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Filter Dropdown"
                )
            }

            DropdownMenu(
                expanded = isFilterDropdownExpanded,
                onDismissRequest = { isFilterDropdownExpanded = false }
            ) {
                filterOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            selectedFilter = option
                            isFilterDropdownExpanded = false
                            onFilterSelected(option)
                        }
                    )
                }
            }
        }

        // Sort Dropdown
        Box(
            modifier = Modifier
                .clickable { isSortDropdownExpanded = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Sort: $selectedSort", modifier = Modifier.padding(end = 8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Sort Dropdown"
                )
            }

            DropdownMenu(
                expanded = isSortDropdownExpanded,
                onDismissRequest = { isSortDropdownExpanded = false }
            ) {
                sortOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            selectedSort = option
                            isSortDropdownExpanded = false
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

// Replace with actual logic for sorting and filtering
fun getFilteredAndSortedStudents(): List<StudentDTOO> {
    // Return filtered and sorted list of students
    return listOf(
        StudentDTOO("John Doe", "1234567890", 5, "Called", " I will come after diwali"),
        StudentDTOO("Jane Smith", "0987654321", 3, "Not Called", "I am not interested")
    )
}

@Composable
fun FilterAndSortOptions() {
    // Replace with actual filter logic
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Filter and sort dropdowns can be implemented here
        // Example: DropdownMenu() for filtering and sorting options
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendeeListItem(
    student: StudentDTOO,
    onStudentUpdated: (StudentDTOO) -> Unit,
    onMessageIconClicked: (StudentDTOO) -> Unit
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

                // Status Chip centered
                AssistChip(
                    onClick = { showDialog = true },
                    label = {
                        Text(
                            text = student.attendanceStatus.split(",").firstOrNull()?.trim()
                                ?: student.attendanceStatus,
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

                Spacer(modifier = Modifier.width(4.dp))

                // Attendance Count in Circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.attendanceCount.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Middle row: Status, Attendance Stars, and Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Switch(
                    checked = true,
                    onCheckedChange = {
//                        onStudentUpdated(student.copy(isInvited = it))
                    }
                )





                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier
//                            .size(24.dp)
//                            .clickable {
//                                val intent = Intent(Intent.ACTION_DIAL).apply {
//                                    data = Uri.parse("tel:${student.phone}")
//                                }
//                                context.startActivity(intent)
//                                showDialog = true
//                            }
                    )


            }

            // Switch On/Off
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
                                    if (index < student.attendanceCount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                                .padding(4.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
//                                onStudentUpdated(student.copy(isInvited = true))
//                                onMessageIconClicked(student)
                        }
                )


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

//    // Dialog to show calling status
//    if (showDialog) {
//        showCallingStatusDialog(
//            student = student,
//            onDismiss = { showDialog = false },
//            onSave = { updatedStudent: CallingReportPOJO ->
//                onStudentUpdated(updatedStudent)
//                showDialog = false
//            }
//        )
//    }
}


fun makeCall(phone: String) {
    // Intent to make a phone call
}

data class StudentDTOO(
    val name: String,
    val phone: String,
    val attendanceCount: Int,
    val attendanceStatus: String,
    val feedback: String
)

// Sample implementation for the SummaryTab
@Composable
fun SummaryTab() {
    // Implement the pie chart summary or any other content here
    Text("Summary will be displayed here.")
}

@Preview
@Composable
fun PreviewFollowUpScreen() {
    FollowUpScreen()
}
