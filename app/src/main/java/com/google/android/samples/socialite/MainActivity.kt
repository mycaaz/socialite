package com.google.android.samples.socialite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.samples.socialite.data.repository.UserRepository
import com.google.android.samples.socialite.ui.navigation.BandConnectDestinations
import com.google.android.samples.socialite.ui.navigation.BandConnectNavGraph
import com.google.android.samples.socialite.ui.screens.admin.AdminPanelScreen
import com.google.android.samples.socialite.ui.screens.chat.ConversationsScreen
import com.google.android.samples.socialite.ui.screens.chat.MessageScreen
import com.google.android.samples.socialite.ui.screens.discover.DiscoverBandMembersScreen
import com.google.android.samples.socialite.ui.screens.main.MainScreen
import com.google.android.samples.socialite.ui.screens.profile.BandMemberProfileScreen
import com.google.android.samples.socialite.ui.screens.welcome.WelcomeScreen
import com.google.android.samples.socialite.ui.theme.BandConnectTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BandConnectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    val currentUser by userRepository.currentUser.collectAsState(initial = null)

                    // Use the main app flow if logged in, otherwise show authentication flow
                    NavHost(
                        navController = navController,
                        startDestination = if (currentUser != null) "main" else "auth"
                    ) {
                        // Authentication flow
                        composable("auth") {
                            WelcomeScreen(
                                onLoginClick = {
                                    // In a real app, this would navigate to login
                                    // For demo purposes, we'll simulate a login
                                    navController.navigate("main") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                },
                                onSignupClick = {
                                    // In a real app, this would navigate to signup
                                    // For demo purposes, we'll simulate a login
                                    navController.navigate("main") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Main app flow
                        composable("main") {
                            MainScreen(
                                onNavigateToProfile = { bandMemberId ->
                                    navController.navigate("profile/$bandMemberId")
                                },
                                onNavigateToMessage = { recipientId ->
                                    navController.navigate("message/$recipientId")
                                },
                                onNavigateToAuth = {
                                    navController.navigate("auth") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Profile screen
                        composable(
                            "profile/{bandMemberId}",
                            arguments = listOf(navArgument("bandMemberId") {
                                defaultValue = ""
                            })
                        ) { backStackEntry ->
                            val bandMemberId = backStackEntry.arguments?.getString("bandMemberId") ?: ""

                            BandMemberProfileScreen(
                                bandMemberId = bandMemberId,
                                onBackClick = { navController.navigateUp() },
                                onMessageClick = { recipientId ->
                                    navController.navigate("message/$recipientId")
                                },
                                onShareLocationClick = { /* Handle location sharing */ }
                            )
                        }

                        // Message screen
                        composable(
                            "message/{recipientId}",
                            arguments = listOf(navArgument("recipientId") {
                                defaultValue = ""
                            })
                        ) { backStackEntry ->
                            val recipientId = backStackEntry.arguments?.getString("recipientId") ?: ""

                            MessageScreen(
                                recipientId = recipientId,
                                onBackClick = { navController.navigateUp() },
                                onShareLocationClick = { /* Handle location sharing */ }
                            )
                        }

                        // Admin panel
                        composable("admin") {
                            AdminPanelScreen(
                                onBackClick = { navController.navigateUp() },
                                onManagePostsClick = { /* Navigate to manage posts */ },
                                onManageEventsClick = { /* Navigate to manage events */ },
                                onFanInsightsClick = { /* Navigate to fan insights */ }
                            )
                        }
                    }
                }
            }
        }
    }
}
