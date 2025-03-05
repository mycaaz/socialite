/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.samples.socialite.ui.home.timeline

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.samples.socialite.R
import com.google.android.samples.socialite.ui.home.HomeAppBar
import com.google.android.samples.socialite.ui.home.HomeBackground
import com.google.android.samples.socialite.ui.navigation.TopLevelDestination
import com.google.android.samples.socialite.ui.rememberIconPainter
import kotlin.math.absoluteValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun Timeline(
    modifier: Modifier = Modifier,
) {
    val viewModel: TimelineViewModel = hiltViewModel()
    val media = viewModel.media
    val player = viewModel.player
    val videoRatio = viewModel.videoRatio
    val upcomingConcerts = viewModel.upcomingConcerts
    val featuredBands = viewModel.featuredBands
    
    var genreSearchQuery by remember { mutableStateOf("") }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            HomeAppBar(title = "Music Discovery") // Changed from generic "Timeline"
        },
    ) { contentPadding ->
        HomeBackground(modifier = Modifier.fillMaxSize())
        
        LazyColumn(
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            // Genre search section
            item {
                SearchSection(
                    query = genreSearchQuery,
                    onQueryChange = { genreSearchQuery = it },
                    onSearch = { viewModel.searchBandsByGenre(genreSearchQuery) }
                )
            }
            
            // Featured Bands Section
            item {
                SectionTitle(title = "Featured Bands")
            }
            
            item {
                if (featuredBands.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(featuredBands) { band ->
                            BandCard(
                                bandInfo = band,
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No bands found. Try another genre.")
                    }
                }
            }
            
            // Divider
            item {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 32.dp)
                )
            }
            
            // Upcoming Concerts Section
            item {
                SectionTitle(title = "Upcoming Concerts")
            }
            
            item {
                if (upcomingConcerts.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(upcomingConcerts) { concert ->
                            ConcertCard(
                                concertInfo = concert,
                                modifier = Modifier.width(250.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No upcoming concerts scheduled")
                    }
                }
            }
            
            // Divider
            item {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 32.dp)
                )
            }
            
            // Music Media Section
            item {
                SectionTitle(title = "Latest Band Media")
            }
            
            item {
                if (media.isEmpty()) {
                    EmptyTimeline(modifier = Modifier.padding(32.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(450.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        TimelineVerticalPager(
                            PaddingValues(0.dp),
                            Modifier,
                            media,
                            player,
                            viewModel::initializePlayer,
                            viewModel::releasePlayer,
                            viewModel::changePlayerItem,
                            videoRatio,
                        )
                    }
                }
            }
            
            // Spacer at the bottom
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        fontWeight = FontWeight.Bold
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Find bands by genre",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Rock, Jazz, Pop...") },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null
                    )
                }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
        }
    }
}

