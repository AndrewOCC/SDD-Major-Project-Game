package com.aocc.majorproject

import android.graphics.Color
import android.graphics.Paint
import com.aocc.framework.GameConstants
import com.aocc.framework.Graphics
import com.aocc.framework.Screen
import com.aocc.majorproject.ui.UiText

class LoadingScreen(private val majorProjectGame: MajorProjectGame) : Screen(majorProjectGame) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val assetLoader: AssetLoader

    @Volatile
    private var progress = 0f

    @Volatile
    private var transitionPending = false

    @Volatile
    private var loadFailed = false

    init {
        assetLoader = AssetLoader(majorProjectGame, object : AssetLoader.Listener {
            override fun onProgress(loaded: Int, total: Int) {
                progress = loaded / total.toFloat()
            }

            override fun onComplete() {
                transitionPending = true
            }

            override fun onError(error: Exception) {
                loadFailed = true
                CrashReporter.log(majorProjectGame, "Asset loading failed", error)
            }
        })
        assetLoader.start()
    }

    override fun update(deltaTime: Float) {
        if (transitionPending) {
            game.setScreen(MainMenuScreen(majorProjectGame))
        }
    }

    override fun paint(deltaTime: Float) {
        val g = game.graphics

        val splash = Assets.splash
        if (splash != null) {
            g.drawImage(splash, 0, 0)
        } else {
            g.drawRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, Color.BLACK)
        }

        g.drawRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, Color.argb(120, 255, 255, 255))
        val fillWidth = maxOf(1, (BAR_WIDTH * progress).toInt())
        g.drawRect(BAR_X, BAR_Y, fillWidth, BAR_HEIGHT, Color.argb(220, 80, 180, 255))

        if (Assets.plain != null) {
            paint.typeface = Assets.plain
        }
        paint.textSize = 28f
        val label = if (loadFailed) "Load failed — retrying may help" else "Loading…"
        UiText.drawCentered(g, paint, label, GameConstants.WORLD_WIDTH / 2, BAR_Y - 24, Color.WHITE)
        UiText.drawCentered(
            g, paint, "${(progress * 100).toInt()}%",
            GameConstants.WORLD_WIDTH / 2, BAR_Y + BAR_HEIGHT / 2, Color.WHITE
        )

        VersionOverlay.paint(g)
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {
        assetLoader.shutdown()
    }

    override fun backButton() {
    }

    companion object {
        private const val BAR_WIDTH = 480
        private const val BAR_HEIGHT = 24
        private val BAR_X: Int = (GameConstants.WORLD_WIDTH - BAR_WIDTH) / 2
        private const val BAR_Y = 620
    }
}
