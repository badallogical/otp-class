package com.harekrishna.otpClasses.ui.dashboard

// SettingsScreen.kt
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.harekrishna.otpClasses.data.api.AttendanceDataStore
import com.harekrishna.otpClasses.ui.followup.FollowUpUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen( navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {
    var showWelcomeImagePicker by remember { mutableStateOf(false) }
    var showThanksImagePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                )
            )
        }
    ) { paddingValues ->

        // Main content container
        Box(modifier = Modifier.fillMaxSize()) {
            // Show a loading spinner if isLoading is true
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)), // Optional overlay
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Main content
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .alpha(if (uiState.isLoading) 0.5f else 1f) // Dim content if loading
            ) {
                // Profile Settings Section
                SettingsSection(title = "Profile Settings") {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = uiState.phone,
                        onValueChange = { viewModel.updatePhone(it) },
                        label = { Text("Phone") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Enhanced Message Settings Section
                SettingsSection(title = "Message Settings") {
                    MessageSettingCard(
                        title = "Welcome Message",
                        message = uiState.welcomeMessage,
                        onMessageChange = { viewModel.updateWelcomeMessage(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MessageSettingCard(
                        title = "Thanks Message",
                        message = uiState.thanksMessage,
                        onMessageChange = { viewModel.updateThanksMessage(it) }
                    )
                }

                // Other Settings Section
                SettingsSection(title = "Other Settings") {
                    // Notifications Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Enable Reminder Notification")
                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.updateNotifications(it) }
                        )
                    }
                }

                SaveButton(navController) { viewModel.saveSettings()
                }
            }
        }
    }
}

@Composable
fun SaveButton(
    navController: NavController,
    onClick: () -> Unit
) {
    // State to track the button's scale for the popping effect
    var isPressed by remember { mutableStateOf(false) }

    // Animates the button's scale during the press effect
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f, // Scale down slightly when pressed
        animationSpec = tween(durationMillis = 200) // Smooth animation
    )

    // Reset isPressed after a short delay whenever it's set to true
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100) // Allow the animation to play
            isPressed = false // Reset to allow consecutive pops
        }
    }

    Button(
        onClick = {
            // Trigger the pop effect
            isPressed = true
            onClick() // Call the save logic
            navController.popBackStack()

        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale) // Apply the scale effect
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        try {
                            isPressed = true
                            awaitRelease() // Wait for press gesture to finish
                        } finally {
                            isPressed = false
                        }
                    }
                )
            }
    ) {
        Text("Save Settings")
    }
}



@Composable
private fun MessageSettingCard(
    title: String,
    message: String,
    onMessageChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Message TextField
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                label = { Text("Message Text") },
                placeholder = { Text("Enter your message here...") },
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                minLines = 4,
                maxLines = Int.MAX_VALUE
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

data class SettingsUiState(
    val name: String = "",
    val phone: String = "",
    val welcomeMessage: String = "",
    val thanksMessage: String = "",
    val notificationsEnabled: Boolean = false,
    val darkModeEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
)


class SettingsViewModel : ViewModel() {

    // The UI state encapsulating all the settings
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()


    init {
        viewModelScope.launch {
            val existingWelcomeMessage = AttendanceDataStore.getWelcomeMessage()
            val existingThanksMessage = AttendanceDataStore.getThanksMessage()

            // If the messages are not already set, save the default ones
            if (existingWelcomeMessage.isNullOrEmpty()) {
                // Define individual string components for the Welcome Message
                val welcomePart1 =
                    "Thank you for registering in ISKCON Youth Forum (IYF) program. \uD83C\uDF89✌\n"
                val welcomePart2 =
                    "\n" + "It is  a *life-changing* step to *discover yourself* and *unleash your true potential*. \uD83D\uDCAF\uD83C\uDF1F\n\n"
                val welcomePart3 = "📢 *We invite you to the Sunday Program*:\n\n"
                val welcomePart4 = "🕒 *Timing*: *4:30 PM*, this *Sunday*\n"
                val welcomePart5 =
                    "\uD83C\uDF89 *Event*: Seminar \uD83E\uDDD1\u200D\uD83D\uDCBB\uD83D\uDDE3, Kirtan \uD83C\uDFA4, Music \uD83C\uDFB8, Q&A session , Mentorship and Delicious Snacks \uD83C\uDF5B\uD83C\uDF70\n"+"\n*Entry is Free*"

                // Concatenate the parts
                val welcomeMsg = welcomePart1 + welcomePart2 + welcomePart3 + welcomePart4 + welcomePart5

                // Save the Welcome Message
                AttendanceDataStore.saveWelcomeMessage(welcomeMsg)


            }

            if (existingThanksMessage.isNullOrEmpty()) {
                // Define individual string components for the Thanks Message
                val thanksPart1 =
                    "Thank you for attending our ISKCON Youth Forum (IYF) Program! 🌟\n"
                val thanksPart2 =
                    "We're glad you joined, and we hope it was a fruitful experience for your spiritual journey. 🌱\n\n"
                val thanksPart3 = "📢 *We warmly invite you to our next Sunday Program*:\n"
                val thanksPart4 = "🕒 *Timing*: 4:30 PM, this Sunday\n"
                val thanksPart5 =
                    "🎉 *Highlights*: Engaging Seminar 🧑‍💻🗣️, Soul-stirring Kirtan 🎤, Live Music 🎸, and Delicious Snacks 🍛🍰.\n\n"
                val thanksPart6 = "🏛️ *Venue*: ISKCON Temple, Lucknow"

                // Concatenate the parts
                val thanksMsg =
                    thanksPart1 + thanksPart2 + thanksPart3 + thanksPart4 + thanksPart5 + thanksPart6

                // Encode the Thanks Message and save it
                AttendanceDataStore.saveThanksMessage(thanksMsg)
            }

            loadSettings()
        }
    }


    // Methods to update the UI state
    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone)
    }

    fun updateWelcomeMessage(message: String) {
        _uiState.value = _uiState.value.copy(welcomeMessage = message)
    }

    fun updateThanksMessage(message: String) {
        _uiState.value = _uiState.value.copy(thanksMessage = message)
    }

    fun updateNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    fun updateDarkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(darkModeEnabled = enabled)
    }

    // Save settings in DataStore
    fun saveSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                withContext(Dispatchers.IO) {
                    AttendanceDataStore.saveUserData(_uiState.value.name, _uiState.value.phone)
                    AttendanceDataStore.saveWelcomeMessage(_uiState.value.welcomeMessage)
                    AttendanceDataStore.saveThanksMessage(_uiState.value.thanksMessage)
                }
            } catch (e: Exception) {
                // Log or handle the error gracefully
                Log.e("SaveSettings", "Error saving settings: ${e.message}", e)
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }


    // Load settings from DataStore into the UI state
    fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            withContext(Dispatchers.IO) {
                val userData = AttendanceDataStore.getUserData().first()

                // Load the settings and update the UI state
                val welcomeMessage = AttendanceDataStore.getWelcomeMessage()
                val thanksMessage = AttendanceDataStore.getThanksMessage()

                // Update the UI state with the loaded data
                _uiState.value = _uiState.value.copy(
                    name = userData.first ?: "",
                    phone = userData.second ?: "",
                    welcomeMessage = welcomeMessage,
                    thanksMessage = thanksMessage,
                    isLoading = false
                )
            }
        }
    }
}

