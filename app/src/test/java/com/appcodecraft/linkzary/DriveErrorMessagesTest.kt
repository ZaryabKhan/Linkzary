package com.appcodecraft.linkzary

import com.appcodecraft.linkzary.util.DriveErrorMessages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Pins the user-facing error mapping for the Drive backup auth/data path so
 * the card never shows bare exception class names (the v1.2.0 "key error"
 * regression) and every GMS/HTTP failure gets actionable text.
 */
class DriveErrorMessagesTest {

    @Test
    fun `auth error codes map to actionable text`() {
        // DEVELOPER_ERROR: app identity not registered in the OAuth client.
        assertTrue(DriveErrorMessages.authErrorMessage(10).contains("signature"))
        // Sign-in interrupted / cancelled states never blame the user.
        assertTrue(DriveErrorMessages.authErrorMessage(13).contains("try again"))
        assertTrue(DriveErrorMessages.authErrorMessage(15).contains("cancelled"))
        // Revoked access points back to reconnecting on the Drive card.
        assertTrue(DriveErrorMessages.authErrorMessage(22).contains("reconnect"))
        // Play services availability issues never show a bare code.
        assertTrue(DriveErrorMessages.authErrorMessage(4).contains("Google Play services"))
    }

    @Test
    fun `unknown auth code still mentions the code for support`() {
        val msg = DriveErrorMessages.authErrorMessage(9999)
        assertTrue(msg.contains("9999"))
        assertTrue(msg.contains("try again"))
    }

    @Test
    fun `io failures map to network text`() {
        val msg = DriveErrorMessages.driveFailureMessage(
            IOException("socket failed: ECONNRESET"),
            default = "Backup failed — please try again."
        )
        assertTrue(msg.contains("Network error"))
    }

    @Test
    fun `generic throwable never leaks its class name`() {
        // The v1.2.0 failure surfaced as "IllegalArgumentException: key error".
        val msg = DriveErrorMessages.driveFailureMessage(
            IllegalArgumentException("key error"),
            default = "Backup failed — please try again."
        )
        assertFalse(msg.contains("IllegalArgumentException"))
        assertFalse(msg.contains("key error"))
        assertEquals("Backup failed — please try again.", msg)
    }
}
