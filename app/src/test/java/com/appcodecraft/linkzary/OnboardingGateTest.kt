package com.appcodecraft.linkzary

import com.appcodecraft.linkzary.util.OnboardingGate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the versionCode-gated onboarding logic (spec §7):
 * pages show on first launch and once per new app version, never
 * twice per version.
 */
class OnboardingGateTest {

    @Test
    fun `first launch ever shows onboarding`() {
        // lastOnboardedVersionCode is 0 when never onboarded
        assertTrue(OnboardingGate.shouldShow(lastOnboardedVersionCode = 0, currentVersionCode = 16))
    }

    @Test
    fun `same version does not show onboarding again`() {
        assertFalse(OnboardingGate.shouldShow(lastOnboardedVersionCode = 16, currentVersionCode = 16))
    }

    @Test
    fun `app update shows onboarding once`() {
        // User onboarded on v16, update to v17 -> show once
        assertTrue(OnboardingGate.shouldShow(lastOnboardedVersionCode = 16, currentVersionCode = 17))
        // ...and after marking v17, not again on v17
        assertFalse(OnboardingGate.shouldShow(lastOnboardedVersionCode = 17, currentVersionCode = 17))
    }

    @Test
    fun `skipped versions show onboarding on next launch`() {
        // Marked v14, current is v18 (skipped updates 15-17)
        assertTrue(OnboardingGate.shouldShow(lastOnboardedVersionCode = 14, currentVersionCode = 18))
    }

    @Test
    fun `multiple version jumps across releases`() {
        val sequence = listOf(16, 17, 18, 20, 21)
        var last = 0
        for (current in sequence) {
            // first launch of each new version shows onboarding
            assertTrue(
                "should show for $current after $last",
                OnboardingGate.shouldShow(lastOnboardedVersionCode = last, currentVersionCode = current)
            )
            last = current
            // marking it suppresses any further shows on the same version
            assertFalse(
                "should not show twice for $current",
                OnboardingGate.shouldShow(lastOnboardedVersionCode = last, currentVersionCode = current)
            )
        }
    }

    @Test
    fun `non-positive current version never shows`() {
        // Defensive: a broken/absent versionCode must not gate the whole app
        assertFalse(OnboardingGate.shouldShow(lastOnboardedVersionCode = 0, currentVersionCode = 0))
        assertFalse(OnboardingGate.shouldShow(lastOnboardedVersionCode = 0, currentVersionCode = -1))
    }

    @Test
    fun `downgrade does not show onboarding`() {
        // Rollback installs: never re-show for an older versionCode
        assertFalse(OnboardingGate.shouldShow(lastOnboardedVersionCode = 18, currentVersionCode = 16))
    }

    @Test
    fun `donation navigation is deferred while onboarding is up`() {
        // P0 regression: tapping Support on onboarding crashed because the
        // nav graph isn't set while the NavHost is not composed. The nav
        // must never fire while onboarding shows.
        assertFalse(
            OnboardingGate.deferWhileOnboarding(showOnboarding = true, pendingDonationNav = true)
        )
    }

    @Test
    fun `pending donation navigation fires once onboarding dismissed`() {
        // The tap marked onboarding done; the deferred nav fires on the next
        // composition, then the pending flag clears (caller's job).
        assertTrue(
            OnboardingGate.deferWhileOnboarding(showOnboarding = false, pendingDonationNav = true)
        )
        assertFalse(
            OnboardingGate.deferWhileOnboarding(showOnboarding = false, pendingDonationNav = false)
        )
    }
}
