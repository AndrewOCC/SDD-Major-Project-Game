package com.aocc.majorproject.ui

import com.aocc.majorproject.Player

/**
 * Java-facing bridge to the Jetpack Compose menu overlay hosted by `AndroidGame`.
 */
interface ComposeOverlayBridge {

    interface MainMenuListener {
        fun onPlay()
        fun onTutorial()
        fun onSignIn()
        fun onLeaderboards()
        fun onAchievements()
        fun isLoggedIn(): Boolean
    }

    interface SettingsListener {
        fun onResumeGame()
        fun onMenu()
        fun onToggleSound()
        fun onToggleMusic()
        fun onFlatTilt()
        fun onTiltedTilt()
        fun onCustomTilt()
        fun isSoundOn(): Boolean
        fun isMusicOn(): Boolean
        fun getTiltMode(): Int
    }

    fun showMainMenu(listener: MainMenuListener)
    fun showSettings(prompt: String, showMenuButton: Boolean, listener: SettingsListener)
    fun hide()
    fun refreshSettings()
}
