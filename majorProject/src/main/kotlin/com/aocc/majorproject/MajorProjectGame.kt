package com.aocc.majorproject

import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.aocc.framework.Screen
import com.aocc.framework.implementation.AndroidGame
import com.aocc.majorproject.display.SecondaryDisplayManager

class MajorProjectGame : AndroidGame() {

    private var playGamesHelper: PlayGamesHelper? = null
    lateinit var secondaryDisplayManager: SecondaryDisplayManager
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        CrashReporter.install(this)
        GamePreferences.load(this)
        super.onCreate(savedInstanceState)
        CrashReporter.showPreviousCrashIfPresent(this)

        secondaryDisplayManager = SecondaryDisplayManager(this)

        try {
            playGamesHelper = PlayGamesHelper(this)
        } catch (e: Exception) {
            CrashReporter.log(this, "Play Games helper failed to initialize", e)
            playGamesHelper = null
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    currentScreen.backButton()
                }
            }
        )
    }

    override val initScreen: Screen
        get() = LoadingScreen(this)

    override fun setScreen(screen: Screen) {
        super.setScreen(screen)
        if (::secondaryDisplayManager.isInitialized) {
            secondaryDisplayManager.updateForScreen(screen)
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && getGamepadInput().onKeyDown(event)) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onDestroy() {
        if (::secondaryDisplayManager.isInitialized) {
            secondaryDisplayManager.shutdown()
        }
        super.onDestroy()
    }

    override fun onResume() {
        // super.onResume() already resumes the current screen and render view.
        super.onResume()
        screenRotation = screenRotationValue
        if (::secondaryDisplayManager.isInitialized) {
            secondaryDisplayManager.refresh()
        }
        playGamesHelper?.refreshSignInState()
    }

    private val screenRotationValue: Int
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = display
                return display?.rotation ?: Display.DEFAULT_DISPLAY
            }
            @Suppress("DEPRECATION")
            return windowManager.defaultDisplay.rotation
        }

    override fun onPause() {
        // super.onPause() already pauses the current screen and render view.
        super.onPause()
        Assets.pauseMusic()
    }

    fun onShowLeaderboardsRequested(id: String) {
        val helper = playGamesHelper
        if (helper != null) {
            helper.showLeaderboards(id)
        } else {
            showPlayGamesUnavailable()
        }
    }

    fun onShowAchievementsRequested(id: String) {
        val helper = playGamesHelper
        if (helper != null) {
            helper.showAchievements()
        } else {
            showPlayGamesUnavailable()
        }
    }

    fun isLoggedIn(): Boolean {
        return playGamesHelper?.isSignedIn() == true
    }

    fun onSignInButtonClicked() {
        val helper = playGamesHelper
        if (helper == null) {
            showPlayGamesUnavailable()
            return
        }
        if (!helper.isSignedIn()) {
            helper.signIn()
        }
    }

    fun onEnteredScore(score: Int) {
        playGamesHelper?.submitScore(getString(R.string.leaderboard_pacifist_mode), score.toLong())
    }

    fun onAchievementUnlocked(id: String) {
        val helper = playGamesHelper
        if (helper != null && helper.isSignedIn()) {
            helper.unlockAchievement(id)
        } else {
            runOnUiThread {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.ccccombo_unlocked),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val isMusicActive: Boolean
        get() = audioManager?.isMusicActive == true

    private fun showPlayGamesUnavailable() {
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                getString(R.string.play_games_unavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun showSecondDisplayUnavailable() {
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                getString(R.string.second_display_unavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun updateSecondaryDisplayForGameState(state: GameScreen.GameState) {
        if (::secondaryDisplayManager.isInitialized) {
            secondaryDisplayManager.updateForGameState(state)
        }
    }

    companion object {
        @JvmField
        var screenRotation: Int = 0
    }
}
