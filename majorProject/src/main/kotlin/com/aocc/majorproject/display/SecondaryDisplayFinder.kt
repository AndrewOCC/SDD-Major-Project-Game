package com.aocc.majorproject.display

import android.hardware.display.DisplayManager
import android.view.Display
import com.aocc.majorproject.CrashReporter
import com.aocc.majorproject.MajorProjectGame

/**
 * Locates a secondary display suitable for [android.app.Presentation].
 * Handhelds like the AYN Thor expose the rear panel via [CATEGORY_REAR].
 */
internal object SecondaryDisplayFinder {

    /** Hidden framework category for complementary rear-facing displays. */
    const val CATEGORY_REAR = "android.hardware.display.category.REAR"

    /** [Display.FLAG_REAR] — not public SDK but required on dual-screen handhelds. */
    private const val FLAG_REAR = 1 shl 13

    fun find(activity: MajorProjectGame, displayManager: DisplayManager?): Display? {
        if (displayManager == null) {
            return null
        }
        val activityDisplayId = getActivityDisplayId(activity)
        return find(displayManager, activityDisplayId, activity)
    }

    fun find(displayManager: DisplayManager, activityDisplayId: Int): Display? {
        return find(displayManager, activityDisplayId, null)
    }

    private fun find(
        displayManager: DisplayManager,
        activityDisplayId: Int,
        logContext: MajorProjectGame?,
    ): Display? {
        var rear = firstCandidate(displayManager.getDisplays(CATEGORY_REAR), activityDisplayId)
        if (rear != null) {
            return rear
        }

        var presentation = firstCandidate(
            displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION),
            activityDisplayId
        )
        if (presentation != null) {
            return presentation
        }

        for (display in displayManager.displays) {
            if (!isCandidate(display, activityDisplayId)) {
                continue
            }
            val flags = display.flags
            if (flags and FLAG_REAR != 0 || flags and Display.FLAG_PRESENTATION != 0) {
                return display
            }
        }

        val fallback = firstCandidate(displayManager.displays, activityDisplayId)
        if (fallback == null && logContext != null) {
            logDisplays(logContext, displayManager, activityDisplayId)
        }
        return fallback
    }

    private fun firstCandidate(displays: Array<Display>?, activityDisplayId: Int): Display? {
        if (displays == null) {
            return null
        }
        for (display in displays) {
            if (isCandidate(display, activityDisplayId)) {
                return display
            }
        }
        return null
    }

    private fun isCandidate(display: Display?, activityDisplayId: Int): Boolean {
        if (display == null || display.displayId == activityDisplayId) {
            return false
        }
        if (!display.isValid) {
            return false
        }
        val state = display.state
        return state == Display.STATE_ON
            || state == Display.STATE_UNKNOWN
            || state == Display.STATE_OFF
    }

    private fun getActivityDisplayId(activity: MajorProjectGame): Int {
        val activityDisplay = activity.display
        if (activityDisplay != null) {
            return activityDisplay.displayId
        }
        @Suppress("DEPRECATION")
        val defaultDisplay = activity.windowManager.defaultDisplay
        return defaultDisplay?.displayId ?: Display.DEFAULT_DISPLAY
    }

    private fun logDisplays(
        activity: MajorProjectGame,
        displayManager: DisplayManager,
        activityDisplayId: Int,
    ) {
        val message = StringBuilder("No secondary display found. activityDisplayId=")
            .append(activityDisplayId)
            .append(" displays=[")
        for (display in displayManager.displays) {
            message.append("{id=").append(display.displayId)
                .append(", name=").append(display.name)
                .append(", state=").append(display.state)
                .append(", flags=0x").append(Integer.toHexString(display.flags))
                .append("} ")
        }
        message.append(']')
        CrashReporter.log(activity, message.toString(), null)
    }
}
