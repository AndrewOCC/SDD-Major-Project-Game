package com.aocc.majorproject.display;

import android.hardware.display.DisplayManager;
import android.view.Display;

import com.aocc.majorproject.CrashReporter;
import com.aocc.majorproject.MajorProjectGame;

/**
 * Locates a secondary display suitable for {@link android.app.Presentation}.
 * Handhelds like the AYN Thor expose the rear panel via {@link #CATEGORY_REAR}.
 */
final class SecondaryDisplayFinder {

    /** Hidden framework category for complementary rear-facing displays. */
    static final String CATEGORY_REAR = "android.hardware.display.category.REAR";

    /** {@link Display#FLAG_REAR} — not public SDK but required on dual-screen handhelds. */
    private static final int FLAG_REAR = 1 << 13;

    private SecondaryDisplayFinder() {
    }

    static Display find(MajorProjectGame activity, DisplayManager displayManager) {
        if (displayManager == null) {
            return null;
        }
        int activityDisplayId = getActivityDisplayId(activity);
        return find(displayManager, activityDisplayId, activity);
    }

    static Display find(DisplayManager displayManager, int activityDisplayId) {
        return find(displayManager, activityDisplayId, null);
    }

    private static Display find(DisplayManager displayManager, int activityDisplayId,
            MajorProjectGame logContext) {

        Display rear = firstCandidate(displayManager.getDisplays(CATEGORY_REAR), activityDisplayId);
        if (rear != null) {
            return rear;
        }

        Display presentation = firstCandidate(
                displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION),
                activityDisplayId);
        if (presentation != null) {
            return presentation;
        }

        for (Display display : displayManager.getDisplays()) {
            if (!isCandidate(display, activityDisplayId)) {
                continue;
            }
            int flags = display.getFlags();
            if ((flags & FLAG_REAR) != 0 || (flags & Display.FLAG_PRESENTATION) != 0) {
                return display;
            }
        }

        Display fallback = firstCandidate(displayManager.getDisplays(), activityDisplayId);
        if (fallback == null && logContext != null) {
            logDisplays(logContext, displayManager, activityDisplayId);
        }
        return fallback;
    }

    private static Display firstCandidate(Display[] displays, int activityDisplayId) {
        if (displays == null) {
            return null;
        }
        for (Display display : displays) {
            if (isCandidate(display, activityDisplayId)) {
                return display;
            }
        }
        return null;
    }

    private static boolean isCandidate(Display display, int activityDisplayId) {
        if (display == null || display.getDisplayId() == activityDisplayId) {
            return false;
        }
        if (!display.isValid()) {
            return false;
        }
        int state = display.getState();
        return state == Display.STATE_ON
                || state == Display.STATE_UNKNOWN
                || state == Display.STATE_OFF;
    }

    private static int getActivityDisplayId(MajorProjectGame activity) {
        Display activityDisplay = activity.getDisplay();
        if (activityDisplay != null) {
            return activityDisplay.getDisplayId();
        }
        Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
        return defaultDisplay != null ? defaultDisplay.getDisplayId() : Display.DEFAULT_DISPLAY;
    }

    private static void logDisplays(MajorProjectGame activity, DisplayManager displayManager,
            int activityDisplayId) {
        StringBuilder message = new StringBuilder("No secondary display found. activityDisplayId=")
                .append(activityDisplayId)
                .append(" displays=[");
        for (Display display : displayManager.getDisplays()) {
            message.append("{id=").append(display.getDisplayId())
                    .append(", name=").append(display.getName())
                    .append(", state=").append(display.getState())
                    .append(", flags=0x").append(Integer.toHexString(display.getFlags()))
                    .append("} ");
        }
        message.append(']');
        CrashReporter.log(activity, message.toString(), null);
    }
}