@Composable
fun BandCard(
    bandInfo: BandInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // Band Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(bandInfo.iconUri)
                    .build(),
                contentDescription = "Band image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Band Info
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = bandInfo.bandName.ifEmpty { bandInfo.name },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Genre: ${bandInfo.genre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = bandInfo.bio,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ConcertCard(
    concertInfo: ConcertInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // Concert Header with Band Image
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Band Icon
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(concertInfo.iconUri)
                        .build(),
                    contentDescription = "Band icon",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.onPrimaryContainer, CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = concertInfo.bandName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Concert Details
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = concertInfo.concertDetails,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Location with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = concertInfo.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // View Details Button
                Button(
                    onClick = { /* Navigate to concert details */ },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                ) {
                    Text("Get Tickets")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineVerticalPager(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    mediaItems: List<TimelineMediaItem>,
    player: Player?,
    onInitializePlayer: () -> Unit = {},
    onReleasePlayer: () -> Unit = {},
    onChangePlayerItem: (uri: Uri?, page: Int) -> Unit = { uri: Uri?, i: Int -> },
    videoRatio: Float?,
) {
    val pagerState = rememberPagerState(pageCount = { mediaItems.count() })
    LaunchedEffect(pagerState) {
        // Collect from the a snapshotFlow reading the settledPage
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (mediaItems[page].type == TimelineMediaType.VIDEO) {
                onChangePlayerItem(Uri.parse(mediaItems[page].uri), pagerState.currentPage)
            } else {
                onChangePlayerItem(null, pagerState.currentPage)
            }
        }
    }

    val currentOnInitializePlayer by rememberUpdatedState(onInitializePlayer)
    val currentOnReleasePlayer by rememberUpdatedState(onReleasePlayer)
    if (Build.VERSION.SDK_INT > 23) {
        LifecycleStartEffect(true) {
            currentOnInitializePlayer()
            onStopOrDispose {
                currentOnReleasePlayer()
            }
        }
    } else {
        LifecycleResumeEffect(true) {
            currentOnInitializePlayer()
            onPauseOrDispose {
                currentOnReleasePlayer()
            }
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = modifier
            .padding(contentPadding)
            .fillMaxSize(),
    ) { page ->
        if (player != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .graphicsLayer {
                        // Calculate the absolute offset for the current page from the
                        // scroll position. We use the absolute value which allows us to mirror
                        // any effects for both directions
                        val pageOffset = (
                            (pagerState.currentPage - page) + pagerState
                                .currentPageOffsetFraction
                            ).absoluteValue

                        // We animate the alpha, between 0% and 100%
                        alpha = lerp(
                            start = 0f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f),
                        )
                    },
            ) {
                TimelinePage(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    media = mediaItems[page],
                    player = player,
                    page,
                    pagerState,
                    videoRatio,
                )

                BandMediaOverlay(
                    modifier = Modifier.padding(16.dp), 
                    mediaItem = mediaItems[page]
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelinePage(
    modifier: Modifier = Modifier,
    media: TimelineMediaItem,
    player: Player,
    page: Int,
    state: PagerState,
    videoRatio: Float?,
) {
    when (media.type) {
        TimelineMediaType.VIDEO -> {
            if (page == state.settledPage) {
                // When in preview, early return a Box with the received modifier preserving layout
                if (LocalInspectionMode.current) {
                    Box(modifier = modifier)
                    return
                }
                AndroidView(
                    factory = { PlayerView(it) },
                    update = { playerView ->
                        playerView.player = player
                    },
                    modifier = modifier.fillMaxSize(),
                )
            }
        }
        TimelineMediaType.PHOTO -> {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(media.uri)
                    .build(),
                contentDescription = null,
                modifier = modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
fun BandMediaOverlay(modifier: Modifier, mediaItem: TimelineMediaItem) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(999f),
    ) {
        if (mediaItem.type == TimelineMediaType.VIDEO) {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            val context = LocalContext.current.applicationContext

            // Running on an IO thread for loading metadata from remote urls to reduce lag time
            val duration: State<Long?> = produceState<Long?>(initialValue = null) {
                withContext(Dispatchers.IO) {
                    // Remote url
                    if (mediaItem.uri.contains("https://")) {
                        mediaMetadataRetriever.setDataSource(mediaItem.uri, HashMap<String, String>())
                    } else { // Locally saved files
                        mediaMetadataRetriever.setDataSource(context, Uri.parse(mediaItem.uri))
                    }
                    value =
                        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            ?.toLong()
                }
            }
            duration.value?.let {
                val seconds = it / 1000L
                val minutes = seconds / 60L
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                ) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "%d:%02d".format(minutes, seconds % 60),
                    )
                }
            }
        }

        // Band Info Box
        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomStart)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                .padding(12.dp),
        ) {
            // Title row with icon
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                mediaItem.chatIconUri?.let {
                    Image(
                        painter = rememberIconPainter(contentUri = it),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                    )
                }
                
                Column {
                    Text(
                        text = mediaItem.chatName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Genre info
                    if (mediaItem.bandDescription.isNotEmpty()) {
                        Text(
                            text = mediaItem.bandDescription,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            
            // Concert location if available
            if (mediaItem.concertLocation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = mediaItem.concertLocation,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Media title if available
            if (mediaItem.mediaTitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mediaItem.mediaTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EmptyTimeline(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(64.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.empty_timeline),
            contentDescription = null,
        )
        Text(
            text = "No Band Media Yet",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = "Follow your favorite bands to see their performance videos and photos here",
            textAlign = TextAlign.Center,
        )
    }
}