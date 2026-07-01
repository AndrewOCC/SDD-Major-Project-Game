package com.aocc.majorproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Writes crash reports to app storage so they can be retrieved without ADB.
 * Log file: Android/data/com.aocc.majorproject/files/crash_log.txt
 */
public final class CrashReporter {

    private static final String TAG = "CrashReporter";
    private static final String LOG_FILE = "crash_log.txt";
    private static final String PREVIOUS_CRASH_FILE = "previous_crash.txt";

    private CrashReporter() {
    }

    public static void install(Context context) {
        Thread.UncaughtExceptionHandler previous =
                Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            writeCrash(context.getApplicationContext(), thread.getName(), throwable);
            if (previous != null) {
                previous.uncaughtException(thread, throwable);
            } else {
                System.exit(1);
            }
        });
    }

    public static void log(Context context, String message, Throwable throwable) {
        writeEntry(context.getApplicationContext(), "WARN", message, throwable);
    }

    public static void showPreviousCrashIfPresent(Activity activity) {
        File previous = new File(activity.getFilesDir(), PREVIOUS_CRASH_FILE);
        if (!previous.exists()) {
            return;
        }

        String contents = readFile(previous);
        previous.delete();

        new AlertDialog.Builder(activity)
                .setTitle("Previous crash report")
                .setMessage(contents + "\n\nFull log: " + getLogPath(activity))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public static String getLogPath(Context context) {
        return new File(context.getFilesDir(), LOG_FILE).getAbsolutePath();
    }

    private static void writeCrash(Context context, String threadName, Throwable throwable) {
        writeEntry(context, "CRASH", "Uncaught exception on thread " + threadName, throwable);

        File logFile = new File(context.getFilesDir(), LOG_FILE);
        File previousCrash = new File(context.getFilesDir(), PREVIOUS_CRASH_FILE);
        if (logFile.exists()) {
            logFile.renameTo(previousCrash);
        }
        writeEntry(context, "CRASH", "Uncaught exception on thread " + threadName, throwable);
    }

    private static void writeEntry(Context context, String level, String message, Throwable throwable) {
        StringWriter stackTrace = new StringWriter();
        if (throwable != null) {
            throwable.printStackTrace(new PrintWriter(stackTrace));
        }

        String entry = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date())
                + " [" + level + "] " + message + "\n"
                + "Device: " + Build.MANUFACTURER + " " + Build.MODEL
                + ", Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\n"
                + stackTrace
                + "\n---\n";

        Log.e(TAG, entry);

        File logFile = new File(context.getFilesDir(), LOG_FILE);
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(entry);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write crash log", e);
        }
    }

    private static String readFile(File file) {
        try {
            java.util.Scanner scanner = new java.util.Scanner(file);
            scanner.useDelimiter("\\A");
            String contents = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
            return contents.length() > 3000 ? contents.substring(0, 3000) + "..." : contents;
        } catch (IOException e) {
            return "Could not read crash report.";
        }
    }
}
