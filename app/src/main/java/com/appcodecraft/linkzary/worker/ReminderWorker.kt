package com.appcodecraft.linkzary.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appcodecraft.linkzary.MainActivity
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.data.repository.LinkRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Fires a "revisit this link" notification at the time the user scheduled,
 * then clears the reminder from the link.
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val linkRepository: LinkRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME_PREFIX = "link_reminder_"
        const val EXTRA_LINK_ID = "extra_link_id"
        const val CHANNEL_ID = "link_reminders"
    }

    override suspend fun doWork(): Result {
        val linkId = inputData.getLong(EXTRA_LINK_ID, -1L)
        if (linkId == -1L) return Result.failure()

        val link = linkRepository.getLinkById(linkId)
        linkRepository.updateReminderAt(linkId, null)

        if (link == null) return Result.success()

        val context = applicationContext

        // POST_NOTIFICATIONS is a runtime permission on Android 13+; if the
        // user denied it we silently skip instead of crashing.
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("openLinkId", linkId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, linkId.toInt(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(link.title.ifBlank { link.url })
            .setStyle(NotificationCompat.BigTextStyle().bigText(link.title.ifBlank { link.url }))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(linkId.toInt(), notification)
        return Result.success()
    }
}
