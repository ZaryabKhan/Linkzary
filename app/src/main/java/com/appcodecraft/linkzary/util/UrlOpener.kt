package com.appcodecraft.linkzary.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Opens external URLs without ever crashing the app.
 *
 * Replaces Compose's [androidx.compose.ui.platform.UriHandler] on every
 * link-opening path: uriHandler.openUri throws IllegalArgumentException /
 * ActivityNotFoundException on malformed, scheme-less or whitespace-padded
 * input (the v1.1.5 crash cluster). Here every failure is caught and reported
 * to the caller so the UI can show a toast instead of dying.
 */
object UrlOpener {

    /**
     * Opens [rawUrl] in an external app (browser). Returns true when the
     * launch succeeded, false when it was caught and logged.
     */
    fun open(context: Context, rawUrl: String): Boolean {
        val url = rawUrl.trim().let { trimmed ->
            if (trimmed.contains("://")) trimmed else "https://$trimmed"
        }
        return try {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (t: Throwable) {
            android.util.Log.e("UrlOpener", "Failed to open URL: $rawUrl", t)
            false
        }
    }

    /** Opens [rawUrl] and shows a toast when the launch failed. */
    fun openOrToast(context: Context, rawUrl: String) {
        if (!open(context, rawUrl)) {
            Toast.makeText(
                context,
                context.getString(com.appcodecraft.linkzary.R.string.error_cannot_open_link),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
