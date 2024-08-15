package com.example.otp_class_app.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.otp_class_app.R
import com.example.otp_class_app.api.ApiService
import com.example.otp_class_app.models.StudentDTO
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

    val facilitators = listOf("NA", "H.G Sadhu Chaitanya Prabhu", "H.G Seva Actyute Prabhu", "H.G Rajiv Lochan Prabhu")
    val batches = listOf("DYS", "TSSV", "VL2")

    val icon1 = if (showDropdownFacilitator)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    val icon2 = if (showDropdownBatch)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Column(modifier = Modifier.padding(16.dp)) {
        // Custom header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
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
                .background(Color.White, shape = MaterialTheme.shapes.medium)
        )

        // Phone input
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .background(Color.White, shape = MaterialTheme.shapes.medium)
        )

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
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
                    .onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size.toSize()
                    },
                trailingIcon = {
                    Icon(icon1,"contentDescription",
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
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
                    .onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size.toSize()
                    },
                trailingIcon = {
                    Icon(icon2,"contentDescription",
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
                .background(Color.White, shape = MaterialTheme.shapes.medium)
        )

        // Address input
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .background(Color.White, shape = MaterialTheme.shapes.medium)
        )

        // Submit button
        // Submit button with loading indicator
        Button(
            onClick = {
                if (!isSubmitting) {
                    isSubmitting = true
                    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val student = StudentDTO(name, phone, facilitator, batch, profession, address, currentDate)
                    CoroutineScope(Dispatchers.Main).launch {
                        val isSuccess = withContext(Dispatchers.IO) {
                            ApiService.registerStudent(student)
                        }
                        isSubmitting = false
                        showSuccessDialog = isSuccess
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            if (isSubmitting) {
                // Show loading indicator inside button
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Submit")
            }
        }

        // Show processing animation if submitting
//        if (isSubmitting) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.5f), shape = MaterialTheme.shapes.medium),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        }

        // Success Dialog
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview(showBackground = true)
fun StudentFormScreenPreview() {
    MaterialTheme {
        StudentFormScreen()
    }
}
