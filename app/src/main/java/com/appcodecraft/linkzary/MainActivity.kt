package com.appcodecraft.linkzary

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appcodecraft.linkzary.data.preferences.UserPreferencesManager
import com.appcodecraft.linkzary.navigation.LinkzaryNavigation
import com.appcodecraft.linkzary.ui.component.LinkzaryBottomNavigationBar
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Performance optimizations
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        
        // Handle shared URL from intent
        val sharedUrl = when {
            intent.action == Intent.ACTION_SEND && intent.type == "text/plain" -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)
            }
            intent.action == Intent.ACTION_VIEW -> {
                intent.dataString
            }
            else -> null
        }
        
        setContent {
            LinkzaryTheme(userPreferencesManager = userPreferencesManager) {
                LinkzaryApp(
                    sharedUrl = sharedUrl,
                    userPreferencesManager = userPreferencesManager
                )
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        
        // Handle new shared URL when app is already running
        val sharedUrl = when {
            intent.action == Intent.ACTION_SEND && intent.type == "text/plain" -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)
            }
            intent.action == Intent.ACTION_VIEW -> {
                intent.dataString
            }
            else -> null
        }
        
        if (!sharedUrl.isNullOrBlank()) {
            // Recreate the activity with the new shared URL
            recreate()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkzaryApp(
    sharedUrl: String? = null,
    userPreferencesManager: UserPreferencesManager
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            LinkzaryBottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LinkzaryNavigation(
            navController = navController,
            sharedUrl = sharedUrl,
            modifier = Modifier.padding(innerPadding),
            userPreferencesManager = userPreferencesManager
        )
    }
}