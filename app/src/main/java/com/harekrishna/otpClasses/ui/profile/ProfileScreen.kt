import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.harekrishna.otpClasses.R
import com.harekrishna.otpClasses.data.models.User
import com.harekrishna.otpClasses.ui.profile.ProfileViewModel
import com.harekrishna.otpClasses.ui.theme.*

// ─── Stateful Screen ──────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    onSignOutSuccess: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.signOutSuccess) {
        if (uiState.signOutSuccess) {
            viewModel.onSignOutHandled()
            onSignOutSuccess()
        }
    }

    LaunchedEffect(uiState.profileUpdateSuccess) {
        if (uiState.profileUpdateSuccess) {
            snackbarHostState.showSnackbar("Profile updated")
            viewModel.onProfileUpdateHandled()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    ProfileScreenContent(
        user = uiState.user,
        isLoading = uiState.isLoading,
        isSigningOut = uiState.isSigningOut,
        isLinkingGoogle = uiState.isLinkingGoogle,
        isSavingProfile = uiState.isSavingProfile,
        snackbarHostState = snackbarHostState,
        onSignOut = viewModel::signOut,
        onSignInWithGoogle = viewModel::signInWithGoogle,
        onSaveProfile = viewModel::updateProfile
    )
}

// ─── Stateless Composable ─────────────────────────────────────────────────────

@Composable
fun ProfileScreenContent(
    user: User?,
    isLoading: Boolean = false,
    isSigningOut: Boolean = false,
    isLinkingGoogle: Boolean = false,
    isSavingProfile: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onSignOut: () -> Unit = {},
    onSignInWithGoogle: () -> Unit = {},
    onSaveProfile: (name: String, phone: String) -> Unit = { _, _ -> }
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                user != null -> {
                    ProfileBody(
                        user = user,
                        isSigningOut = isSigningOut,
                        isLinkingGoogle = isLinkingGoogle,
                        isSavingProfile = isSavingProfile,
                        onSignOut = onSignOut,
                        onSignInWithGoogle = onSignInWithGoogle,
                        onSaveProfile = onSaveProfile
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No profile found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ─── Profile Body ─────────────────────────────────────────────────────────────

@Composable
private fun ProfileBody(
    user: User,
    isSigningOut: Boolean,
    isLinkingGoogle: Boolean,
    isSavingProfile: Boolean,
    onSignOut: () -> Unit,
    onSignInWithGoogle: () -> Unit,
    onSaveProfile: (name: String, phone: String) -> Unit
) {
    var showSignOutDialog by remember { mutableStateOf(false) }

    // Edit state — local to UI only, committed on Save
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember(user.name) { mutableStateOf(user.name) }
    var editPhone by remember(user.phone) { mutableStateOf(user.phone) }

    // When save completes (isSavingProfile goes false while editing), exit edit mode
    LaunchedEffect(isSavingProfile) {
        if (!isSavingProfile && isEditing) {
            isEditing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeader(user = user)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Guest upgrade banner
            AnimatedVisibility(
                visible = user.isGuest,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                GuestUpgradeBanner(
                    isLinkingGoogle = isLinkingGoogle,
                    onSignInWithGoogle = onSignInWithGoogle
                )
            }

            if (user.isGuest) Spacer(modifier = Modifier.height(16.dp))

            // Info card — view or edit mode
            ProfileInfoCard(
                user = user,
                isEditing = isEditing,
                isSaving = isSavingProfile,
                editName = editName,
                editPhone = editPhone,
                onEditNameChange = { editName = it },
                onEditPhoneChange = { editPhone = it },
                onEditClick = {
                    editName = user.name
                    editPhone = user.phone
                    isEditing = true
                },
                onSaveClick = { onSaveProfile(editName, editPhone) },
                onCancelClick = {
                    editName = user.name
                    editPhone = user.phone
                    isEditing = false
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AccountStatusCard(user = user)

            Spacer(modifier = Modifier.height(24.dp))

            SignOutButton(
                isSigningOut = isSigningOut,
                onClick = { showSignOutDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showSignOutDialog) {
        SignOutDialog(
            onConfirm = {
                showSignOutDialog = false
                onSignOut()
            },
            onDismiss = { showSignOutDialog = false }
        )
    }
}

// ─── Hero Header ──────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(user: User) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(colors = listOf(Saffron, SaffronDark))
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    ProfileAvatar(
                        photoURL = user.photoURL,
                        name = user.name,
                        isGuest = user.isGuest
                    )
                    if (!user.isGuest) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(2.dp, Saffron, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Verified,
                                contentDescription = "Verified",
                                tint = GreenSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (user.isGuest) "Guest Devotee" else user.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.20f),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = if (user.isGuest) "Anonymous · Guest" else "Google Account",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ─── Avatar ───────────────────────────────────────────────────────────────────

@Composable
private fun ProfileAvatar(photoURL: String, name: String, isGuest: Boolean) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(CircleShape)
            .border(3.dp, Color.White.copy(alpha = 0.7f), CircleShape)
            .background(SaffronLight),
        contentAlignment = Alignment.Center
    ) {
        if (photoURL.isNotBlank() && !isGuest) {
            AsyncImage(
                model = photoURL,
                contentDescription = "Profile photo of $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else if (isGuest || name.isBlank()) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = "Guest",
                tint = Saffron,
                modifier = Modifier.size(44.dp)
            )
        } else {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Saffron
                )
            )
        }
    }
}

// ─── Info Card (view + edit modes) ───────────────────────────────────────────

@Composable
private fun ProfileInfoCard(
    user: User,
    isEditing: Boolean,
    isSaving: Boolean,
    editName: String,
    editPhone: String,
    onEditNameChange: (String) -> Unit,
    onEditPhoneChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val nameFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isEditing) {
        if (isEditing) nameFocusRequester.requestFocus()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            if (user.isGuest) {
                // Guest placeholder
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Sign in with Google to see your full profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                return@Card
            }

            // ── Card header row: "Profile Info" label + Edit button ──────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Profile info",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isEditing) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // ── Name row ─────────────────────────────────────────────────────
            if (isEditing) {
                EditableInfoRow(
                    icon = Icons.Outlined.Person,
                    label = "Name",
                    value = editName,
                    onValueChange = onEditNameChange,
                    focusRequester = nameFocusRequester,
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                    placeholder = "Your name"
                )
            } else {
                ProfileInfoRow(
                    icon = Icons.Outlined.Person,
                    label = "Name",
                    value = user.name.ifBlank { "—" }
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // ── Email row — always read-only ──────────────────────────────────
            ProfileInfoRow(
                icon = Icons.Outlined.Email,
                label = "Email",
                value = user.email.ifBlank { "—" },
                isReadOnly = true
            )

            if (user.phone.isNotBlank() || isEditing) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // ── Phone row ─────────────────────────────────────────────────
                if (isEditing) {
                    EditableInfoRow(
                        icon = Icons.Outlined.Phone,
                        label = "Phone",
                        value = editPhone,
                        onValueChange = onEditPhoneChange,
                        keyboardType = KeyboardType.Phone,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Done,
                        onDone = onSaveClick,
                        placeholder = "+91 00000 00000"
                    )
                } else {
                    ProfileInfoRow(
                        icon = Icons.Outlined.Phone,
                        label = "Phone",
                        value = user.phone
                    )
                }
            }

            // ── Save / Cancel row (edit mode only) ────────────────────────────
            AnimatedVisibility(visible = isEditing) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Cancel
                        OutlinedButton(
                            onClick = onCancelClick,
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }

                        // Save
                        Button(
                            onClick = onSaveClick,
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Saving…", fontSize = 13.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Read-only Info Row ───────────────────────────────────────────────────────

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isReadOnly: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isReadOnly)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        SaffronLight
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isReadOnly)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    Saffron,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (isReadOnly)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isReadOnly) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Read-only",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ─── Editable Info Row ────────────────────────────────────────────────────────

@Composable
private fun EditableInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() },
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    imeAction: ImeAction = ImeAction.Next,
    onDone: (() -> Unit)? = null,
    placeholder: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SaffronLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Saffron,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = capitalization,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone?.invoke() }
            ),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )
    }
}

