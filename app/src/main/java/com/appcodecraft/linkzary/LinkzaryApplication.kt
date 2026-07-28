package com.appcodecraft.linkzary

import android.app.Application
import android.content.Context
import android.os.StrictMode
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.appcodecraft.linkzary.data.preferences.UserPreferencesManager
import com.appcodecraft.linkzary.utils.LocaleHelper
import com.appcodecraft.linkzary.worker.LinkHealthWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
        // runBlocking here is acceptable: attachBaseContext runs before any UI
        // is displayed, so there is no risk of ANR visible to the user.
        // TODO: Migrate UserPreferencesManager to DataStore for fully async reads.
        val updatedContext = base?.let { context ->
            val userPreferencesManager = UserPreferencesManager(context)
            val savedLanguage = runBlocking {
                userPreferencesManager.currentLanguage.first()
            }
            LocaleHelper.setLocale(context, savedLanguage)
        }
        super.attachBaseContext(updatedContext ?: base)
    }
}