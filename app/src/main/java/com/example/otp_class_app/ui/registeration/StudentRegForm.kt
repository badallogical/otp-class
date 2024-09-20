package com.example.otp_class_app.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.otp_class_app.R
import com.example.otp_class_app.data.api.ApiService
import com.example.otp_class_app.data.models.StudentDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormScreen() {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var facilitator by remember { mutableStateOf("Select Facilitator") }
    var batch by remember { mutableStateOf("Select Batch") }
    var profession by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var showDropdownFacilitator by remember { mutableStateOf(false) }
    var showDropdownBatch by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var showStudentNotFoundDialog by remember { mutableStateOf(false) }
    var showDataFetchedToast by remember { mutableStateOf(false) }
    var updated by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var response: okhttp3.Response? = null

    val facilitators = listOf(
        "NA",
        "H.G Sadhu Chaitanya Prabhu",
        "H.G Seva Actyute Prabhu",
        "H.G Rajiv Lochan Prabhu"
    )
    val batches = listOf("DYS", "TSSV", "VL2")

    val icon1 = if (showDropdownFacilitator)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    val icon2 = if (showDropdownBatch)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    val backgroundColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.surfaceVariant // Dark theme background color
    } else {
        MaterialTheme.colorScheme.surface // Light theme background color
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // Custom header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hari Bol 🙏",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp).weight(1f)
            )

            Icon(
                painter = painterResource(id = R.drawable.baseline_edit_24), // Replace with your refresh icon resource
                contentDescription = "Edit Registration",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        // Edit
                        showPhoneDialog = true
                        updated = true
                    }
            )
        }

        // Name input
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .background(backgroundColor, shape = MaterialTheme.shapes.small)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            // Phone input field
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor, shape = MaterialTheme.shapes.small)
             )

            // Clickable text
            Text(
                text = "\uD83E\uDD1D Invite",
                color = MaterialTheme.colorScheme.primary, // Or any color you want for the text
                modifier = Modifier
                    .align(Alignment.CenterEnd) // Align to the right
                    .padding(16.dp) // Adjust padding for aesthetics
                    .clickable {
                        if (phone.length == 10 && phone.all { it.isDigit() }) {
                            sendWhatsAppMessage(
                                context,
                                phone,
                                name,
                                "95329450033"
                            )
                        } else {
                            Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            )
        }



        // Facilitator dropdown
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

            OutlinedTextField(
                value = facilitator,
                onValueChange = { /* No-op */ },
                label = { Text("Facilitator") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor, shape = MaterialTheme.shapes.small)
                    .onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size.toSize()
                    },
                trailingIcon = {
                    Icon(icon1, "contentDescription",
                        Modifier.clickable { showDropdownFacilitator = !showDropdownFacilitator })
                }
            )

            DropdownMenu(
                expanded = showDropdownFacilitator,
                onDismissRequest = { showDropdownFacilitator = false },
                modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
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

        // Batch dropdown
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

            OutlinedTextField(
                value = batch,
                onValueChange = { /* No-op */ },
                label = { Text("Batch") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor, shape = MaterialTheme.shapes.small)
                    .onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size.toSize()
                    },
                trailingIcon = {
                    Icon(icon2, "contentDescription",
                        Modifier.clickable { showDropdownBatch = !showDropdownBatch })
                }
            )
            DropdownMenu(
                expanded = showDropdownBatch,
                onDismissRequest = { showDropdownBatch = false },
                modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
            ) {
                batches.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            batch = option
                            showDropdownBatch = false
                        }
                    )
                }
            }
        }

        // Profession input
        OutlinedTextField(
            value = profession,
            onValueChange = { profession = it },
            label = { Text("Profession") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .background(backgroundColor, shape = MaterialTheme.shapes.small)
        )

        // Address input
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .background(backgroundColor, shape = MaterialTheme.shapes.small)
        )

        // Submit button
        Button(
            onClick = {

                if (!isSubmitting) {
                    if (!(phone.length == 10 && phone.all { it.isDigit() })) {
                        Toast.makeText(
                            context,
                            "Please enter a valid phone number",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    isSubmitting = true
                    val currentDate =
                        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val student = StudentDTO(
                        name,
                        phone,
                        facilitator,
                        batch,
                        profession,
                        address,
                        currentDate
                    )
                    CoroutineScope(Dispatchers.Main).launch {
                        response = ApiService.registerStudent(student, updated)
                        isSubmitting = false
                        showSuccessDialog = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Submit")
            }
        }

        // Success Dialog
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = { Text( if(response!!.isSuccessful) "Success" else "Failed")  },
                text = { Text(if( response!!.isSuccessful ) "Gaurange $name Prabhu Ji, Hari Bol 🙏" else "Hari Bol, ${response!!.message}") },
                confirmButton = {
                    TextButton(onClick = { showSuccessDialog = false }) {
                        Text("Hari Bol")
                    }
                }
            )
        }

        // Phone Number Dialog
        if (showPhoneDialog) {
            var phoneInput by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showPhoneDialog = false },
                title = { Text("Enter Phone Number") },
                text = {
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPhoneDialog = false
                            CoroutineScope(Dispatchers.Main).launch {
                                val student = withContext(Dispatchers.IO) {
                                    ApiService.findStudentByPhone(phoneInput)
                                }
                                if (student != null) {
                                    Log.d("Student form", student.toString())
                                    name = student.name
                                    phone = student.phone
                                    facilitator = student.facilitator
                                    batch = student.batch
                                    profession = student.profession
                                    address = student.address
                                    showDataFetchedToast = true
                                } else {
                                    showStudentNotFoundDialog = true
                                }
                            }
                        }
                    ) {
                        Text("Fetch")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPhoneDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Student Not Found Dialog
        if (showStudentNotFoundDialog) {
            AlertDialog(
                onDismissRequest = { showStudentNotFoundDialog = false },
                title = { Text("Not Found") },
                text = { Text("Student not found. Please check the phone number.") },
                confirmButton = {
                    TextButton(onClick = { showStudentNotFoundDialog = false }) {
                        Text("Ok")
                    }
                }
            )
        }

        // Data Fetched Toast
        if (showDataFetchedToast) {
            // Use a Toast or Snackbar for data fetched message
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    Button(onClick = { showDataFetchedToast = false }) {
                        Text("Close")
                    }
                }
            ) {
                Text("Data fetched and populated successfully!")
            }
        }
    }
}

