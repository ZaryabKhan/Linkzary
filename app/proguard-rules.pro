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

# Kotlin optimizations
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Compose optimizations
-keep class androidx.compose.** { *; }
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
-keep class okhttp3.** { *; }

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

# Coil optimizations
-keep class coil.** { *; }
-dontwarn coil.**

# JSoup optimizations
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**