package com.appcodecraft.linkzary

import android.app.Application
import android.content.Context
import android.os.StrictMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.appcodecraft.linkzary.utils.LocaleHelper
import com.appcodecraft.linkzary.worker.LinkHealthWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class LinkzaryApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Enable StrictMode in debug builds for performance monitoring
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )

            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectActivityLeaks()
                    .penaltyLog()
                    .build()
            )
        }

        // Optimize coroutine scheduler pool for better background throughput
        System.setProperty("kotlinx.coroutines.scheduler.core.pool.size", "2")
        System.setProperty("kotlinx.coroutines.scheduler.max.pool.size", "8")

        // Schedule link health check worker
        scheduleLinkHealthCheck()
    }

    private fun scheduleLinkHealthCheck() {
        val workRequest = PeriodicWorkRequestBuilder<LinkHealthWorker>(
            12, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            LinkHealthWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun attachBaseContext(base: Context?) {
        // Apply locale early so all resources are correctly configured.
        // Read the language directly from SharedPreferences: constructing the
        // full preferences manager (and its StateFlows) inside a runBlocking
        // here was a startup ANR risk (input-dispatch timeout on slow disks).
        val updatedContext = base?.let { context ->
            val language = context.getSharedPreferences(
                "linkzary_preferences",
                Context.MODE_PRIVATE
            ).getString("current_language", "en") ?: "en"
            LocaleHelper.setLocale(context, language)
        }
        super.attachBaseContext(updatedContext ?: base)
    }
}