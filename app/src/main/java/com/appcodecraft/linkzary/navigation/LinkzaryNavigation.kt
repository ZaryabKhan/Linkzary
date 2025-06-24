package com.appcodecraft.linkzary.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.appcodecraft.linkzary.ui.screen.collections.CollectionsScreen
import com.appcodecraft.linkzary.ui.screen.collections.CollectionDetailScreen
import com.appcodecraft.linkzary.ui.screen.home.HomeScreen
import com.appcodecraft.linkzary.ui.screen.settings.SettingsScreen

@Composable
fun LinkzaryNavigation(
    navController: NavHostController,
    sharedUrl: String? = null,
    modifier: Modifier
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
            SettingsScreen()
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
}