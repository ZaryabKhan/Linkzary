package com.appcodecraft.linkzary

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.appcodecraft.linkzary.data.preferences.UserPreferencesManager
import com.appcodecraft.linkzary.navigation.LinkzaryNavigation
import com.appcodecraft.linkzary.navigation.Screen
import com.appcodecraft.linkzary.ui.component.LinkzaryBottomNavigationBar
import com.appcodecraft.linkzary.ui.component.LinkzaryHaptics
import com.appcodecraft.linkzary.ui.screen.onboarding.OnboardingScreen
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
                // Keep the global haptics gate in step with the persisted
                // preference (v4.1). StateFlow's current value means the gate
                // is correct from the first frame, before any collection.
                val hapticsEnabled by userPreferencesManager.hapticsEnabled.collectAsState()
                androidx.compose.runtime.LaunchedEffect(hapticsEnabled) {
                    LinkzaryHaptics.enabled = hapticsEnabled
                }
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

    // Onboarding gate (spec §7): show on first launch and once per new
    // versionCode, gated by the persisted flag via OnboardingGate.
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentVersionCode = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
        } catch (e: Exception) {
            0
        }
    }
    val lastOnboardedVersionCode by userPreferencesManager.lastOnboardedVersionCode.collectAsState()
    val showOnboarding = com.appcodecraft.linkzary.util.OnboardingGate.shouldShow(
        lastOnboardedVersionCode = lastOnboardedVersionCode,
        currentVersionCode = currentVersionCode
    )

    // P0 crash fix: while onboarding is up, the NavHost below is NOT composed,
    // so the nav graph is never set and navigate() here crashed the app. The
    // donate tap now marks onboarding done and defers the navigation until the
    // graph exists (see OnboardingGate.deferWhileOnboarding).
    var pendingDonationNav by remember { androidx.compose.runtime.mutableStateOf(false) }
    if (showOnboarding) {
        OnboardingScreen(
            onFinish = { userPreferencesManager.markOnboarded(currentVersionCode) },
            onSupport = {
                userPreferencesManager.markOnboarded(currentVersionCode)
                pendingDonationNav = true
            }
        )
        return
    }

    if (pendingDonationNav) {
        androidx.compose.runtime.LaunchedEffect(pendingDonationNav) {
            if (com.appcodecraft.linkzary.util.OnboardingGate.deferWhileOnboarding(
                    showOnboarding = showOnboarding,
                    pendingDonationNav = pendingDonationNav
                )
            ) {
                navController.navigate(Screen.Donation.route)
            }
            pendingDonationNav = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            LinkzaryBottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        // Screens own their window insets: CollectionDetail's tonal header goes
        // full-bleed behind the status bar, TopAppBar screens handle the inset
        // themselves, and plain screens (Settings/Collections/Stats) apply
        // statusBarsPadding at their root. A Scaffold-level top inset here
        // double-padded those and left the status bar on the background color.
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LinkzaryNavigation(
            navController = navController,
            sharedUrl = sharedUrl,
            modifier = Modifier.padding(innerPadding),
            userPreferencesManager = userPreferencesManager
        )
    }
}