package com.appcodecraft.linkzary.data.service

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.ClearTokenRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.api.client.http.InputStreamContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backs up and restores all links, collections and tags to the user's own
 * Google Drive, inside the hidden app-data folder that only Linkzary can see.
 * The app never reads any other Drive content.
 *
 * Auth (v1.2.1): the deprecated GoogleAuthUtil/GoogleAccountCredential token
 * path — whose GMS-side failures surfaced as bare "key error" messages — is
 * replaced by Google Identity AuthorizationClient. Account picking stays on
 * legacy GoogleSignIn; Drive access goes through authorize() → access token →
 * HttpCredentialsAdapter on the Drive REST client.
 */
@Singleton
class DriveBackupService @Inject constructor() {

    companion object {
        const val BACKUP_FILE_NAME = "linkzary_backup.json"
        const val RC_SIGN_IN = 7101
        val DRIVE_SCOPE = Scope(DriveScopes.DRIVE_APPDATA)

        /**
         * Maps a GMS ApiException status code to actionable user text.
         * Pure — covered by DriveErrorMessagesTest.
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
         * exception class names on the card. Pure — covered by tests.
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

    /** Result of the AuthorizationClient step for one Drive operation. */
    internal sealed class DriveAuthStep {
        /** Authorized — carry this access token to the Drive REST client. */
        data class Ready(val accessToken: String) : DriveAuthStep()

        /** User consent needed — surface this IntentSender to the UI launcher. */
        data class NeedsConsent(val intentSender: IntentSender) : DriveAuthStep()

        /** Failed — [userMessage] is safe to show on the Drive card. */
        data class Failed(val userMessage: String) : DriveAuthStep()
    }

    private fun signInClient(context: Context): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(DRIVE_SCOPE)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, options)
    }

    /** The signed-in account, or null when the user has not connected yet. */
    fun getSignedInAccount(context: Context): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)

    /**
     * Launches the Google account picker / consent screen.
     * The result arrives at the sign-in launcher registered in SettingsScreen.
     */
    fun launchSignIn(activity: Activity) {
        activity.startActivityForResult(signInClient(activity).signInIntent, RC_SIGN_IN)
    }

    /** Signs the user out of Drive backup and revokes the granted scopes. */
    fun signOut(context: Context, onComplete: () -> Unit = {}) {
        val account = getSignedInAccount(context)?.account
        if (account != null) {
            try {
                Identity.getAuthorizationClient(context).revokeAccess(
                    RevokeAccessRequest.builder()
                        .setAccount(account)
                        .setScopes(listOf(DRIVE_SCOPE))
                        .build()
                )
            } catch (_: Exception) {
                // Revocation is best-effort; local sign-out always proceeds.
            }
        }
        signInClient(context).signOut().addOnCompleteListener { onComplete() }
    }

    /**
     * Drive REST client authorized by an AuthorizationClient access token
     * (HttpCredentialsAdapter replaces the GoogleAccountCredential path).
     */
    private fun driveService(accessToken: String): Drive =
        Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(
                GoogleCredentials.newBuilder()
                    .setAccessToken(AccessToken(accessToken, null))
                    .build()
            )
        )
            .setApplicationName("Linkzary")
            .build()

    /**
     * Runs AuthorizationClient.authorize() for drive.appdata. If the user has
     * previously granted access this returns Ready with a fresh token;
     * otherwise NeedsConsent carries the PendingIntent's IntentSender for the
     * UI launcher. Failures map to actionable text — never raw class names.
     */
    private fun authorizeDrive(context: Context): DriveAuthStep {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(DRIVE_SCOPE))
            .build()
        return try {
            val result = Tasks.await(Identity.getAuthorizationClient(context).authorize(request))
            val token = result.accessToken
            val pending = result.pendingIntent
            when {
                token != null -> DriveAuthStep.Ready(token)
                pending != null -> DriveAuthStep.NeedsConsent(pending.intentSender)
                else -> DriveAuthStep.Failed("Drive access is not ready — please try again.")
            }
        } catch (e: com.google.android.gms.common.api.ApiException) {
            DriveAuthStep.Failed(authErrorMessage(e.statusCode))
        } catch (e: Exception) {
            android.util.Log.e("DriveBackup", "authorize failed", e)
            DriveAuthStep.Failed("Drive access is not ready — please try again.")
        }
    }

    /** Drops an unusable access token from GMS's cache so the next run re-fetches. */
    private fun clearCachedToken(context: Context, accessToken: String) {
        try {
            Tasks.await(
                Identity.getAuthorizationClient(context)
                    .clearToken(ClearTokenRequest.builder().setToken(accessToken).build())
            )
        } catch (_: Exception) {
            // Best-effort; the retry path handles the fresh token case.
        }
    }

    /**
     * Uploads [json] (a serialized [com.appcodecraft.linkzary.data.model.ExportData])
     * as the backup, replacing any previous backup.
     * Returns null on success, "signin"/"permission" when user action is needed
     * ([onConsentNeeded] receives the IntentSender to launch), or
     * "error: <actionable message>".
     */
    suspend fun backup(
        context: Context,
        json: String,
        onConsentNeeded: (IntentSender) -> Unit = {}
    ): String? = withContext(Dispatchers.IO) {
        val account = getSignedInAccount(context) ?: return@withContext "signin"
        when (val step = authorizeDrive(context)) {
            is DriveAuthStep.Ready -> {
                val drive = driveService(step.accessToken)
                try {
                    val files = drive.files()
                        .list()
                        .setSpaces("appDataFolder")
                        .setQ("name = '$BACKUP_FILE_NAME'")
                        .setFields("files(id, name)")
                        .execute()

                    val content = InputStreamContent("application/json",
                        ByteArrayInputStream(json.toByteArray(Charsets.UTF_8)))
                    val existingId = files.files.firstOrNull()?.id
                    if (existingId != null) {
                        // parents is not writable on update requests (Drive API returns
                        // 403 fieldNotWritable); only create() may set it.
                        val metadata = File().setName(BACKUP_FILE_NAME)
                        drive.files().update(existingId, metadata, content).execute()
                    } else {
                        val metadata = File()
                            .setName(BACKUP_FILE_NAME)
                            .setParents(listOf("appDataFolder"))
                        drive.files().create(metadata, content).execute()
                    }
                    null
                } catch (e: GoogleJsonResponseException) {
                    if (e.statusCode == 401) clearCachedToken(context, step.accessToken)
                    e.printStackTrace()
                    "error: ${driveFailureMessage(e, "Backup failed — please try again.")}"
                } catch (e: Exception) {
                    e.printStackTrace()
                    "error: ${driveFailureMessage(e, "Backup failed — please try again.")}"
                }
            }
            is DriveAuthStep.NeedsConsent -> {
                onConsentNeeded(step.intentSender)
                "permission"
            }
            is DriveAuthStep.Failed -> "error: ${step.userMessage}"
        }
    }

    /**
     * Downloads the backup file's JSON content. Every outcome is explicit so
     * the UI can surface it — restore failures used to be swallowed as null.
     */
    sealed class DriveRestoreResult {
        data class Success(val json: String) : DriveRestoreResult()
        data object NeedsSignIn : DriveRestoreResult()
        data class NeedsPermission(val intentSender: IntentSender? = null) : DriveRestoreResult()
        data object NoBackup : DriveRestoreResult()
        data class Error(val message: String) : DriveRestoreResult()
    }

    suspend fun restore(
        context: Context,
        onConsentNeeded: (IntentSender) -> Unit = {}
    ): DriveRestoreResult = withContext(Dispatchers.IO) {
        val account = getSignedInAccount(context)
            ?: return@withContext DriveRestoreResult.NeedsSignIn
        when (val step = authorizeDrive(context)) {
            is DriveAuthStep.Ready -> {
                val drive = driveService(step.accessToken)
                try {
                    val files = drive.files()
                        .list()
                        .setSpaces("appDataFolder")
                        .setQ("name = '$BACKUP_FILE_NAME'")
                        .setFields("files(id, name)")
                        .execute()
                    val fileId = files.files.firstOrNull()?.id
                        ?: return@withContext DriveRestoreResult.NoBackup

                    val json = drive.files().get(fileId).executeMediaAsInputStream().use { stream ->
                        stream.bufferedReader(Charsets.UTF_8).readText()
                    }
                    DriveRestoreResult.Success(json)
                } catch (e: GoogleJsonResponseException) {
                    if (e.statusCode == 401) clearCachedToken(context, step.accessToken)
                    e.printStackTrace()
                    DriveRestoreResult.Error(driveFailureMessage(e, "Restore failed — please try again."))
                } catch (e: Exception) {
                    e.printStackTrace()
                    DriveRestoreResult.Error(driveFailureMessage(e, "Restore failed — please try again."))
                }
            }
            is DriveAuthStep.NeedsConsent -> {
                onConsentNeeded(step.intentSender)
                DriveRestoreResult.NeedsPermission(step.intentSender)
            }
            is DriveAuthStep.Failed -> DriveRestoreResult.Error(step.userMessage)
        }
    }
}
