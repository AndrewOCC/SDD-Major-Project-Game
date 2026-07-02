package com.aocc.majorproject

import com.aocc.framework.Graphics
import com.aocc.framework.Graphics.ImageFormat
import com.aocc.framework.Screen

class LoadingSplashScreen(private val majorProjectGame: MajorProjectGame) : Screen(majorProjectGame) {

    override fun update(deltaTime: Float) {
        Assets.splash = game.graphics.newImage("splash.png", ImageFormat.RGB565)
        game.setScreen(LoadingScreen(majorProjectGame))
    }

    override fun paint(deltaTime: Float) {
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {
    }

    override fun backButton() {
    }
}
