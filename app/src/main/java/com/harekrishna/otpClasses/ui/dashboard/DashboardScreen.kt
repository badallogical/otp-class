package com.harekrishna.otpClasses.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.harekrishna.otpClasses.R
import com.harekrishna.otpClasses.ui.theme.*
import kotlinx.coroutines.launch

// ─── Dashboard Screen ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SideNavigationBar(navController = navController, drawerState = drawerState)
        }
    ) {
        Scaffold(
            topBar = {
                DashboardTopBar(
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { _, dragAmount ->
                            if (dragAmount > 10) scope.launch { drawerState.open() }
                        }
                    }
            ) {
                DashboardContent(navController = navController)
            }
        }
    }
}

// ─── Top App Bar ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: greeting + hamburger hint
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onMenuClick() }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = "Open menu",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Hari Bol 🙏",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }

                // Right: IYF brand mark
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.iyflogo),
                        contentDescription = "IYF logo",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "IYF Classes",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

// ─── Dashboard Content ────────────────────────────────────────────────────────

@Composable
fun DashboardContent(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            DashboardCard(
                icon = painterResource(R.drawable.baseline_edit_24),
                title = "Registration",
                subtitle = "Register new students",
                accentColor = Saffron,
                accentBg = SaffronLight,
                onClick = { navController.navigate("registration") }
            )

            DashboardCard(
                icon = painterResource(R.drawable.baseline_done_outline_24),
                title = "Attendance",
                subtitle = "Mark studentProfile attendance",
                accentColor = GreenSuccess,
                accentBg = GreenLight,
                onClick = { navController.navigate("attendance") }
            )

            DashboardCard(
                icon = painterResource(R.drawable.event),
                title = "Events",
                subtitle = "Customised special occasions",
                accentColor = BlueInfo,
                accentBg = BlueLight,
                onClick = { navController.navigate("events") }
            )
        }

        // Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.iyflogo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ISKCON Youth Forum",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

// ─── Dashboard Card ───────────────────────────────────────────────────────────

@Composable
fun DashboardCard(
    icon: Painter,
    title: String,
    subtitle: String,
    accentColor: Color,
    accentBg: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container with accent background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── Side Navigation Drawer ───────────────────────────────────────────────────

@Composable
fun SideNavigationBar(
    navController: NavController,
    drawerState: DrawerState
) {
    val coroutineScope = rememberCoroutineScope()

    ModalDrawerSheet(
        modifier = Modifier.width(272.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp),
        windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Bottom)
    ) {
        // Hero image — full height, no crop
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.prabhupad),
                contentDescription = "Srila Prabhupada",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
            // Gradient overlay at bottom for text legibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                        )
                    )
            )
            Text(
                text = "Hare Krishna",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 10.dp)
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        Spacer(modifier = Modifier.height(8.dp))

        NavigationMenuItem(
            label = "Home",
            icon = R.drawable.baseline_home_24,
            onClick = { coroutineScope.launch { drawerState.close() } }
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

        Spacer(modifier = Modifier.weight(1f))

        // Profile shortcut at the bottom of the drawer
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        NavigationMenuItem(
            label = "My Profile",
            icon = R.drawable.baseline_settings_24, // swap with a person icon if available
            onClick = { navController.navigate("profile") }
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

// ─── Navigation Menu Item ─────────────────────────────────────────────────────

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
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "Dashboard · Light", showBackground = true, backgroundColor = 0xFFFFFBF7)
@Composable
private fun DashboardContentPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        DashboardContent(navController = rememberNavController())
    }
}

@Preview(name = "Dashboard · Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun DashboardContentDarkPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.DARK) {
        DashboardContent(navController = rememberNavController())
    }
}

@Preview(name = "Dashboard Card", showBackground = true, backgroundColor = 0xFFFFFBF7)
@Composable
private fun DashboardCardPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardCard(
                icon = painterResource(R.drawable.baseline_edit_24),
                title = "Registration",
                subtitle = "Register new students",
                accentColor = Saffron,
                accentBg = SaffronLight,
                onClick = {}
            )
            DashboardCard(
                icon = painterResource(R.drawable.baseline_done_outline_24),
                title = "Attendance",
                subtitle = "Mark studentProfile attendance",
                accentColor = GreenSuccess,
                accentBg = GreenLight,
                onClick = {}
            )
            DashboardCard(
                icon = painterResource(R.drawable.event),
                title = "Events",
                subtitle = "Customised special occasions",
                accentColor = BlueInfo,
                accentBg = BlueLight,
                onClick = {}
            )
        }
    }
}