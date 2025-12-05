package com.appcodecraft.linkzary.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.appcodecraft.linkzary.data.preferences.UserPreferencesManager
import com.appcodecraft.linkzary.ui.screen.collections.CollectionDetailScreen
import com.appcodecraft.linkzary.ui.screen.collections.CollectionsScreen
import com.appcodecraft.linkzary.ui.screen.donation.DonationScreen
import com.appcodecraft.linkzary.ui.screen.home.HomeScreen
import com.appcodecraft.linkzary.ui.screen.settings.SettingsScreen

@Composable
fun LinkzaryNavigation(
    navController: NavHostController,
    sharedUrl: String? = null,
    modifier: Modifier,
    userPreferencesManager: UserPreferencesManager
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) {
            HomeScreen(
                navController = navController,
                sharedUrl = sharedUrl
            )
        }
        
        composable(
            route = Screen.Collections.route,
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) {
            CollectionsScreen(
                navController = navController
            )
        }
        
        composable(
            route = Screen.CollectionDetail.route,
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
            CollectionDetailScreen(
                collectionId = collectionId,
                navController = navController
            )
        }
        
        composable(
            route = Screen.Settings.route,
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) {
            SettingsScreen(
                userPreferencesManager = userPreferencesManager,
                onNavigateToDonation = {
                    navController.navigate(Screen.Donation.route)
                }
            )
        }
        
        composable(
            route = Screen.Donation.route,
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) {
            DonationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Reader.route,
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) { backStackEntry ->
            val linkId = backStackEntry.arguments?.getString("linkId")?.toLongOrNull()
            if (linkId != null) {
                // We need to fetch the link. 
                // Ideally passing the whole link might be cleaner but NavArg is okay.
                // Or we can get it from a shared ViewModel or fetching from Repository in ReaderScreen/ViewModel.
                // For simplicity, let's create a ReaderViewModel or pass logic.
                // Wait, I didn't create a ReaderViewModel. I should probably have one to fetch the link.
                // Or I can misuse HomeViewModel if I pass it, but better to fetch.
                
                // Let's quickly create a ReaderViewModel or simpler: 
                // Just fetch it in the Composable using a LaunchedEffect if we didn't make a VM.
                // But ReaderScreen expects a `SavedLink` object.
                // So I need to fetch it.
                
                // Let's modify LinkzaryNavigation to construct the ReaderScreen 
                // with a temporary loading state or fetch logic.
                
                // Actually, let's use hiltViewModel() inside ReaderScreen to fetch the link by ID.
                // I need to update ReaderScreen to accept linkId instead of SavedLink, OR fetching it.
                // Let's Go with: Modify ReaderScreen to take Link ID and fetch it.
                
                com.appcodecraft.linkzary.ui.screen.reader.ReaderScreenWrapper(
                    linkId = linkId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Collections : Screen("collections", "Collections")
    object CollectionDetail : Screen("collection_detail/{collectionId}", "Collection Detail") {
        fun createRoute(collectionId: String) = "collection_detail/$collectionId"
    }
    object Settings : Screen("settings", "Settings")
    object Donation : Screen("donation", "Donation")
    object Reader : Screen("reader/{linkId}", "Reader Mode") {
        fun createRoute(linkId: Long) = "reader/$linkId"
    }
}