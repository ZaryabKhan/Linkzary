package com.appcodecraft.linkzary

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appcodecraft.linkzary.data.preferences.UserPreferencesManager
import com.appcodecraft.linkzary.navigation.LinkzaryNavigation
import com.appcodecraft.linkzary.ui.component.LinkzaryBottomNavigationBar
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme
import com.appcodecraft.linkzary.utils.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        // Performance optimizations
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )

        setContent {
            LinkzaryTheme(userPreferencesManager = userPreferencesManager) {
                LinkzaryApp(
                    sharedUrl = null,
                    userPreferencesManager = userPreferencesManager
                )
            }
        }
    }

    // onNewIntent removed as sharing is handled by ShareActivity

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { context ->
            val userPreferencesManager = UserPreferencesManager(context)
            val savedLanguage = userPreferencesManager.getCurrentLanguage()
            LocaleHelper.setLocale(context, savedLanguage)
        })
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
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->
        LinkzaryNavigation(
            navController = navController,
            sharedUrl = sharedUrl,
            modifier = Modifier.padding(innerPadding),
            userPreferencesManager = userPreferencesManager
        )
    }
}