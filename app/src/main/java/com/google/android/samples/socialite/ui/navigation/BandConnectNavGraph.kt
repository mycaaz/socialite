package com.google.android.samples.socialite.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.samples.socialite.ui.screens.admin.AdminPanelScreen
import com.google.android.samples.socialite.ui.screens.chat.ConversationsScreen
import com.google.android.samples.socialite.ui.screens.chat.MessageScreen
import com.google.android.samples.socialite.ui.screens.discover.DiscoverBandMembersScreen
import com.google.android.samples.socialite.ui.screens.profile.BandMemberProfileScreen
import com.google.android.samples.socialite.ui.screens.welcome.WelcomeScreen

object BandConnectDestinations {
    const val WELCOME_ROUTE = "welcome"
    const val DISCOVER_ROUTE = "discover"
    const val PROFILE_ROUTE = "profile/{bandMemberId}"
    const val CONVERSATIONS_ROUTE = "conversations"
    const val MESSAGE_ROUTE = "message/{recipientId}"
    const val ADMIN_PANEL_ROUTE = "admin"

    // For navigation with arguments
    fun profileRoute(bandMemberId: String) = "profile/$bandMemberId"
    fun messageRoute(recipientId: String) = "message/$recipientId"
}

@Composable
fun BandConnectNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = BandConnectDestinations.WELCOME_ROUTE
) {
    val actions = remember(navController) { BandConnectNavigationActions(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(BandConnectDestinations.WELCOME_ROUTE) {
            WelcomeScreen(
                onLoginClick = { /* Navigate to login - would be implemented in real app */ },
                onSignupClick = { /* Navigate to signup - would be implemented in real app */ }
                // For demo purpose, we'll add a direct navigation to discover
                // This would be removed in a real implementation with proper auth
                // onSignupClick = actions.navigateToDiscover
            )
        }

        composable(BandConnectDestinations.DISCOVER_ROUTE) {
            DiscoverBandMembersScreen(
                onBandMemberClick = { bandMemberId -> actions.navigateToProfile(bandMemberId) },
                onMessageClick = { recipientId -> actions.navigateToMessage(recipientId) }
            )
        }

        composable(
            route = BandConnectDestinations.PROFILE_ROUTE,
            arguments = listOf(
                navArgument("bandMemberId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bandMemberId = backStackEntry.arguments?.getString("bandMemberId") ?: ""

            BandMemberProfileScreen(
                bandMemberId = bandMemberId,
                onBackClick = actions.upPress,
                onMessageClick = { actions.navigateToMessage(bandMemberId) },
                onShareLocationClick = { /* Handle location sharing */ }
            )
        }

        composable(BandConnectDestinations.CONVERSATIONS_ROUTE) {
            ConversationsScreen(
                onConversationClick = { recipientId -> actions.navigateToMessage(recipientId) },
                onNewMessageClick = { /* Navigate to new message screen */ }
            )
        }

        composable(
            route = BandConnectDestinations.MESSAGE_ROUTE,
            arguments = listOf(
                navArgument("recipientId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val recipientId = backStackEntry.arguments?.getString("recipientId") ?: ""

            MessageScreen(
                recipientId = recipientId,
                onBackClick = actions.upPress,
                onShareLocationClick = { /* Handle location sharing */ }
            )
        }

        composable(BandConnectDestinations.ADMIN_PANEL_ROUTE) {
            AdminPanelScreen(
                onBackClick = actions.upPress,
                onManagePostsClick = { /* Navigate to manage posts */ },
                onManageEventsClick = { /* Navigate to manage events */ },
                onFanInsightsClick = { /* Navigate to fan insights */ }
            )
        }
    }
}

class BandConnectNavigationActions(navController: NavHostController) {
    val navigateToDiscover: () -> Unit = {
        navController.navigate(BandConnectDestinations.DISCOVER_ROUTE) {
            popUpTo(BandConnectDestinations.WELCOME_ROUTE) { inclusive = true }
        }
    }

    val navigateToProfile = { bandMemberId: String ->
        navController.navigate(BandConnectDestinations.profileRoute(bandMemberId))
    }

    val navigateToConversations: () -> Unit = {
        navController.navigate(BandConnectDestinations.CONVERSATIONS_ROUTE)
    }

    val navigateToMessage = { recipientId: String ->
        navController.navigate(BandConnectDestinations.messageRoute(recipientId))
    }

    val navigateToAdminPanel: () -> Unit = {
        navController.navigate(BandConnectDestinations.ADMIN_PANEL_ROUTE)
    }

    val upPress: () -> Unit = {
        navController.navigateUp()
    }
}
