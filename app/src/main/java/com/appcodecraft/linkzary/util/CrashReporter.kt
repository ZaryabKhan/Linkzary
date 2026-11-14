package com.appcodecraft.linkzary.util

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Writes uncaught exceptions to a small rotating log in the app's own
 * external files directory, so device-test crashes can be diagnosed without
 * adb. Chained to the platform handler, so normal crash behavior is kept.
 *
 * Files: <external-files-dir>/crash/crash-N.txt (newest = highest N, max 3).
 */
object CrashReporter {

    private const val MAX_FILES = 3

    fun install(appFilesDir: File?) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                appFilesDir?.let { writeCrash(it, thread, throwable) }
            } catch (_: Throwable) {
                // Never let the reporter itself crash.
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    private fun writeCrash(dir: File, thread: Thread, throwable: Throwable) {
        val crashDir = File(dir, "crash").apply { mkdirs() }
        val next = (crashDir.listFiles()
            ?.mapNotNull { it.name.removePrefix("crash-").removeSuffix(".txt").toIntOrNull() }
            ?.maxOrNull() ?: 0) + 1
        val stack = StringWriter().also { throwable.printStackTrace(PrintWriter(it)) }.toString()
        File(crashDir, "crash-$next.txt").writeText(
            buildString {
                appendLine("time=${System.currentTimeMillis()}")
                appendLine("thread=${thread.name}")
                appendLine(stack)
            }
        )
        // Keep only the newest MAX_FILES reports.
        crashDir.listFiles()
            ?.sortedByDescending { it.name }
            ?.drop(MAX_FILES)
            ?.forEach { it.delete() }
    }
}
