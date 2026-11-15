package com.appcodecraft.linkzary.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.appcodecraft.linkzary.data.preferences.UserPreferencesManager
import com.appcodecraft.linkzary.ui.screen.share.ShareScreen
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedUrl = when {
            intent.action == Intent.ACTION_SEND && intent.type == "text/plain" -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)
            }
            else -> null
        }

        if (sharedUrl.isNullOrBlank()) {
            finish()
            return
        }

        setContent {
            LinkzaryTheme(userPreferencesManager = userPreferencesManager) {
                // Haptics gate sync (v4.1) — same as MainActivity.
                val hapticsEnabled by userPreferencesManager.hapticsEnabled.collectAsState()
                androidx.compose.runtime.LaunchedEffect(hapticsEnabled) {
                    com.appcodecraft.linkzary.ui.component.LinkzaryHaptics.enabled = hapticsEnabled
                }
                ShareScreen(
                    sharedUrl = sharedUrl,
                    onFinish = {
                        finish()
                    }
                )
            }
        }
    }
}
