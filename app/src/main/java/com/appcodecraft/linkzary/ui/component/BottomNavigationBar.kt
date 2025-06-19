package com.appcodecraft.linkzary.ui.component

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.appcodecraft.linkzary.navigation.Screen
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme

data class BottomNavItem(
    val screen: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        screen = Screen.Collections,
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.Bookmark
    ),
    BottomNavItem(
        screen = Screen.Settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)

@Composable
fun LinkzaryBottomNavigationBar(
    navController: NavController,
    currentRoute: String?
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            
            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "iconScale"
            )
            
            val iconTint by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200),
                label = "iconTint"
            )
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.screen.title,
                        tint = iconTint,
                        modifier = Modifier.scale(iconScale)
                    )
                },
                label = { Text(item.screen.title) },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    LinkzaryTheme {
        LinkzaryBottomNavigationBar(
            navController = rememberNavController(),
            currentRoute = Screen.Home.route
        )
    }
}