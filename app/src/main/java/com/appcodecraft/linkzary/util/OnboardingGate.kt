package com.appcodecraft.linkzary.util

/**
 * Pure decision logic for the versionCode-gated onboarding (spec §7):
 * pages show on first launch (never onboarded) and once per new app
 * version (current > last onboarded). Kept free of Android classes so it
 * is directly unit-testable in OnboardingGateTest.
 */
object OnboardingGate {

    /**
     * @param lastOnboardedVersionCode persisted versionCode of the last
     *        completed onboarding, 0 when the user never saw it.
     * @param currentVersionCode the running app's versionCode.
     */
    fun shouldShow(lastOnboardedVersionCode: Int, currentVersionCode: Int): Boolean {
        if (currentVersionCode <= 0) return false
        return lastOnboardedVersionCode < currentVersionCode
    }
}
