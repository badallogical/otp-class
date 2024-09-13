package com.example.otp_class_app.screens

import android.content.Context
import android.content.Intent
import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.otp_class_app.R
import com.example.otp_class_app.api.ApiService
import com.example.otp_class_app.api.ApiService.fetchStudentReportByFacilitator
import com.example.otp_class_app.api.ApiService.fetchStudentsByFacilitator
import com.example.otp_class_app.api.ApiService.postStudentReport
import com.example.otp_class_app.models.ReportDTO
import com.example.otp_class_app.models.StudentDTO
import com.example.otp_class_app.models.StudentPOJO
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportingScreen(context: Context, navController: NavController) {
    // State to handle the selected facilitator
    var selectedFacilitator by remember { mutableStateOf<String?>(null) }

    // State to handle dropdown visibility
    var showDropdown by remember { mutableStateOf(false) }
    var isDataFetched by remember { mutableStateOf(false) }

    // State to handle fetching and displaying student reports
    var studentReports by remember { mutableStateOf<List<ReportDTO>>(emptyList()) }
    var studentsList by remember { mutableStateOf<List<StudentPOJO>>(emptyList()) }
    var trackingStudentList by remember { mutableStateOf<List<StudentPOJO>>(emptyList()) }

    var filterstudentReports by remember { mutableStateOf<List<ReportDTO>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }

    var isDialogVisible by remember { mutableStateOf(false) }

    // List of facilitators
    val facilitators = listOf(
        "H.G Sadhu Chaitanya Prabhu",
        "H.G Seva Actyute Prabhu",
        "H.G Rajiv Lochan Prabhu",
        "H.G Madhu Smita Prabhu"
    )

    fun filterReports(query: String, studentList: List<ReportDTO>?) {
        if( query == "all"){
            filterstudentReports = studentReports
        }else {
            filterstudentReports = studentList?.filter {
                it.name.contains(query, ignoreCase = true)
            } ?: emptyList()
        }
    }


    // Top App Bar with share button
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reporting") },
                actions = {
                    // Add "+" button
                    IconButton(onClick = { isDialogVisible = true }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Devotees")
                    }

//                    IconButton(onClick = {
//                        // Handle sharing
//                        shareFormLink(context)
//                    }) {
//                        Icon(
//                            imageVector = Icons.Filled.Share,
//                            contentDescription = "Share"
//                        )
//                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = padding.calculateTopPadding()
                )
            ) {

                if( !isDataFetched ) {
                    // Box for the dropdown menu
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

                        // OutlinedTextField for displaying the selected facilitator
                        OutlinedTextField(
                            value = selectedFacilitator ?: "Select Facilitator",
                            onValueChange = { /* No-op */ },
                            label = { Text("Facilitator") },
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.background,
                                    shape = MaterialTheme.shapes.small
                                )
                                .onGloballyPositioned { coordinates ->
                                    textFieldSize = coordinates.size.toSize()
                                },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Dropdown",
                                    modifier = Modifier.clickable { showDropdown = !showDropdown }
                                )
                            }
                        )

                        // DropdownMenu to display list of facilitators
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false },
                            modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                        ) {
                            facilitators.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(text = option) },
                                    onClick = {
                                        selectedFacilitator = option
                                        showDropdown = false
//                                    // Fetch student reports based on selected facilitator
//                                    fetchStudentReports(option, onSuccess = {
//                                        studentReports = it
//                                        isLoading = false
//                                    }, onError = {
//                                        errorMessage = it
//                                        isLoading = false
//                                    })
                                    }
                                )
                            }
                        }
                    }

                    // Fetch button
                    Button(
                        onClick = {
                            selectedFacilitator?.let { facilitator ->

                                // Launch a coroutine to fetch data
                                CoroutineScope(Dispatchers.Main).launch {
                                    isLoading = true
                                    val reports = fetchStudentReportByFacilitator(facilitator)
                                    studentReports = reports // Store the fetched reports
                                    filterReports("all", studentReports)

                                    studentsList = fetchStudentsByFacilitator(facilitator)
                                    isLoading = false // Reset loading state
                                    //onReportFetched(reports) // Notify parent or handle data

                                    isDataFetched = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("Fetch")
                    }
                }

                // Show the Add Devotees Dialog
                if (isDialogVisible) {
                    AddDevoteesDialog(
                        studentList = studentsList,
                        onDismiss = { isDialogVisible = false },
                        onConfirm = { selectedStudents ->
                            trackingStudentList = selectedStudents // Save the selected students
                            CoroutineScope(Dispatchers.IO).launch {
                                trackingStudentList.forEach { student ->
                                    var report = ReportDTO(student.name, student.phone, student.facilitator, student.batch)
                                    postStudentReport(report)
                                }
                            }

                            isDialogVisible = false // Close the dialog
                        }
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { newQuery ->
                        searchQuery = newQuery
                        filterReports(
                            searchQuery,
                            studentReports
                        ) // Update the filtered list on search query change
                    },
                    label = { Text("Search by Name") },
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

                // Show loading indicator
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                // Show error message
                if (errorMessage != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            errorMessage ?: "An error occurred",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Check if the studentReports list is empty
                if (filterstudentReports.isEmpty()) {
                    // Show a message if there are no reports
                    NoStudentFoundMessage()
                } else {
                    // Show student reports in a LazyColumn
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filterstudentReports) { report ->
                            ReportItem(report, onClick = { student ->
                                // Navigate to EditStudentReportScreen
                                val reportJson = Gson().toJson(report) // Serialize your ReportDTO
                                navController.navigate("edit_report/$reportJson")
                            })
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun AddDevoteesDialog(
    studentList: List<StudentPOJO>,
    onDismiss: () -> Unit,
    onConfirm: (List<StudentPOJO>) -> Unit
) {
    val selectedStudents = remember { mutableStateListOf<StudentPOJO>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add devotees to track") },
        text = {
            LazyColumn {
                items(studentList) { student ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${student.name} - ${student.phone}",
                            modifier = Modifier.weight(1f)
                        )
                        Checkbox(
                            checked = selectedStudents.contains(student),
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    selectedStudents.add(student)
                                } else {
                                    selectedStudents.remove(student)
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedStudents) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NoStudentFoundMessage() {
    // Centered text indicating no students were found
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No Devotee found",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// Function to fetch student reports based on facilitator
//fun fetchStudentReports(
//    facilitator: String,
//    onSuccess: (List<ReportDTO>) -> Unit,
//    onError: (String) -> Unit
//) {
//    // Example implementation (replace with actual API call or data source)
//    CoroutineScope(Dispatchers.IO).launch {
//        try {
//            // Simulate a network call or database query
//            delay(1000) // Simulate network delay
//
//            // Example student reports (replace with actual data fetching logic)
//            val reports = listOf(
//                ReportDTO(name = "John Doe", chanting = 20, lastMeetingDate = "2024-09-01"),
//                ReportDTO(name = "Jane Smith", chanting = 25, lastMeetingDate = "2024-08-30")
//            )
//            withContext(Dispatchers.Main) {
//                onSuccess(reports)
//            }
//        } catch (e: Exception) {
//            withContext(Dispatchers.Main) {
//                onError(e.message ?: "An error occurred")
//            }
//        }
//    }
//}

// Function to handle sharing form link
fun shareFormLink(context: Context) {
    val link =
        "https://docs.google.com/spreadsheets/d/11xLvKOnIUb88yd4nzW4q7-N-4IO08GJYHqyobbYSpdo/edit?gid=211086684#gid=211086684"
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, link)
    }
    val chooser = Intent.createChooser(intent, "Share Form Link")
    context.startActivity(chooser)
}

// Composable for displaying individual student report
@Composable
fun ReportItem(report: ReportDTO, onClick: (ReportDTO) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick(report) },
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Name: ${report.name}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Chanting Rounds: ${report.chanting}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Last Meeting Date: ${report.lastMeetingDate}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Function to handle form link sharing logic
fun shareOrCreateFormLink(
    context: Context,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    // Call Google Apps Script to check if form exists for the sheet
    val formExists = checkIfFormExists() // Dummy function to check for form

    if (formExists) {
        // Fetch the existing form link
        val existingFormLink = fetchExistingFormLink() // Fetch link from Google Sheet or database
        onSuccess(existingFormLink)
    } else {
        // Create new form if it doesn't exist
//        createNewForm({ newFormLink ->
//            onSuccess(newFormLink)
//        }, { error ->
//            onError("Failed to create the form: $error")
//        })
    }
}

// Dummy function to check if a form already exists for the sheet
fun checkIfFormExists(): Boolean {
    // Replace this with actual logic to check if the form is already created
    return false // For now, assume form doesn't exist
}

// Dummy function to fetch existing form link
fun fetchExistingFormLink(): String {
    // Replace this with actual logic to fetch the existing form link
    return "https://docs.google.com/forms/d/xxxxxx"
}


@Composable
fun EditReportScreen(
    reportData: ReportDTO,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(reportData.name) }
    var contact by remember { mutableStateOf(reportData.contact) }
    var facilitator by remember { mutableStateOf(reportData.facilitator) }
    var classlevel by remember { mutableStateOf(reportData.classlevel) }
    var chanting by remember { mutableStateOf(reportData.chanting) }
    var wrh by remember { mutableStateOf(reportData.wrh) }
    var book by remember { mutableStateOf(reportData.book) }
    var whh by remember { mutableStateOf(reportData.whh) }
    var active by remember { mutableStateOf(reportData.active) }
    var fourRegPrinciples by remember { mutableStateOf(reportData.fourRegPrinciples) }
    var percentageRegularity by remember { mutableStateOf(reportData.percentageRegularity) }
    var meetsFacilitator by remember { mutableStateOf(reportData.meetsFacilitator) }
    var sewa by remember { mutableStateOf(reportData.sewa) }
    var seriousness by remember { mutableStateOf(reportData.seriousness) }
    var attendsDailyEveningClass by remember { mutableStateOf(reportData.attendsDailyEveningClass) }
    var reasonForNotAttending by remember { mutableStateOf(reportData.reasonForNotAttending.orEmpty()) }
    var noOfNightStay by remember { mutableStateOf(reportData.noOfNightStay) }
    var remarks by remember { mutableStateOf(reportData.remarks.orEmpty()) }
    var lastMeetingDate by remember { mutableStateOf(reportData.lastMeetingDate.orEmpty()) }

    // States for dropdown menus
    var showDropdownFacilitator by remember { mutableStateOf(false) }
    var showDropdownClassLevel by remember { mutableStateOf(false) }
    var textFieldSizeFacilitator by remember { mutableStateOf(Size.Zero) }
    var textFieldSizeClassLevel by remember { mutableStateOf(Size.Zero) }

    val facilitators = listOf("H.G Sadhu", "H.G Rajiva", "H.G Sewa Actyute Prabhu")
    val classLevels = listOf("DYS", "TSSV", "VL2")

    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Make the column scrollable
    ) {
        Text("Edit Report", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = contact,
            onValueChange = { contact = it },
            label = { Text("Contact") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Facilitator dropdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = facilitator,
                onValueChange = { /* No-op */ },
                label = { Text("Facilitator") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.background,
                        shape = MaterialTheme.shapes.small
                    )
                    .onGloballyPositioned { coordinates ->
                        textFieldSizeFacilitator = coordinates.size.toSize()
                    },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown",
                        modifier = Modifier.clickable {
                            showDropdownFacilitator = !showDropdownFacilitator
                        }
                    )
                }
            )

            DropdownMenu(
                expanded = showDropdownFacilitator,
                onDismissRequest = { showDropdownFacilitator = false },
                modifier = Modifier.width(with(LocalDensity.current) { textFieldSizeFacilitator.width.toDp() })
            ) {
                facilitators.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            facilitator = option
                            showDropdownFacilitator = false
                        }
                    )
                }
            }
        }

        // Class Level dropdown
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = classlevel,
                onValueChange = { /* No-op */ },
                label = { Text("Class Level") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.background,
                        shape = MaterialTheme.shapes.small
                    )
                    .onGloballyPositioned { coordinates ->
                        textFieldSizeClassLevel = coordinates.size.toSize()
                    },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown",
                        modifier = Modifier.clickable {
                            showDropdownClassLevel = !showDropdownClassLevel
                        }
                    )
                }
            )

            DropdownMenu(
                expanded = showDropdownClassLevel,
                onDismissRequest = { showDropdownClassLevel = false },
                modifier = Modifier.width(with(LocalDensity.current) { textFieldSizeClassLevel.width.toDp() })
            ) {
                classLevels.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            classlevel = option
                            showDropdownClassLevel = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Other fields
        TextField(
            value = chanting.toString(),
            onValueChange = { chanting = (it.toIntOrNull() ?: chanting).toString() },
            label = { Text("Chanting") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = wrh,
            onValueChange = { wrh = (it.toDoubleOrNull() ?: wrh).toString() },
            label = { Text("WRH") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = book,
            onValueChange = { book = it },
            label = { Text("Book") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = whh.toString(),
            onValueChange = { whh = (it.toDoubleOrNull() ?: whh).toString() },
            label = { Text("WHH") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Active")
            Switch(
                checked = active.toBoolean(),
                onCheckedChange = { active = it.toString() }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Four Reg Principles")
            Switch(
                checked = fourRegPrinciples.toBoolean(),
                onCheckedChange = { fourRegPrinciples = it.toString() }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Attends Daily Evening Class")
            Switch(
                checked = attendsDailyEveningClass.toBoolean(),
                onCheckedChange = { attendsDailyEveningClass = it.toString() }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = percentageRegularity.toString(),
            onValueChange = {
                percentageRegularity = (it.toDoubleOrNull() ?: percentageRegularity).toString()
            },
            label = { Text("Percentage Regularity") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = meetsFacilitator,
            onValueChange = { meetsFacilitator = it },
            label = { Text("Meets Facilitator") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = sewa,
            onValueChange = { sewa = it },
            label = { Text("Sewa") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = seriousness,
            onValueChange = { seriousness = it },
            label = { Text("Seriousness") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = noOfNightStay,
            onValueChange = { noOfNightStay = it },
            label = { Text("No. of Night Stay") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = remarks,
            onValueChange = { remarks = it },
            label = { Text("Remarks") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = lastMeetingDate,
            onValueChange = { lastMeetingDate = it },
            label = { Text("Last Meeting Date") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp)) // Add extra spacing before buttons

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (!isLoading) {
                        // Launch a coroutine to handle the save operation
                        CoroutineScope(Dispatchers.Main).launch {
                            isLoading = true
                            val report = ReportDTO(
                                name,
                                contact,
                                facilitator,
                                classlevel,
                                chanting,
                                wrh,
                                book,
                                whh,
                                active,
                                fourRegPrinciples,
                                percentageRegularity,
                                meetsFacilitator,
                                sewa,
                                seriousness,
                                attendsDailyEveningClass,
                                reasonForNotAttending,
                                noOfNightStay,
                                remarks,
                                lastMeetingDate
                            )

                            var isSuccessfull = withContext(Dispatchers.IO) {
                                postStudentReport(report);
                            }

                            isLoading = false;
                            showSuccessDialog = isSuccessfull
                        }
                    }
                },
                enabled = !isLoading // Disable the button while loading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save")
                }
            }

            Button(onClick = onCancel) {
                Text("Cancel")
            }

            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { showSuccessDialog = false },
                    title = { Text("Success") },
                    text = { Text("Gaurange $name Prabhu Ji, Hari Bol 🙏") },
                    confirmButton = {
                        TextButton(onClick = { showSuccessDialog = false }) {
                            Text("Hari Bol")
                        }
                    }
                )

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEditReportScreen() {
    MaterialTheme {
        EditReportScreen(
            reportData = ReportDTO(
                name = "John Doe",
                contact = "1234567890",
                facilitator = "H.G Sadhu",
                classlevel = "DYS",
                chanting = "20",
                wrh = "5.0",
                book = "Bhagavad Gita",
                whh = "2.0",
                active = "true",
                fourRegPrinciples = "true",
                percentageRegularity = "95.0",
                meetsFacilitator = "H.G Sadhu",
                sewa = "Daily",
                seriousness = "High",
                attendsDailyEveningClass = "true",
                reasonForNotAttending = "null",
                noOfNightStay = "3",
                remarks = "Good progress",
                lastMeetingDate = "2024-09-01"
            ),
            onCancel = { /* Handle cancel */ }
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview(showBackground = true)
fun ReportScreenPreview() {
    MaterialTheme {
        ReportingScreen(context = LocalContext.current, rememberNavController())

    }
}