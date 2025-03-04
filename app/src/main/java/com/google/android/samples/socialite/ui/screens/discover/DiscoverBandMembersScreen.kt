package com.google.android.samples.socialite.ui.screens.discover

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
fun DiscoverBandMembersScreen(
    viewModel: DiscoverBandMembersViewModel = hiltViewModel(),
    onBandMemberClick: (String) -> Unit,
    onMessageClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.explore_title)) },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingScreen()
        } else {
            DiscoverBandMembersContent(
                bandMembers = uiState.bandMembers,
                genres = uiState.availableGenres,
                selectedGenre = uiState.selectedGenre,
                onGenreSelected = { viewModel.filterByGenre(it) },
                onSearchQueryChanged = { viewModel.updateSearchQuery(it) },
                onBandMemberClick = onBandMemberClick,
                onMessageClick = onMessageClick,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DiscoverBandMembersContent(
    bandMembers: List<User>,
    genres: List<String>,
    selectedGenre: String?,
    onGenreSelected: (String?) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onBandMemberClick: (String) -> Unit,
    onMessageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onSearchQueryChanged(it)
            },
            placeholder = { Text("Search band members...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        // Genre Filters
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedGenre == null,
                    onClick = { onGenreSelected(null) },
                    label = "All"
                )
            }

            items(genres) { genre ->
                FilterChip(
                    selected = genre == selectedGenre,
                    onClick = { onGenreSelected(genre) },
                    label = genre
                )
            }
        }

        Divider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.1f)
        )

        if (bandMembers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No band members found",
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            // Band Members List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                items(bandMembers) { bandMember ->
                    BandMemberCard(
                        bandMember = bandMember,
                        onBandMemberClick = { onBandMemberClick(bandMember.id) },
                        onMessageClick = { onMessageClick(bandMember.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Chip(
        onClick = onClick,
        colors = ChipDefaults.chipColors(
            backgroundColor = if (selected) {
                MaterialTheme.colors.primary
            } else {
                MaterialTheme.colors.surface
            },
            contentColor = if (selected) {
                MaterialTheme.colors.onPrimary
            } else {
                MaterialTheme.colors.onSurface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colors.primary
            } else {
                MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            }
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun BandMemberCard(
    bandMember: User,
    onBandMemberClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onBandMemberClick),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = bandMember.name.first().toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bandMember.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface
                        )

                        if (bandMember.bandInfo?.isVerified == true) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Verified",
                                tint = MaterialTheme.colors.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    bandMember.bandInfo?.let {
                        Text(
                            text = "${it.role} • ${it.bandName}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colors.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = bandMember.bio,
                        fontSize = 14.sp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Message Button
                Icon(
                    imageVector = Icons.Default.Message,
                    contentDescription = "Message",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onMessageClick)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Genres
            bandMember.bandInfo?.let { bandInfo ->
                if (bandInfo.genres.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        bandInfo.genres.take(3).forEach { genre ->
                            GenreTag(genre = genre)
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        if (bandInfo.genres.size > 3) {
                            Text(
                                text = "+${bandInfo.genres.size - 3}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GenreTag(genre: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = genre,
            fontSize = 12.sp,
            color = MaterialTheme.colors.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BandMemberCardPreview() {
    val previewBandMember = User(
        id = "user1",
        username = "johnlennon",
        email = "john@example.com",
        name = "John Lennon",
        bio = "Rhythm guitarist and vocalist for the Beatles.",
        role = UserRole.BAND_MEMBER,
        bandInfo = BandMemberInfo(
            bandName = "The Beatles",
            role = "Lead Vocalist/Guitarist",
            instruments = listOf("Guitar", "Piano", "Harmonica"),
            yearsOfExperience = 20,
            genres = listOf("Rock", "Pop"),
            isVerified = true
        )
    )

    BandConnectTheme {
        BandMemberCard(
            bandMember = previewBandMember,
            onBandMemberClick = {},
            onMessageClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DiscoverBandMembersScreenPreview() {
    val previewBandMembers = listOf(
        User(
            id = "user1",
            username = "johnlennon",
            email = "john@example.com",
            name = "John Lennon",
            bio = "Rhythm guitarist and vocalist for the Beatles.",
            role = UserRole.BAND_MEMBER,
            bandInfo = BandMemberInfo(
                bandName = "The Beatles",
                role = "Lead Vocalist/Guitarist",
                instruments = listOf("Guitar", "Piano", "Harmonica"),
                yearsOfExperience = 20,
                genres = listOf("Rock", "Pop"),
                isVerified = true
            )
        ),
        User(
            id = "user2",
            username = "davegrohl",
            email = "dave@example.com",
            name = "Dave Grohl",
            bio = "Founder of Foo Fighters, former drummer of Nirvana",
            role = UserRole.BAND_MEMBER,
            bandInfo = BandMemberInfo(
                bandName = "Foo Fighters",
                role = "Lead Vocalist/Guitarist",
                instruments = listOf("Drums", "Guitar", "Vocals"),
                yearsOfExperience = 30,
                genres = listOf("Rock", "Alternative"),
                isVerified = true
            )
        )
    )

    BandConnectTheme {
        DiscoverBandMembersContent(
            bandMembers = previewBandMembers,
            genres = listOf("Rock", "Pop", "Alternative", "Hip-Hop", "Country"),
            selectedGenre = "Rock",
            onGenreSelected = {},
            onSearchQueryChanged = {},
            onBandMemberClick = {},
            onMessageClick = {}
        )
    }
}
