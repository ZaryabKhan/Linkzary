package com.appcodecraft.linkzary.data.service

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.client.http.InputStreamContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backs up and restores all links, collections and tags to the user's own
 * Google Drive, inside the hidden app-data folder that only Linkzary can see.
 * The app never reads any other Drive content.
 */
@Singleton
class DriveBackupService @Inject constructor() {

    companion object {
        const val BACKUP_FILE_NAME = "linkzary_backup.json"
        const val RC_SIGN_IN = 7101
        const val RC_AUTH_PERMISSION = 7102
    }

    private fun signInClient(context: Context): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, options)
    }

    /** The signed-in account, or null when the user has not connected yet. */
    fun getSignedInAccount(context: Context): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)

    /**
     * Launches the Google account picker / consent screen.
     * The result arrives in [android.app.Activity.onActivityResult] with [RC_SIGN_IN].
     */
    fun launchSignIn(activity: Activity) {
        activity.startActivityForResult(signInClient(activity).signInIntent, RC_SIGN_IN)
    }

    /** Signs the user out of Drive backup. */
    fun signOut(context: Context, onComplete: () -> Unit = {}) {
        signInClient(context).signOut().addOnCompleteListener { onComplete() }
    }

    private fun driveService(context: Context, account: GoogleSignInAccount): Drive? {
        // On current Play services GoogleSignInAccount.getAccount() can come
        // back null; rebuild the android Account from the email in that case.
        val androidAccount = account.account
            ?: account.email?.let { android.accounts.Account(it, "com.google") }
            ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(context, setOf(DriveScopes.DRIVE_APPDATA))
            .setSelectedAccount(androidAccount)
        return Drive.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
            .setApplicationName("Linkzary")
            .build()
    }

    /**
     * Uploads [json] (a serialized [com.appcodecraft.linkzary.data.model.ExportData])
     * as the backup, replacing any previous backup.
     * Returns null on success, or "signin"/"permission" when user action is needed.
     */
    suspend fun backup(
        context: Context,
        json: String,
        onAuthIntent: (android.content.Intent) -> Unit = {}
    ): String? = withContext(Dispatchers.IO) {
        val account = getSignedInAccount(context) ?: return@withContext "signin"
        val drive = driveService(context, account) ?: return@withContext "signin"
        try {
            val files = drive.files()
                .list()
                .setSpaces("appDataFolder")
                .setQ("name = '$BACKUP_FILE_NAME'")
                .setFields("files(id, name)")
                .execute()

            val metadata = File()
                .setName(BACKUP_FILE_NAME)
                .setParents(listOf("appDataFolder"))

            val content = InputStreamContent("application/json",
                java.io.ByteArrayInputStream(json.toByteArray(Charsets.UTF_8)))
            val existingId = files.files.firstOrNull()?.id
            if (existingId != null) {
                drive.files().update(existingId, metadata, content).execute()
            } else {
                drive.files().create(metadata, content).execute()
            }
            null
        } catch (e: UserRecoverableAuthIOException) {
            // User must grant the Drive permission in the shown dialog
            onAuthIntent(e.intent)
            "permission"
        } catch (e: Exception) {
            e.printStackTrace()
            // Include the exception class so device-test reports identify the
            // actual failure (token fetch, network, HTTP 403, ...) without adb.
            "error: ${e.javaClass.simpleName}: ${e.message ?: "unknown"}"
        }
    }

    /**
     * Downloads the backup file's JSON content. Every outcome is explicit so
     * the UI can surface it — restore failures used to be swallowed as null.
     */
    sealed class DriveRestoreResult {
        data class Success(val json: String) : DriveRestoreResult()
        data object NeedsSignIn : DriveRestoreResult()
        data object NeedsPermission : DriveRestoreResult()
        data object NoBackup : DriveRestoreResult()
        data class Error(val message: String) : DriveRestoreResult()
    }

    suspend fun restore(
        context: Context,
        onAuthIntent: (android.content.Intent) -> Unit = {}
    ): DriveRestoreResult = withContext(Dispatchers.IO) {
        val account = getSignedInAccount(context)
            ?: return@withContext DriveRestoreResult.NeedsSignIn
        val drive = driveService(context, account)
            ?: return@withContext DriveRestoreResult.Error(
                "Google account has no usable email for Drive access"
            )
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
        } catch (e: UserRecoverableAuthIOException) {
            e.intent?.let { onAuthIntent(it) }
            DriveRestoreResult.NeedsPermission
        } catch (e: Exception) {
            e.printStackTrace()
            DriveRestoreResult.Error("${e.javaClass.simpleName}: ${e.message ?: "unknown"}")
        }
    }
}
