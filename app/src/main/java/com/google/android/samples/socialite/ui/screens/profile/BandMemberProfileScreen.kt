package com.google.android.samples.socialite.ui.screens.profile

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.samples.socialite.R
import com.google.android.samples.socialite.model.BandMemberInfo
import com.google.android.samples.socialite.model.User
import com.google.android.samples.socialite.model.UserRole
import com.google.android.samples.socialite.ui.components.LoadingScreen
import com.google.android.samples.socialite.ui.theme.BandConnectTheme

@Composable
fun BandMemberProfileScreen(
    viewModel: BandMemberProfileViewModel = hiltViewModel(),
    bandMemberId: String,
    onBackClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onShareLocationClick: () -> Unit,
    isCurrentUser: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {
            LoadingScreen()
        }
        uiState.bandMember != null -> {
            BandMemberProfileContent(
                bandMember = uiState.bandMember!!,
                isCurrentUser = isCurrentUser,
                isFollowing = uiState.isFollowing,
                onBackClick = onBackClick,
                onFollowClick = { viewModel.toggleFollow(bandMemberId) },
                onMessageClick = { onMessageClick(bandMemberId) },
                onShareLocationClick = onShareLocationClick,
                onEditClick = { /* Handle edit click */ }
            )
        }
        else -> {
            // Error state or not found
            ErrorScreen(onBackClick = onBackClick)
        }
    }
}

@Composable
fun BandMemberProfileContent(
    bandMember: User,
    isCurrentUser: Boolean,
    isFollowing: Boolean,
    onBackClick: () -> Unit,
    onFollowClick: () -> Unit,
    onMessageClick: () -> Unit,
    onShareLocationClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (isCurrentUser) {
                        IconButton(onClick = onEditClick) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile"
                            )
                        }
                    } else {
                        IconButton(onClick = { /* Share profile */ }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Profile"
                            )
                        }
                    }
                },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colors.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile Header
                ProfileHeader(
                    bandMember = bandMember,
                    isCurrentUser = isCurrentUser,
                    isFollowing = isFollowing,
                    onFollowClick = onFollowClick,
                    onMessageClick = onMessageClick,
                    onShareLocationClick = onShareLocationClick
                )

                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.1f)
                )

                // Band Info Section
                bandMember.bandInfo?.let { bandInfo ->
                    BandInfoSection(bandInfo = bandInfo)
                }

                // Upcoming Events Section
                UpcomingEventsSection()

                // Latest Posts Section
                LatestPostsSection()

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ProfileHeader(
    bandMember: User,
    isCurrentUser: Boolean,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onMessageClick: () -> Unit,
    onShareLocationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colors.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            // In a real app, we would load the profile image from a URL
            // For now, using a placeholder
            Text(
                text = bandMember.name.first().toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = bandMember.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onBackground
        )

        // Role in Band
        bandMember.bandInfo?.let {
            Text(
                text = "${it.role} • ${it.bandName}",
                fontSize = 16.sp,
                color = MaterialTheme.colors.primary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bio
        Text(
            text = bandMember.bio,
            fontSize = 14.sp,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Followers count
        Text(
            text = "${bandMember.followers.size} followers",
            fontSize = 14.sp,
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        if (!isCurrentUser) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Follow/Unfollow Button
                Button(
                    onClick = onFollowClick,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (isFollowing) {
                            MaterialTheme.colors.surface
                        } else {
                            MaterialTheme.colors.primary
                        }
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isFollowing) "Following" else "Follow",
                        color = if (isFollowing) {
                            MaterialTheme.colors.primary
                        } else {
                            MaterialTheme.colors.onPrimary
                        }
                    )
                }

                Spacer(modifier = Modifier.size(8.dp))

                // Message Button
                OutlinedButton(
                    onClick = onMessageClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "Message",
                        tint = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(text = "Message")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Share Location Button
            OutlinedButton(
                onClick = onShareLocationClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Share Location",
                    tint = MaterialTheme.colors.primary
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(text = stringResource(id = R.string.share_location))
            }
        }
    }
}

@Composable
fun BandInfoSection(bandInfo: BandMemberInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Artist Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Instruments
            InfoRow(
                title = "Instruments:",
                value = bandInfo.instruments.joinToString(", ")
            )

            // Genres
            InfoRow(
                title = "Genres:",
                value = bandInfo.genres.joinToString(", ")
            )

            // Experience
            InfoRow(
                title = "Experience:",
                value = "${bandInfo.yearsOfExperience} years"
            )

            // Verification Status
            if (bandInfo.isVerified) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Verified Artist",
                        tint = MaterialTheme.colors.secondary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Verified Artist",
                        fontSize = 14.sp,
                        color = MaterialTheme.colors.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.weight(0.3f)
        )

        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.weight(0.7f)
        )
    }
}

@Composable
fun UpcomingEventsSection() {
    // This would be populated with actual events in a real implementation
    SectionTitle(title = "Upcoming Events")

    Text(
        text = "No upcoming events",
        fontSize = 14.sp,
        color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun LatestPostsSection() {
    // This would be populated with actual posts in a real implementation
    SectionTitle(title = "Latest Updates")

    Text(
        text = "No updates yet",
        fontSize = 14.sp,
        color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colors.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun ErrorScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Could not load band member profile",
            fontSize = 18.sp,
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBackClick) {
            Text(text = "Go Back")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BandMemberProfilePreview() {
    val previewBandMember = User(
        id = "user1",
        username = "johnlennon",
        email = "john@example.com",
        name = "John Lennon",
        bio = "Rhythm guitarist and vocalist for the Beatles. Known for songwriting and activism.",
        role = UserRole.BAND_MEMBER,
        bandInfo = BandMemberInfo(
            bandName = "The Beatles",
            role = "Lead Vocalist/Guitarist",
            instruments = listOf("Guitar", "Piano", "Harmonica"),
            yearsOfExperience = 20,
            genres = listOf("Rock", "Pop"),
            isVerified = true
        ),
        followers = listOf("fan1", "fan2", "fan3")
    )

    BandConnectTheme {
        BandMemberProfileContent(
            bandMember = previewBandMember,
            isCurrentUser = false,
            isFollowing = true,
            onBackClick = {},
            onFollowClick = {},
            onMessageClick = {},
            onShareLocationClick = {},
            onEditClick = {}
        )
    }
}
