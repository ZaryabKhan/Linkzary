package com.appcodecraft.linkzary.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            HomeScreen(
                navController = navController,
                sharedUrl = sharedUrl
            )
        }
        
        composable(
            route = Screen.Collections.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            CollectionsScreen(
                navController = navController
            )
        }
        
        composable(
            route = Screen.CollectionDetail.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(400))
            }
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
            CollectionDetailScreen(
                collectionId = collectionId,
                navController = navController
            )
        }
        
        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            }
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