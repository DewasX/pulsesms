package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MessagesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MessagesViewModel = viewModel()
            val isDynamicTheme by viewModel.isDynamicTheme.collectAsState()
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()

            MyApplicationTheme(dynamicColor = isDynamicTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        // 1. Splash Screen
                        composable("splash") {
                            SplashScreen(
                                isOnboardingCompleted = isOnboardingCompleted,
                                onNavigateToNext = { destination ->
                                    navController.navigate(destination) {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. Onboarding / Permissions Section
                        composable("onboarding") {
                            OnboardingScreen(
                                onOnboardingDone = {
                                    viewModel.completeOnboarding()
                                    navController.navigate("inbox") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. Inbox Conversations Hub
                        composable("inbox") {
                            InboxScreen(
                                viewModel = viewModel,
                                onNavigateToChat = { id ->
                                    navController.navigate("conversation/$id")
                                },
                                onNavigateToContactPicker = {
                                    navController.navigate("contact_picker")
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                },
                                onNavigateToArchived = {
                                    navController.navigate("archived")
                                },
                                onNavigateToSpam = {
                                    navController.navigate("spam")
                                },
                                onNavigateToScheduled = {
                                    navController.navigate("scheduled")
                                },
                                onNavigateToLinkedDevices = {
                                    navController.navigate("linked_devices")
                                },
                                onNavigateToPromo = {
                                    navController.navigate("play_store_promo")
                                },
                                onNavigateToSearch = {
                                    navController.navigate("search")
                                }
                            )
                        }

                        // 4. Conversation Thread Panel
                        composable(
                            route = "conversation/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("id") ?: 0
                            ConversationScreen(
                                viewModel = viewModel,
                                conversationId = id,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 5. Create Thread Contact Picker
                        composable("contact_picker") {
                            ContactPickerScreen(
                                viewModel = viewModel,
                                onNavigateToChat = { id ->
                                    navController.navigate("conversation/$id") {
                                        popUpTo("contact_picker") { inclusive = true }
                                    }
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 6. Messages Settings Page
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 6b. Play Store Promotional Showcase Page
                        composable("play_store_promo") {
                            PlayStorePromoScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 7. Archived Conversations Board
                        composable("archived") {
                            ArchivedScreen(
                                viewModel = viewModel,
                                onNavigateToChat = { id ->
                                    navController.navigate("conversation/$id")
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 8. Spam details Block Center
                        composable("spam") {
                            SpamCenterScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 9. Scheduled message dispatcher queue
                        composable("scheduled") {
                            ScheduledScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 10. Companion linked paired devices page
                        composable("linked_devices") {
                            LinkedDevicesScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 11. Custom Search board
                        composable("search") {
                            SearchScreen(
                                viewModel = viewModel,
                                onNavigateToChat = { id ->
                                    navController.navigate("conversation/$id")
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