// ─── Account Status Card ──────────────────────────────────────────────────────

@Composable
private fun AccountStatusCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (user.isGuest) SaffronLight else GreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (user.isGuest) Icons.Outlined.PersonOutline else Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        tint = if (user.isGuest) Saffron else GreenSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Account Type",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (user.isGuest) "Guest (Anonymous)" else "Signed In · Google",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = if (user.isGuest) Saffron else GreenSuccess
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Badge,
                        contentDescription = null,
                        tint = BlueInfo,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "User ID",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = user.id.take(20) + if (user.id.length > 20) "…" else "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// ─── Guest Upgrade Banner ─────────────────────────────────────────────────────

@Composable
private fun GuestUpgradeBanner(
    isLinkingGoogle: Boolean,
    onSignInWithGoogle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SaffronLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, Saffron.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Stars,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Save your progress",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = SaffronDark
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "You're currently a guest. Sign in with Google to sync attendance, save your class history, and never lose your data.",
                style = MaterialTheme.typography.bodySmall,
                color = SaffronDark.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSignInWithGoogle,
                enabled = !isLinkingGoogle,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Saffron,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLinkingGoogle) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connecting…")
                } else {
                    Icon(
                        painter = painterResource(R.drawable.google_logo),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─── Sign Out Button ──────────────────────────────────────────────────────────

@Composable
private fun SignOutButton(isSigningOut: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isSigningOut,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
        )
    ) {
        if (isSigningOut) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.error,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Signing out…", fontWeight = FontWeight.SemiBold)
        } else {
            Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Sign Out", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─── Sign Out Dialog ──────────────────────────────────────────────────────────

@Composable
private fun SignOutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                "Sign Out?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                "You'll need to sign in again to access your class records and attendance history.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Sign Out", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "View mode · Light", showBackground = true, backgroundColor = 0xFFFFFBF7)
@Composable
private fun ProfileViewModePreview() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        ProfileScreenContent(
            user = User(
                id = "firebase_uid_abc123xyz",
                name = "Radha Madhav Das",
                phone = "+91 98765 43210",
                photoURL = "",
                email = "radhamadhav@gmail.com",
                isGuest = false
            )
        )
    }
}

