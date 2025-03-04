package com.google.android.samples.socialite.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.samples.socialite.ui.screens.chat.ConversationsScreen
import com.google.android.samples.socialite.ui.screens.discover.DiscoverBandMembersScreen
import com.google.android.samples.socialite.ui.screens.feed.FeedScreen
import com.google.android.samples.socialite.ui.screens.profile.UserProfileScreen

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Feed : Screen("feed", Icons.Default.Home, "Feed")
    object Discover : Screen("discover", Icons.Default.Explore, "Discover")
    object Messages : Screen("messages", Icons.Default.Message, "Messages")
    object Profile : Screen("profile", Icons.Default.Person, "Profile")
}

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    onNavigateToProfile: (String) -> Unit,
    onNavigateToMessage: (String) -> Unit,
    onNavigateToAuth: () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Feed.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Feed.route) {
                FeedScreen(
                    onBandMemberClick = onNavigateToProfile
                )
            }
            composable(Screen.Discover.route) {
                DiscoverBandMembersScreen(
                    onBandMemberClick = onNavigateToProfile,
                    onMessageClick = onNavigateToMessage
                )
            }
            composable(Screen.Messages.route) {
                ConversationsScreen(
                    onConversationClick = onNavigateToMessage,
                    onNewMessageClick = { /* Navigate to band members to start new message */ }
                )
            }
            composable(Screen.Profile.route) {
                UserProfileScreen(
                    onLogoutClick = onNavigateToAuth
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        Screen.Feed,
        Screen.Discover,
        Screen.Messages,
        Screen.Profile
    )

    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}
