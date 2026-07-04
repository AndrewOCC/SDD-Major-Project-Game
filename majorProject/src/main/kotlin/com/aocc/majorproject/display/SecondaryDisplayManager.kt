package com.aocc.majorproject.display

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.WindowManager
import com.aocc.framework.Image
import com.aocc.framework.Screen
import com.aocc.majorproject.Assets
import com.aocc.majorproject.CrashReporter
import com.aocc.majorproject.GamePreferences
import com.aocc.majorproject.GameScreen
import com.aocc.majorproject.MainMenuScreen
import com.aocc.majorproject.MajorProjectGame
import com.aocc.majorproject.TutorialScreen

/** Detects secondary displays and mirrors menu / pause content when enabled. */
class SecondaryDisplayManager(private val activity: MajorProjectGame) :
    DisplayManager.DisplayListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val displayManager: DisplayManager? =
        activity.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager?

    private var presentation: SecondaryDisplayPresentation? = null
    private var currentMode = SecondaryDisplayMode.OFF
    private var pendingShow: PendingShow? = null

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

    fun updateForScreen(screen: Screen) {
        when (screen) {
            is MainMenuScreen -> show(SecondaryDisplayMode.MAIN_MENU, Assets.menu_bg, null)
            is TutorialScreen -> show(SecondaryDisplayMode.MAIN_MENU, Assets.tutorial, null)
            is GameScreen -> updateForGameState(screen.state)
            else -> show(SecondaryDisplayMode.OFF, null, null)
        }
    }

    fun updateForGameState(state: GameScreen.GameState) {
        when (state) {
            GameScreen.GameState.Ready, GameScreen.GameState.Paused ->
                show(SecondaryDisplayMode.PAUSE_MENU, Assets.game_bg, "Paused")
            GameScreen.GameState.GameOver ->
                show(SecondaryDisplayMode.PAUSE_MENU, Assets.game_bg, "Game Over")
            GameScreen.GameState.Running ->
                show(SecondaryDisplayMode.BACKGROUND, Assets.menu_bg, null)
        }
    }

    private fun show(mode: SecondaryDisplayMode, background: Image?, overlayLabel: String?) {
        currentMode = mode
        if (!GamePreferences.secondScreenEnabled || mode == SecondaryDisplayMode.OFF) {
            pendingShow = null
            dismissPresentationAsync()
            return
        }

        val secondary = SecondaryDisplayFinder.find(activity, displayManager)
        if (secondary == null) {
            pendingShow = PendingShow(mode, background, overlayLabel)
            dismissPresentationAsync()
            return
        }

        pendingShow = PendingShow(mode, background, overlayLabel)
        mainHandler.post { presentOnDisplay(secondary, pendingShow!!) }
    }

    private fun presentOnDisplay(secondary: Display, request: PendingShow) {
        if (!GamePreferences.secondScreenEnabled) {
            return
        }

        try {
            if (presentation == null || presentation!!.display == null
                || presentation!!.display!!.displayId != secondary.displayId
            ) {
                dismissPresentationSync()
                presentation = SecondaryDisplayPresentation(activity, secondary)
                presentation!!.show()
            }
            presentation!!.setMode(request.mode, request.background, request.overlayLabel)
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
        if (presentation != null) {
            try {
                presentation!!.dismiss()
            } catch (e: RuntimeException) {
                CrashReporter.log(activity, "Failed to dismiss secondary display", e)
            }
            presentation = null
        }
    }

    override fun onDisplayAdded(displayId: Int) {
        if (GamePreferences.secondScreenEnabled) {
            activity.runOnUiThread {
                if (pendingShow != null) {
                    show(pendingShow!!.mode, pendingShow!!.background, pendingShow!!.overlayLabel)
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
        if (presentation == null || presentation!!.display == null
            || presentation!!.display!!.displayId != displayId
        ) {
            activity.runOnUiThread { updateForScreen(activity.currentScreen) }
        }
    }

    private class PendingShow(
        val mode: SecondaryDisplayMode,
        val background: Image?,
        val overlayLabel: String?,
    )
}
