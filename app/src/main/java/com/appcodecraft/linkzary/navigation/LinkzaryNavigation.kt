package com.appcodecraft.linkzary.navigation

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
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                sharedUrl = sharedUrl
            )
        }
        
        composable(Screen.Collections.route) {
            CollectionsScreen(
                navController = navController
            )
        }
        
        composable(Screen.CollectionDetail.route) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
            CollectionDetailScreen(
                collectionId = collectionId,
                navController = navController
            )
        }
        
        composable(Screen.Settings.route) {
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