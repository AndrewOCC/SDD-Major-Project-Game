package com.aocc.majorproject.ui.compose

import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.FragmentActivity
import com.aocc.majorproject.ui.ComposeOverlayBridge

class ComposeOverlayHost(activity: FragmentActivity) : ComposeOverlayBridge {

    private enum class Mode {
        Hidden,
        MainMenu,
        Settings,
    }

    private var mode by mutableStateOf(Mode.Hidden)
    private var settingsPrompt by mutableStateOf("")
    private var showMenuButton by mutableStateOf(false)
    private var soundOn by mutableStateOf(true)
    private var musicOn by mutableStateOf(true)
    private var tiltMode by mutableIntStateOf(2)

    private var mainMenuListener: ComposeOverlayBridge.MainMenuListener? = null
    private var settingsListener: ComposeOverlayBridge.SettingsListener? = null

    val view: ComposeView = ComposeView(activity).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            GameMenuTheme {
                when (mode) {
                    Mode.Hidden -> Unit
                    Mode.MainMenu -> {
                        val listener = mainMenuListener
                        if (listener != null) {
                            MainMenuOverlayContent(
                                loggedIn = listener.isLoggedIn(),
                                onPlay = { listener.onPlay() },
                                onTutorial = { listener.onTutorial() },
                                onSignIn = { listener.onSignIn() },
                                onLeaderboards = { listener.onLeaderboards() },
                                onAchievements = { listener.onAchievements() },
                            )
                        }
                    }
                    Mode.Settings -> {
                        val listener = settingsListener
                        if (listener != null) {
                            SettingsOverlayContent(
                                prompt = settingsPrompt,
                                showMenuButton = showMenuButton,
                                soundOn = soundOn,
                                musicOn = musicOn,
                                tiltMode = tiltMode,
                                onResume = { listener.onResumeGame() },
                                onMenu = { listener.onMenu() },
                                onToggleSound = {
                                    listener.onToggleSound()
                                    refreshFromListener(listener)
                                },
                                onToggleMusic = {
                                    listener.onToggleMusic()
                                    refreshFromListener(listener)
                                },
                                onFlatTilt = {
                                    listener.onFlatTilt()
                                    refreshFromListener(listener)
                                },
                                onTiltedTilt = {
                                    listener.onTiltedTilt()
                                    refreshFromListener(listener)
                                },
                                onCustomTilt = {
                                    listener.onCustomTilt()
                                    refreshFromListener(listener)
                                },
                            )
                        }
                    }
                }
            }
        }
        visibility = View.GONE
        isClickable = true
    }

    override fun showMainMenu(listener: ComposeOverlayBridge.MainMenuListener) {
        mainMenuListener = listener
        settingsListener = null
        mode = Mode.MainMenu
        view.visibility = View.VISIBLE
    }

    override fun showSettings(
        prompt: String,
        showMenuButton: Boolean,
        listener: ComposeOverlayBridge.SettingsListener,
    ) {
        settingsListener = listener
        mainMenuListener = null
        settingsPrompt = prompt
        this.showMenuButton = showMenuButton
        refreshFromListener(listener)
        mode = Mode.Settings
        view.visibility = View.VISIBLE
    }

    override fun hide() {
        mode = Mode.Hidden
        mainMenuListener = null
        settingsListener = null
        view.visibility = View.GONE
    }

    override fun refreshSettings() {
        settingsListener?.let { refreshFromListener(it) }
    }

    private fun refreshFromListener(listener: ComposeOverlayBridge.SettingsListener) {
        soundOn = listener.isSoundOn()
        musicOn = listener.isMusicOn()
        tiltMode = listener.getTiltMode()
    }
}
