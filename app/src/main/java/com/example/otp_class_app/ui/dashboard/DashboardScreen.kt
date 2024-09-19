package com.example.otp_class_app.ui.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Background color from the theme
                    titleContentColor = MaterialTheme.colorScheme.onPrimary // Title text color from the theme
                )
                
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Column(
                modifier = Modifier
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardOption(
                    icon = painterResource(R.drawable.baseline_edit_24),
                    title = "Registration",
                    subtitle = "Register new students",
                    onClick = { navController.navigate("registration") }
                )
                DashboardOption(
                    icon = painterResource(R.drawable.baseline_done_outline_24),
                    title = "Attendance",
                    subtitle = "Mark student attendance",
                    onClick = { navController.navigate("attendance") }
                )
                DashboardOption(
                    icon = painterResource(R.drawable.outline_report_24),
                    title = "Reporting",
                    subtitle = "Generate and view reports",
                    onClick = { navController.navigate("reporting") }
                )
            }
            Spacer(modifier = Modifier.weight(1f)) // Adjust the space between top and bottom content

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
fun DashboardOption(
    icon: Painter,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(35.dp), // Adjust icon size as needed
            tint = MaterialTheme.colorScheme.primary // Set the icon color
        )
        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
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


