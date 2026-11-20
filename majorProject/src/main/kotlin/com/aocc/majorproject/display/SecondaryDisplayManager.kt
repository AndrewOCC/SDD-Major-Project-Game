package com.aocc.majorproject.display

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.WindowManager
import com.aocc.framework.Screen
import com.aocc.majorproject.CrashReporter
import com.aocc.majorproject.GamePreferences
import com.aocc.majorproject.GameScreen
import com.aocc.majorproject.MainMenuScreen
import com.aocc.majorproject.MajorProjectGame
import com.aocc.majorproject.TutorialScreen

/** Detects a secondary (rear) display and, when enabled, moves every control onto it. */
class SecondaryDisplayManager(private val activity: MajorProjectGame) :
    DisplayManager.DisplayListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val displayManager: DisplayManager? =
        activity.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager?

    private var presentation: SecondaryDisplayPresentation? = null
    private var currentMode = SecondaryDisplayMode.OFF
    private var pendingShow: PendingShow? = null
    private var lastDebugState: SecondaryDebugState? = null

    init {
        displayManager?.registerDisplayListener(this, mainHandler)
    }

    fun shutdown() {
        displayManager?.unregisterDisplayListener(this)
        dismissPresentationSync()
    }

    fun refresh() {
        if (GamePreferences.secondScreenEnabled) {
            updateForScreen(activity.currentScreen)
        }
    }

    fun isSecondaryDisplayAvailable(): Boolean {
        return SecondaryDisplayFinder.find(activity, displayManager) != null
    }

    /** True while the rear display is up and owns every control (dual-screen mode). */
    fun isRearControlsActive(): Boolean {
        return GamePreferences.secondScreenEnabled &&
            currentMode != SecondaryDisplayMode.OFF &&
            presentation != null
    }

    fun updateForScreen(screen: Screen) {
        when (screen) {
            is MainMenuScreen, is TutorialScreen -> show(SecondaryDisplayMode.MAIN_MENU)
            is GameScreen -> updateForGameState(screen.state)
            else -> show(SecondaryDisplayMode.OFF)
        }
    }

    fun updateForGameState(state: GameScreen.GameState) {
        when (state) {
            GameScreen.GameState.Running -> show(SecondaryDisplayMode.BACKGROUND)
            else -> show(SecondaryDisplayMode.PAUSE_MENU)
        }
    }

    /** Mirrors debug-parameter values into the rear screen's parameters popup. */
    fun updateDebugState(debugState: SecondaryDebugState) {
        if (!GamePreferences.secondScreenEnabled || currentMode != SecondaryDisplayMode.BACKGROUND) {
            return
        }
        if (debugState == lastDebugState) {
            return
        }
        lastDebugState = debugState
        mainHandler.post {
            if (currentMode == SecondaryDisplayMode.BACKGROUND) {
                presentation?.setDebugState(debugState)
            }
        }
    }

    private fun show(mode: SecondaryDisplayMode) {
        if (mode != currentMode) {
            lastDebugState = null
        }
        currentMode = mode
        if (!GamePreferences.secondScreenEnabled || mode == SecondaryDisplayMode.OFF) {
            pendingShow = null
            dismissPresentationAsync()
            return
        }

        val secondary = SecondaryDisplayFinder.find(activity, displayManager)
        if (secondary == null) {
            pendingShow = PendingShow(mode)
            dismissPresentationAsync()
            return
        }

        val request = PendingShow(mode)
        pendingShow = request
        mainHandler.post { presentOnDisplay(secondary, request) }
    }

    private fun presentOnDisplay(secondary: Display, request: PendingShow) {
        if (!GamePreferences.secondScreenEnabled) {
            return
        }
        // A newer request may have superseded this one before the post ran.
        if (pendingShow !== request) {
            return
        }

        try {
            var current = presentation
            if (current == null || current.display?.displayId != secondary.displayId) {
                dismissPresentationSync()
                current = SecondaryDisplayPresentation(activity, secondary, activity)
                presentation = current
                current.show()
            }
            current.setMode(request.mode)
            pendingShow = null
        } catch (e: WindowManager.InvalidDisplayException) {
            CrashReporter.log(activity, "Secondary display rejected Presentation.show()", e)
            dismissPresentationSync()
        } catch (e: RuntimeException) {
            CrashReporter.log(activity, "Failed to show secondary display", e)
            dismissPresentationSync()
        }
    }

    private fun dismissPresentationAsync() {
        mainHandler.post { dismissPresentationSync() }
    }

    private fun dismissPresentationSync() {
        val current = presentation ?: return
        try {
            current.dismiss()
        } catch (e: RuntimeException) {
            CrashReporter.log(activity, "Failed to dismiss secondary display", e)
        }
        presentation = null
    }

    override fun onDisplayAdded(displayId: Int) {
        if (GamePreferences.secondScreenEnabled) {
            activity.runOnUiThread {
                val pending = pendingShow
                if (pending != null) {
                    show(pending.mode)
                } else {
                    updateForScreen(activity.currentScreen)
                }
            }
        }
    }

    override fun onDisplayRemoved(displayId: Int) {
        if (presentation?.display?.displayId == displayId) {
            dismissPresentationAsync()
        }
    }

    override fun onDisplayChanged(displayId: Int) {
        if (!GamePreferences.secondScreenEnabled) {
            return
        }
        if (presentation?.display?.displayId != displayId) {
            activity.runOnUiThread { updateForScreen(activity.currentScreen) }
        }
    }

    private class PendingShow(val mode: SecondaryDisplayMode)
}
