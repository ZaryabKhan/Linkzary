package com.appcodecraft.linkzary.ui.component

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * M3 haptics (v4.1) — one gate, one helper. Every haptic in the app goes
 * through [LinkzaryHaptics.perform] so the Settings "Haptic feedback" switch
 * gates them all in one place, and nothing vibrates on scroll.
 *
 * [LinkzaryHaptic.TICK] (CLOCK_TICK) for light selection taps: cards, toggles,
 * nav items. [LinkzaryHaptic.CONFIRM] for commitments: FAB press, dialog
 * confirm, delete. CONFIRM is API 30+; below that CLOCK_TICK carries it.
 */
enum class LinkzaryHaptic { TICK, CONFIRM }

object LinkzaryHaptics {
    /** Mirrors UserPreferencesManager.hapticsEnabled; synced in MainActivity. */
    @Volatile
    var enabled: Boolean = true

    fun perform(view: View, kind: LinkzaryHaptic) {
        if (!enabled) return
        val constant = when (kind) {
            LinkzaryHaptic.TICK -> HapticFeedbackConstants.CLOCK_TICK
            LinkzaryHaptic.CONFIRM ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    HapticFeedbackConstants.CONFIRM
                } else {
                    HapticFeedbackConstants.CLOCK_TICK
                }
        }
        view.performHapticFeedback(constant)
    }
}

/** Returns a stable fire callback for the current view; call it, don't hold it. */
@Composable
fun rememberLinkzaryHaptics(): (LinkzaryHaptic) -> Unit {
    val view = LocalView.current
    return remember(view) { { kind -> LinkzaryHaptics.perform(view, kind) } }
}
