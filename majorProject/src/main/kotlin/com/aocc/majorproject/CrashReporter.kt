package com.aocc.majorproject

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Writes crash reports to app storage so they can be retrieved without ADB.
 * Log file: Android/data/com.aocc.majorproject/files/crash_log.txt
 */
object CrashReporter {

    private const val TAG = "CrashReporter"
    private const val LOG_FILE = "crash_log.txt"
    private const val PREVIOUS_CRASH_FILE = "previous_crash.txt"

    @JvmStatic
    fun install(context: Context) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            writeCrash(context.applicationContext, thread.name, throwable)
            if (previous != null) {
                previous.uncaughtException(thread, throwable)
            } else {
                System.exit(1)
            }
        }
    }

    @JvmStatic
    fun log(context: Context, message: String, throwable: Throwable?) {
        writeEntry(context.applicationContext, "WARN", message, throwable)
    }

    @JvmStatic
    fun showPreviousCrashIfPresent(activity: Activity) {
        val previous = File(activity.filesDir, PREVIOUS_CRASH_FILE)
        if (!previous.exists()) {
            return
        }

        val contents = readFile(previous)
        previous.delete()

        AlertDialog.Builder(activity)
            .setTitle("Previous crash report")
            .setMessage("$contents\n\nFull log: ${getLogPath(activity)}")
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    @JvmStatic
    fun getLogPath(context: Context): String {
        return File(context.filesDir, LOG_FILE).absolutePath
    }

    private fun writeCrash(context: Context, threadName: String, throwable: Throwable) {
        writeEntry(context, "CRASH", "Uncaught exception on thread $threadName", throwable)

        val logFile = File(context.filesDir, LOG_FILE)
        val previousCrash = File(context.filesDir, PREVIOUS_CRASH_FILE)
        if (logFile.exists()) {
            logFile.renameTo(previousCrash)
        }
        writeEntry(context, "CRASH", "Uncaught exception on thread $threadName", throwable)
    }

    private fun writeEntry(context: Context, level: String, message: String, throwable: Throwable?) {
        val stackTrace = StringWriter()
        throwable?.printStackTrace(PrintWriter(stackTrace))

        val entry = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()) +
            " [$level] $message\n" +
            "Device: ${Build.MANUFACTURER} ${Build.MODEL}" +
            ", Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n" +
            stackTrace +
            "\n---\n"

        Log.e(TAG, entry)

        val logFile = File(context.filesDir, LOG_FILE)
        try {
            FileWriter(logFile, true).use { writer ->
                writer.write(entry)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to write crash log", e)
        }
    }

    private fun readFile(file: File): String {
        return try {
            file.bufferedReader().use { reader ->
                val contents = reader.readText()
                if (contents.length > 3000) contents.substring(0, 3000) + "..." else contents
            }
        } catch (e: IOException) {
            "Could not read crash report."
        }
    }
}