@Preview(name = "Edit mode · Light", showBackground = true, backgroundColor = 0xFFFFFBF7)
@Composable
private fun ProfileEditModePreview() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        // Directly show the card in edit state for quick preview
        Column(modifier = Modifier.padding(16.dp)) {
            ProfileInfoCard(
                user = User(
                    id = "firebase_uid_abc123xyz",
                    name = "Radha Madhav Das",
                    phone = "+91 98765 43210",
                    photoURL = "",
                    email = "radhamadhav@gmail.com",
                    isGuest = false
                ),
                isEditing = true,
                isSaving = false,
                editName = "Radha Madhav Das",
                editPhone = "+91 98765 43210",
                onEditNameChange = {},
                onEditPhoneChange = {},
                onEditClick = {},
                onSaveClick = {},
                onCancelClick = {}
            )
        }
    }
}

@Preview(name = "Saving state · Light", showBackground = true, backgroundColor = 0xFFFFFBF7)
@Composable
private fun ProfileSavingPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        Column(modifier = Modifier.padding(16.dp)) {
            ProfileInfoCard(
                user = User(
                    id = "firebase_uid_abc123xyz",
                    name = "Radha Madhav Das",
                    phone = "+91 98765 43210",
                    photoURL = "",
                    email = "radhamadhav@gmail.com",
                    isGuest = false
                ),
                isEditing = true,
                isSaving = true,
                editName = "Radha Madhav Das",
                editPhone = "+91 98765 43210",
                onEditNameChange = {},
                onEditPhoneChange = {},
                onEditClick = {},
                onSaveClick = {},
                onCancelClick = {}
            )
        }
    }
}

@Preview(name = "Dark mode", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun ProfileDarkPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.DARK) {
        ProfileScreenContent(
            user = User(
                id = "firebase_uid_abc123xyz",
                name = "Vrindavan Chandra Das",
                phone = "+91 99887 76655",
                photoURL = "",
                email = "vrindavan@gmail.com",
                isGuest = false
            )
        )
    }
}