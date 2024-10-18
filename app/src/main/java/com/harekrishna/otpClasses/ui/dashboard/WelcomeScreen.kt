package com.harekrishna.otpClasses.ui.dashboard

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.harekrishna.otpClasses.MyApplication
import com.harekrishna.otpClasses.data.api.AttendanceDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WelcomeScreen(navController: NavController) {
    // State variables for the text fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Background color and padding
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Heading
        Text(
            text = "Hare Krishna Prabhu Ji,",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Subheading
        Text(
            text = "Please give your basic details and engage your best to the service of Guru and Gauranga, Hari Bol! 🙏",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Name TextField
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone TextField
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(50.dp))

        // Proceed Button
        Button(
            onClick = {
                if (name.isNotBlank() && phone.isNotBlank()) {

                    if (!(phone.length == 10 && phone.all { it.isDigit() })) {
                        Toast.makeText(
                            MyApplication.applicationContext(),
                            "Please enter a valid phone number",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Call the method to save to DataStore
                        CoroutineScope(Dispatchers.IO).launch {
                            AttendanceDataStore.saveUserData(name, phone)

                            // Navigate to the dashboard on the main thread
                            withContext(Dispatchers.Main) {
                                navController.navigate("dashboard") // Navigate to the dashboard
                            }
                        }
                    }

                }
            },
            modifier = Modifier.width(150.dp)
        ) {
            Text("Proceed")
        }
    }
}

@Preview
@Composable
fun WelcomePreview() {
    WelcomeScreen(rememberNavController())
}
