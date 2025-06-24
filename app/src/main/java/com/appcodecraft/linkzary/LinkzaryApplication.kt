package com.appcodecraft.linkzary

import android.app.Application
import android.content.Context
import android.os.StrictMode
import com.appcodecraft.linkzary.data.preferences.UserPreferencesManager
import com.appcodecraft.linkzary.utils.LocaleHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class LinkzaryApplication : Application() {
    
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
        
        // Optimize memory usage
        System.setProperty("kotlinx.coroutines.scheduler.core.pool.size", "2")
        System.setProperty("kotlinx.coroutines.scheduler.max.pool.size", "4")
        
        // Apply saved language on app start
        val userPreferencesManager = UserPreferencesManager(this)
        val savedLanguage = runBlocking {
            userPreferencesManager.currentLanguage.first()
        }
        LocaleHelper.setLocale(this, savedLanguage)
    }
    
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        
        // Apply locale when the application context is created
        base?.let { context ->
            val userPreferencesManager = UserPreferencesManager(context)
            val savedLanguage = runBlocking {
                userPreferencesManager.currentLanguage.first()
            }
            LocaleHelper.setLocale(context, savedLanguage)
        }
    }
}