fun isValidPhoneNumber(phone: String): Boolean {
    return phone.length == 10 && phone.all { it.isDigit() }
}

fun sendWhatsAppMessage(
    context: Context,
    phoneNumber: String,
    name: String,
    contact: String
) {
    val message = """
    Hare Krishna *${name.toCamelCase()} Prabhu Ji* 🙏
    
    Thanks for your registration for ISKCON Youth Forum (IYF) classes, it's a life-changing step to discover yourself and unleash your true potential. 💯
    
    📢 *We invite you to the Sunday Program*:
    🕒 *Timing*: 4:30 PM, this Sunday
    🎉  *Event*: Seminar 🧑‍💻🗣️, Kirtan 🎤, Music 🎸 and Delicious Prasadam 🍛🍰
    
    🏛️ *Venue*: ISKCON Temple, Lucknow
    
    Hare Krishna Prabhu Ji 🙏
    Badal Prabhu
    📞 *Contact*: $contact 
    (Please save this number)
""".trimIndent()

    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse("https://wa.me/$phoneNumber?text=$message")
    context.startActivity(intent)
}

fun String.toCamelCase(): String {
    return this.lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview(showBackground = true)
fun StudentFormScreenPreview() {
    MaterialTheme {
        StudentFormScreen()
    }
}

