package com.appcodecraft.linkzary.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.worker.ReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules per-link revisit reminders and creates their notification channel.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            ReminderWorker.CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.reminder_channel_description)
        }
        ContextCompat.getSystemService(context, NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    /** Schedules a one-shot notification for [linkId] at [triggerAtMillis]. */
    fun scheduleReminder(linkId: Long, triggerAtMillis: Long) {
        val delay = (triggerAtMillis - System.currentTimeMillis()).coerceAtLeast(0)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.EXTRA_LINK_ID to linkId))
            .addTag(ReminderWorker.WORK_NAME_PREFIX + linkId)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ReminderWorker.WORK_NAME_PREFIX + linkId,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /** Cancels any pending reminder for [linkId]. */
    fun cancelReminder(linkId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(
            ReminderWorker.WORK_NAME_PREFIX + linkId
        )
    }
}
