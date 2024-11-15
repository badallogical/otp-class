package com.harekrishna.otpClasses.ui.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.harekrishna.otpClasses.R
import com.harekrishna.otpClasses.ui.theme.Otp_class_appTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SideNavigationBar(navController = navController, drawerState)
        },
        content = {
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
                                    modifier = Modifier.weight(1f).clickable {
                                        scope.launch {
                                            drawerState.open()
                                        }
                                    }
                                )

                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                )
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
                        ),
                    )
                },
                content = { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { change, dragAmount ->
                                    if (dragAmount > 10) {
                                        scope.launch { drawerState.open() }
                                    }
                                }
                            }
                    ) {
                        DashboardContent(navController)
                    }
                }
            )
        }
    )


}

@Composable
fun DashboardContent(navController: NavController){
    Scaffold(
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
                    title = "Follow Up",
                    subtitle = "Monitor and Track students",
                    onClick = { navController.navigate("followup") }
                )
            }
            Spacer(modifier = Modifier.weight(1f)) // Adjust the space between top and bottom content

            Text(
                text = "ISKCON Youth Forum",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(modifier = Modifier.size(20.dp))
        }
    }
}


@Composable
fun SideNavigationBar(
    navController: NavController,
    drawerState : DrawerState
) {

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(color =MaterialTheme.colorScheme.onPrimary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.prabhupad),
                contentDescription = "Srila Prabhupad",
                modifier = Modifier.fillMaxWidth()
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 0.5.dp),
            color = Color.Gray.copy(alpha = 0.3f)
        )

        NavigationMenuItem(
            label = "Home",
            icon = R.drawable.baseline_home_24,
            onClick = { coroutineScope.launch {  drawerState.close() } }
        )

        NavigationMenuItem(
            label = "Settings",
            icon = R.drawable.baseline_settings_24,
            onClick = { navController.navigate("settings") }
        )

        NavigationMenuItem(
            label = "About",
            icon = R.drawable.baseline_info_24,
            onClick = { navController.navigate("about") }
        )
    }
}

@Composable
fun NavigationMenuItem(
    label: String,
    icon: Int,
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
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
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


