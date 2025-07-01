package com.appcodecraft.linkzary.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {
    
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = when (languageCode) {
            "pt-br" -> Locale("pt", "BR")
            "zh" -> Locale.SIMPLIFIED_CHINESE
            else -> Locale(languageCode)
        }
        
        Locale.setDefault(locale)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, locale)
        } else {
            updateResourcesLegacy(context, locale)
        }
    }
    
    private fun updateResources(context: Context, locale: Locale): Context {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }
    
    @Suppress("DEPRECATION")
    private fun updateResourcesLegacy(context: Context, locale: Locale): Context {
        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale)
        }
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }
    
    fun restartActivity(activity: Activity) {
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "fr" -> "Français"
            "pt-br" -> "Português (Brasil)"
            "es" -> "Español"
            "de" -> "Deutsch"
            "it" -> "Italiano"
            "ar" -> "العربية"
            else -> "English"
        }
    }
}