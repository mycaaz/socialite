package com.google.android.samples.socialite.ui.screens.admin

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.samples.socialite.R
import com.google.android.samples.socialite.model.QuickResponse
import com.google.android.samples.socialite.ui.components.LoadingScreen
import com.google.android.samples.socialite.ui.theme.BandConnectTheme

@Composable
fun AdminPanelScreen(
    viewModel: AdminPanelViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onManagePostsClick: () -> Unit,
    onManageEventsClick: () -> Unit,
    onFanInsightsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.admin_dashboard)) },
                backgroundColor = MaterialTheme.colors.primary
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingScreen()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colors.background)
            ) {
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Dashboard") }
                    )

                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Quick Replies") }
                    )

                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = { Text("Settings") }
                    )
                }

                // Tab Content
                when (selectedTabIndex) {
                    0 -> AdminDashboardTab(
                        onManagePostsClick = onManagePostsClick,
                        onManageEventsClick = onManageEventsClick,
                        onFanInsightsClick = onFanInsightsClick
                    )
                    1 -> QuickRepliesTab(
                        quickResponses = uiState.quickResponses,
                        onAddQuickResponse = { category, content ->
                            viewModel.addQuickResponse(category, content)
                        },
                        onDeleteQuickResponse = { responseId ->
                            viewModel.deleteQuickResponse(responseId)
                        }
                    )
                    2 -> AdminSettingsTab()
                }
            }
        }
    }
}

@Composable
fun AdminDashboardTab(
    onManagePostsClick: () -> Unit,
    onManageEventsClick: () -> Unit,
    onFanInsightsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Admin Tools",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Admin Menu Options
        AdminMenuOption(
            icon = Icons.Default.Edit,
            title = "Manage Posts",
            description = "Create, edit, or delete band posts",
            onClick = onManagePostsClick
        )

        AdminMenuOption(
            icon = Icons.Default.Message,
            title = "Manage Events",
            description = "Create and manage upcoming events",
            onClick = onManageEventsClick
        )

        AdminMenuOption(
            icon = Icons.Default.Person,
            title = "Fan Insights",
            description = "View analytics about your fans",
            onClick = onFanInsightsClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Summary
        Text(
            text = "Quick Stats",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatCard(title = "Followers", value = "1,248")
            StatCard(title = "Messages", value = "32")
            StatCard(title = "Posts", value = "15")
        }
    }
}

@Composable
fun AdminMenuOption(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface
                )

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier
            .height(100.dp)
            .width(100.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun QuickRepliesTab(
    quickResponses: List<QuickResponse>,
    onAddQuickResponse: (String, String) -> Unit,
    onDeleteQuickResponse: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Reply Templates",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onBackground,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    // In a real implementation, this would show a dialog to add a new quick response
                    onAddQuickResponse("General", "Thank you for your message! I'll get back to you soon.")
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Quick Response",
                    tint = MaterialTheme.colors.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (quickResponses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No quick replies yet. Add your first one!",
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn {
                items(quickResponses.groupBy { it.category }) { (category, responses) ->
                    Text(
                        text = category,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    responses.forEach { response ->
                        QuickResponseItem(
                            response = response,
                            onDeleteClick = { onDeleteQuickResponse(response.id) }
                        )
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colors.onBackground.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickResponseItem(
    response: QuickResponse,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 1.dp,
        backgroundColor = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Reply,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = response.content,
                fontSize = 14.sp,
                color = MaterialTheme.colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colors.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AdminSettingsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Admin Settings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // In a real implementation, this would have actual settings
        SettingsOption(
            title = "Notification Settings",
            description = "Manage how and when you receive notifications"
        )

        SettingsOption(
            title = "Privacy Settings",
            description = "Control who can message you and view your profile"
        )

        SettingsOption(
            title = "Account Settings",
            description = "Manage your account details and preferences"
        )
    }
}

@Composable
fun SettingsOption(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { /* onClick implementation */ },
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface
                )

                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardTabPreview() {
    BandConnectTheme {
        AdminDashboardTab(
            onManagePostsClick = {},
            onManageEventsClick = {},
            onFanInsightsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QuickRepliesTabPreview() {
    val mockQuickResponses = listOf(
        QuickResponse(
            id = "1",
            bandMemberId = "user1",
            content = "Thanks for your message! I'll get back to you soon.",
            category = "General"
        ),
        QuickResponse(
            id = "2",
            bandMemberId = "user1",
            content = "We'll be in your city next month! Check our tour dates for more info.",
            category = "Events"
        ),
        QuickResponse(
            id = "3",
            bandMemberId = "user1",
            content = "I'm glad you enjoyed our latest album!",
            category = "Music"
        )
    )

    BandConnectTheme {
        QuickRepliesTab(
            quickResponses = mockQuickResponses,
            onAddQuickResponse = { _, _ -> },
            onDeleteQuickResponse = { }
        )
    }
}
