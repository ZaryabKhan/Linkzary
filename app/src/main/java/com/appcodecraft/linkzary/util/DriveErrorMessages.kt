package com.appcodecraft.linkzary.util

import com.google.api.client.googleapis.json.GoogleJsonResponseException

/**
 * Pure, JVM-testable user-facing error text for the Drive backup path.
 * Kept Android-type-free so plain unit tests can load this class
 * (a companion object holding GMS types fails to initialize on the JVM).
 */
object DriveErrorMessages {

    /**
     * Maps a GMS ApiException status code to actionable user text.
     * Covered by DriveErrorMessagesTest.
     */
    fun authErrorMessage(statusCode: Int): String = when (statusCode) {
        // ConnectionResult.PLAY_SERVICES_*
        1 -> "Google Play services is needed for Drive backup — enable or update it and try again."
        2 -> "A newer version of Google Play services is required — update it and try again."
        3 -> "Google Play services is temporarily unavailable — try again shortly."
        4 -> "Google Play services is out of date — update it and try again."
        // Network / server-side
        7 -> "Network error while talking to Google — check your connection and try again."
        // DEVELOPER_ERROR: app identity (SHA-1) not registered in the OAuth client
        10 -> "Google rejected this app's signature setup — reconnect your Google account and try again."
        // Interrupted / cancelled
        13 -> "Google sign-in was interrupted — please try again."
        14 -> "Google sign-in is updating — please try again in a moment."
        15 -> "Sign-in was cancelled — please try again when ready."
        16 -> "Sign-in was cancelled — please try again when ready."
        17 -> "Google Play services is still starting — try again in a moment."
        18 -> "Google Play services needs to update — follow the prompt and try again."
        // Auth-layer failures
        20 -> "Google sign-in is loading — try again in a moment."
        22 -> "Drive access was revoked — reconnect your account from the Drive card."
        else -> "Google sign-in error (code $statusCode) — please try again."
    }

    /**
     * Maps a Drive/HTTP/IO failure to actionable user text — no bare
     * exception class names on the card. Covered by DriveErrorMessagesTest.
     */
    fun driveFailureMessage(e: Exception, default: String): String = when (e) {
        is GoogleJsonResponseException -> when (e.statusCode) {
            401 -> "Drive session expired — please try again."
            403 -> "Drive access was revoked — reconnect your account from the Drive card."
            404 -> "The backup file is missing on Google Drive."
            in 500..599 -> "Google Drive is temporarily unavailable — try again later."
            else -> "Google Drive error (HTTP ${e.statusCode}) — please try again."
        }
        is java.io.IOException -> "Network error — check your connection and try again."
        else -> default
    }
}
