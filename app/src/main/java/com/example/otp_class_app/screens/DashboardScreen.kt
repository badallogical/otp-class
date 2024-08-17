package com.example.otp_class_app.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.otp_class_app.R
import com.example.otp_class_app.ui.theme.Otp_class_appTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                         // Align the content to the far sides
                    ) {
                        Text(
                            text = "Hari Bol",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )

                        Row(modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically)
                        {
                            Image(
                                painter = painterResource(id = R.drawable.iyflogo), // Replace with your refresh icon resource
                                contentDescription = "iyf logo",
                                modifier = Modifier
                                    .size(50.dp)
                            )

                            Text(
                                text = " IYF Classes",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardOption(
                    text = "Registration",
                    icon = painterResource(R.drawable.baseline_edit_24),
                    onClick = { navController.navigate("registration") }
                )
                DashboardOption(
                    text = "Attendance",
                    icon = painterResource(R.drawable.baseline_done_outline_24),
                    onClick = { navController.navigate("attendance") }
                )
                DashboardOption(
                    text = "Reporting",
                    icon = painterResource(R.drawable.outline_report_24),
                    onClick = { navController.navigate("reporting") }
                )
            }

            Image(
                painter = painterResource(id = R.drawable.iskcon_logo), // Replace with your image resource
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp) // Adjust size as needed
            )
        }
    }
}

@Composable
fun DashboardOption(text: String, icon: Painter, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(15.dp)

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                painter = icon, // Replace with your icon resource
                contentDescription = null,
                modifier = Modifier.size(24.dp) // Size of the icon
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview(showBackground = true)
fun DashboardPreview() {
    Otp_class_appTheme {
        DashboardScreen(navController = rememberNavController())
    }
}


