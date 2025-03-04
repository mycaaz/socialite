package com.google.android.samples.socialite.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.samples.socialite.R
import com.google.android.samples.socialite.model.User
import com.google.android.samples.socialite.model.UserRole
import com.google.android.samples.socialite.ui.components.LoadingScreen
import com.google.android.samples.socialite.ui.theme.BandConnectTheme

@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel = hiltViewModel(),
    onLogoutClick: () -> Unit,
    onAdminPanelClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.profile_title)) },
                actions = {
                    IconButton(onClick = { /* Open settings */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingScreen()
        } else if (uiState.currentUser != null) {
            UserProfileContent(
                user = uiState.currentUser!!,
                onLogoutClick = onLogoutClick,
                onAdminPanelClick = onAdminPanelClick,
                onNotificationToggle = { enabled ->
                    viewModel.updateNotificationSettings(enabled)
                },
                onLocationSharingToggle = { enabled ->
                    viewModel.updateLocationSharingSettings(enabled)
                },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // Not logged in state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = onLogoutClick) {
                    Text("Sign In")
                }
            }
        }
    }
}

@Composable
fun UserProfileContent(
    user: User,
    onLogoutClick: () -> Unit,
    onAdminPanelClick: () -> Unit,
    onNotificationToggle: (Boolean) -> Unit,
    onLocationSharingToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var notificationsEnabled by remember { mutableStateOf(user.notificationsEnabled) }
    var locationSharingEnabled by remember { mutableStateOf(user.locationSharingEnabled) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.first().toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onBackground
            )

            Text(
                text = "@${user.username}",
                fontSize = 16.sp,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Role Badge
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        when (user.role) {
                            UserRole.BAND_MEMBER -> MaterialTheme.colors.primary
                            UserRole.ADMIN -> MaterialTheme.colors.secondary
                            else -> MaterialTheme.colors.primary.copy(alpha = 0.3f)
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when (user.role) {
                        UserRole.BAND_MEMBER -> "Band Member"
                        UserRole.ADMIN -> "Admin"
                        else -> "Fan"
                    },
                    color = MaterialTheme.colors.onPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bio
            if (user.bio.isNotEmpty()) {
                Text(
                    text = user.bio,
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Edit profile */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile"
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Edit Profile")
                }

                if (user.role == UserRole.BAND_MEMBER || user.role == UserRole.ADMIN) {
                    Button(
                        onClick = onAdminPanelClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Panel"
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Admin Panel")
                    }
                }
            }
        }

        Divider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.1f)
        )

        // Settings Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications Setting
            SettingItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                description = "Receive updates from bands you follow",
                trailingContent = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            notificationsEnabled = it
                            onNotificationToggle(it)
                        }
                    )
                }
            )

            // Location Sharing Setting
            SettingItem(
                icon = Icons.Default.Person,
                title = "Location Sharing",
                description = "Share your location with band members",
                trailingContent = {
                    Switch(
                        checked = locationSharingEnabled,
                        onCheckedChange = {
                            locationSharingEnabled = it
                            onLocationSharingToggle(it)
                        }
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Logout Button
        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = androidx.compose.material.ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Logout"
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    description: String,
    trailingContent: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colors.primary
            )

            Spacer(modifier = Modifier.size(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colors.onSurface
                )

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }

            trailingContent()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileContentPreview() {
    val previewUser = User(
        id = "user1",
        username = "johnfan",
        email = "john@example.com",
        name = "John Smith",
        bio = "Huge music fan! Love rock and indie bands.",
        role = UserRole.FAN
    )

    BandConnectTheme {
        UserProfileContent(
            user = previewUser,
            onLogoutClick = {},
            onAdminPanelClick = {},
            onNotificationToggle = {},
            onLocationSharingToggle = {}
        )
    }
}
