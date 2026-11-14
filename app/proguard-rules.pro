# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Performance optimizations
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Keep line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin: kotlin/ and kotlinx.coroutines ship their own consumer R8 rules,
# so blanket keeps here are unnecessary and disable most shrinking/obfuscation.
-dontwarn kotlinx.coroutines.**

# Compose ships consumer R8 rules; no keep needed.
-dontwarn androidx.compose.**

# Room optimizations
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt optimizations
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-dontwarn dagger.hilt.**

# OkHttp optimizations
-dontwarn okhttp3.**
-dontwarn okio.**

# Serialization optimizations
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.appcodecraft.linkzary.**$$serializer { *; }
-keepclassmembers class com.appcodecraft.linkzary.** {
    *** Companion;
}
-keepclasseswithmembers class com.appcodecraft.linkzary.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Coil ships consumer R8 rules; no keep needed.
-dontwarn coil.**

# JSoup ships consumer R8 rules; no keep needed.
-dontwarn org.jsoup.**

# RssParser optimizations
-keep class com.prof18.rssparser.** { *; }
-dontwarn com.prof18.rssparser.**

# Keep data classes used in Room entities
-keep class com.appcodecraft.linkzary.data.entity.** { *; }
-keep class com.appcodecraft.linkzary.data.model.** { *; }

# Keep WorkManager workers
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.appcodecraft.linkzary.worker.** { *; }

# Keep BillingManager
-keep class com.appcodecraft.linkzary.billing.** { *; }
-dontwarn com.android.vending.**

# Drive backup (google-api-client / google-http-client reference optional
# JDK/extension classes that are never used on Android)
-dontwarn com.google.api.client.**
-dontwarn com.google.api.services.**
-dontwarn com.google.common.**
-dontwarn org.apache.http.**