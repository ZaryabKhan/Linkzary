package com.appcodecraft.linkzary.ui.component

// Animation imports removed for better performance
// Scale import removed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.navigation.Screen
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme

data class BottomNavItem(
    val screen: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val titleRes: Int
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        titleRes = R.string.navigation_home
    ),
    BottomNavItem(
        screen = Screen.Collections,
        selectedIcon = Icons.Filled.Folder,
        unselectedIcon = Icons.Outlined.Folder,
        titleRes = R.string.navigation_collections
    ),
    BottomNavItem(
        screen = Screen.Settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        titleRes = R.string.navigation_settings
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
            
            // Static values for better performance
            val iconTint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.titleRes),
                        tint = iconTint,
                        // Scale modifier removed for better performance
                    )
                },
                label = { Text(stringResource(item.titleRes)) },
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