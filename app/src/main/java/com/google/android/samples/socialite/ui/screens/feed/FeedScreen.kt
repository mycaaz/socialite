package com.google.android.samples.socialite.ui.screens.feed

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.samples.socialite.R
import com.google.android.samples.socialite.model.Post
import com.google.android.samples.socialite.model.PostType
import com.google.android.samples.socialite.ui.components.LoadingScreen
import com.google.android.samples.socialite.ui.theme.BandConnectTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    onBandMemberClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.event_feed_title)) },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingScreen()
        } else {
            FeedContent(
                posts = uiState.posts,
                userMap = uiState.userMap,
                onBandMemberClick = onBandMemberClick,
                onLikeClick = { postId -> viewModel.toggleLike(postId) },
                likedPosts = uiState.likedPosts,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun FeedContent(
    posts: List<Post>,
    userMap: Map<String, String>,
    onBandMemberClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    likedPosts: Set<String>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.no_posts),
                    fontSize = 18.sp,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
            ) {
                items(posts) { post ->
                    PostCard(
                        post = post,
                        authorName = userMap[post.authorId] ?: "Unknown Artist",
                        onAuthorClick = { onBandMemberClick(post.authorId) },
                        isLiked = likedPosts.contains(post.id),
                        onLikeClick = { onLikeClick(post.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    authorName: String,
    onAuthorClick: () -> Unit,
    isLiked: Boolean,
    onLikeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Post Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author image
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                        .clickable(onClick = onAuthorClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = authorName.first().toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = authorName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface,
                            modifier = Modifier.clickable(onClick = onAuthorClick)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // Verified icon
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Verified",
                            tint = MaterialTheme.colors.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = formatTimestamp(post.timestamp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Post type icon
                PostTypeIcon(postType = post.type)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post content
            Text(
                text = post.content,
                fontSize = 14.sp,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            // Event details if applicable
            post.eventDetails?.let { event ->
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = event.eventName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = "Event Date",
                                tint = MaterialTheme.colors.primary,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = formatDate(event.date),
                                fontSize = 14.sp,
                                color = MaterialTheme.colors.onSurface
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = event.location,
                                fontSize = 14.sp,
                                color = MaterialTheme.colors.onSurface,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Album details if applicable
            post.albumDetails?.let { album ->
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = album.albumName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.secondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Album",
                                tint = MaterialTheme.colors.secondary,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "${album.trackCount} tracks",
                                fontSize = 14.sp,
                                color = MaterialTheme.colors.onSurface
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "Released: ${formatDate(album.releaseDate)}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colors.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Post actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Like button
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "${post.likes.size}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Comment button
                IconButton(
                    onClick = { /* Handle comment */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = "Comment",
                            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "${post.comments.size}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Share button
                IconButton(
                    onClick = { /* Handle share */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun PostTypeIcon(postType: PostType) {
    val icon: ImageVector = when (postType) {
        PostType.EVENT_ANNOUNCEMENT -> Icons.Default.Event
        PostType.NEW_RELEASE -> Icons.Default.MusicNote
        else -> return // No icon for other types
    }

    val tint = when (postType) {
        PostType.EVENT_ANNOUNCEMENT -> MaterialTheme.colors.primary
        PostType.NEW_RELEASE -> MaterialTheme.colors.secondary
        else -> MaterialTheme.colors.onSurface
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = postType.name,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
        diff < 7 * 24 * 60 * 60 * 1000 -> {
            val format = SimpleDateFormat("EEE", Locale.getDefault())
            format.format(Date(timestamp))
        }
        else -> {
            val format = SimpleDateFormat("MMM d", Locale.getDefault())
            format.format(Date(timestamp))
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun FeedContentPreview() {
    // Create mock data here
    BandConnectTheme {
        FeedContent(
            posts = emptyList(),
            userMap = emptyMap(),
            onBandMemberClick = {},
            onLikeClick = {},
            likedPosts = emptySet()
        )
    }
}
