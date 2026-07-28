package com.appcodecraft.linkzary.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.appcodecraft.linkzary.data.database.LinkzaryDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Date

@HiltWorker
class LinkHealthWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: LinkzaryDatabase,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "link_health_check"
        private const val TAG = "LinkHealthWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val savedLinkDao = database.savedLinkDao()
            val linksToCheck = savedLinkDao.getLinksNeedingHealthCheck(
                Date(System.currentTimeMillis() - 24 * 3600_000L)
            )

            if (linksToCheck.isEmpty()) {
                return@withContext Result.success()
            }

            var checked = 0
            linksToCheck.forEach { link ->
                try {
                    val request = Request.Builder()
                        .url(link.url)
                        .head()
                        .build()

                    val response = okHttpClient.newCall(request).execute()
                    val isAlive = response.code !in 400..499
                    response.close()

                    savedLinkDao.updateLinkHealth(link.id, isAlive, Date())
                    checked++
                } catch (e: Exception) {
                    Log.w(TAG, "Network error checking ${link.url}: ${e.message}")
                    checked++
                }
            }

            Log.d(TAG, "Checked $checked/${linksToCheck.size} links")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Link health check failed", e)
            Result.retry()
        }
    }
}
