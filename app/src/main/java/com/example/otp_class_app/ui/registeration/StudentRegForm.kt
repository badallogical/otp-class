package com.example.otp_class_app.screens

import android.os.Build
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
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.otp_class_app.R
import com.example.otp_class_app.ui.registeration.StudentFormViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StudentFormScreen( viewModel: StudentFormViewModel = viewModel(factory = StudentFormViewModel.Factory)) {

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val facilitators = listOf(
        "NA",
        "H.G Sadhu Chaitanya Prabhu",
        "H.G Seva Actyute Prabhu",
        "H.G Rajiv Lochan Prabhu"
    )
    val batches = listOf("DYS", "TSSV", "VL2")

    val icon1 = if (uiState.showDropdownFacilitator)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    val icon2 = if (uiState.showDropdownBatch)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    val backgroundColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.surfaceVariant // Dark theme background color
    } else {
        MaterialTheme.colorScheme.background // Light theme background color
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
                        viewModel.onEditRegistration()
                    }
            )
        }

        // Name input
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.onNameChange(it) },
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
                value = uiState.phone,
                onValueChange = { viewModel.onPhoneChange(it) },
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
                        if (uiState.phone.length == 10 && uiState.phone.all { it.isDigit() }) {
                            viewModel.sendWhatsAppMessage(
                                context,
                                uiState.phone,
                                uiState.name
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
                value = uiState.facilitator,
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
                        Modifier.clickable { viewModel.onDropDownFacilitator() })
                }
            )

            DropdownMenu(
                expanded = uiState.showDropdownFacilitator,
                onDismissRequest = { viewModel.onDismissFacilitator() },
                modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
            ) {
                facilitators.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                         viewModel.onFacilitatorChange(option)
                        }
                    )
                }
            }

        }

        // Batch dropdown
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

            OutlinedTextField(
                value = uiState.batch,
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
                        Modifier.clickable { viewModel.onDropDownBatch() } )
                }
            )
            DropdownMenu(
                expanded = uiState.showDropdownBatch,
                onDismissRequest = { viewModel.onDropDownBatch() },
                modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
            ) {
                batches.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                           viewModel.onBatchChange(option)
                        }
                    )
                }
            }
        }

        // Profession input
        OutlinedTextField(
            value = uiState.profession,
            onValueChange = { viewModel.onProfessionChange(it) },
            label = { Text("Profession") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .background(backgroundColor, shape = MaterialTheme.shapes.small)
        )

        // Address input
        OutlinedTextField(
            value = uiState.address,
            onValueChange = { viewModel.onAddressChange(it) },
            label = { Text("Address") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .background(backgroundColor, shape = MaterialTheme.shapes.small)
        )

        // Submit button
        Button(
            onClick = {

                if (!uiState.isSubmitting) {
                    if (!(uiState.phone.length == 10 && uiState.phone.all { it.isDigit() })) {
                        Toast.makeText(
                            context,
                            "Please enter a valid phone number",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    viewModel.onSubmit()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            if (uiState.isSubmitting) {
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
        if (uiState.showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onDismissSuccessDialog() },
                title = { Text( if(uiState.isSuccessfull) "Success" else "Failed")  },
                text = { Text(if( uiState.isSuccessfull ) "Gaurange ${uiState.name} Prabhu Ji, Hari Bol 🙏" else "Hari Bol, Try Again") },
                confirmButton = {
                    TextButton(onClick = { viewModel.onDismissSuccessDialog() }) {
                        Text("Hari Bol")
                    }
                }
            )
        }

        // Phone Number Dialog
        if (uiState.showPhoneDialog) {
            var phoneInput by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { viewModel.onDismissPhoneDialog() },
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
                            viewModel.onFetchStudentByPhone(phoneInput)
                        }
                    ) {
                        Text("Fetch")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissPhoneDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Student Not Found Dialog
        if (uiState.showStudentNotFoundDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onStudentNotFound() },
                title = { Text("Not Found") },
                text = { Text("Student not found. Please check the phone number.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.onStudentNotFound() }) {
                        Text("Ok")
                    }
                }
            )
        }

        // Data Fetched Toast
        if ( uiState.showDataFetchedToast) {
            // Use a Toast or Snackbar for data fetched message
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    Button(onClick = { viewModel.onDataFetched() }) {
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




@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview(showBackground = true)
fun StudentFormScreenPreview() {
    MaterialTheme {
        StudentFormScreen()
    }
}

