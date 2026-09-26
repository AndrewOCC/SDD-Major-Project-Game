package com.aocc.majorproject.screenshots

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.view.View
import com.aocc.framework.Viewport
import com.aocc.framework.implementation.AndroidGraphics
import com.aocc.majorproject.Assets
import com.aocc.majorproject.GamePreferences
import com.aocc.majorproject.GameScreen
import com.aocc.majorproject.MainMenuScreen
import com.aocc.majorproject.MajorProjectGame
import com.aocc.majorproject.display.SecondaryDisplayPresentation
import com.aocc.framework.Screen
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDisplayManager
import java.io.File
import java.io.FileOutputStream

/**
 * Renders key screens to PNGs under majorProject/build/screenshots so UI changes can be
 * checked without a device. Opt-in: run with -Pscreenshots (skipped otherwise).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1280dp-h720dp-land-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotTest {

    private lateinit var controller: ActivityController<MajorProjectGame>
    private lateinit var activity: MajorProjectGame
    private val outDir = File(System.getProperty("screenshots.dir") ?: "build/screenshots")

    @Before
    fun setUp() {
        assumeTrue("screenshots disabled", System.getProperty("screenshots.enabled") == "true")
        outDir.mkdirs()
        // Not resumed: that would start the render thread, and only this test should draw.
        controller = Robolectric.buildActivity(MajorProjectGame::class.java).create().start()
        activity = controller.get()
        waitForAssets()
    }

    @Test
    fun primaryScreens() {
        GamePreferences.secondScreenEnabled = false
        captureScreen("01-main-menu", MainMenuScreen(activity))

        val ready = GameScreen(activity)
        captureScreen("02-ready", ready)

        val running = GameScreen(activity)
        invoke(running, "changeState", GameScreen.GameState.Running)
        repeat(240) { running.update(1f / 60f) }
        captureScreen("03-running", running)

        val paused = GameScreen(activity)
        invoke(paused, "changeState", GameScreen.GameState.Running)
        repeat(90) { paused.update(1f / 60f) }
        invoke(paused, "openPauseMenu", false)
        captureScreen("04-paused", paused)

        invoke(paused, "startResumeCountdown")
        paused.update(0.5f)
        captureScreen("05-paused-countdown", paused)

        val quit = GameScreen(activity)
        invoke(quit, "changeState", GameScreen.GameState.Running)
        invoke(quit, "openPauseMenu", false)
        invoke(quit, "openQuitConfirm")
        captureScreen("06-quit-confirm", quit)
    }

    @Test
    fun dualScreen() {
        // Approximates the AYN Thor bottom panel (1080x1240 @ xhdpi).
        ShadowDisplayManager.addDisplay("w540dp-h620dp-xhdpi")
        GamePreferences.secondScreenEnabled = true
        try {
            val game = GameScreen(activity)
            activity.setScreen(game)
            invoke(game, "changeState", GameScreen.GameState.Running)
            repeat(120) { game.update(1f / 60f); idle() }
            val rear = rearPresentation()
            captureScreen("20-dual-top-running", game)
            captureRear("21-rear-running", rear)

            val gear = ArrayList<View>()
            rear.window!!.decorView.findViewsWithText(gear, "⚙", View.FIND_VIEWS_WITH_TEXT)
            gear.first().performClick()
            idle()
            captureRear("22-rear-debug-popup", rear)

            invoke(game, "openPauseMenu", false)
            game.update(1f / 60f)
            idle()
            captureScreen("23-dual-top-paused", game)
            captureRear("24-rear-paused", rear)

            invoke(game, "startResumeCountdown")
            game.update(0.5f)
            captureRear("25-rear-paused-countdown", rear)

            invoke(game, "cancelResumeCountdown")
            invoke(game, "openQuitConfirm")
            captureRear("26-rear-quit-confirm", rear)
        } finally {
            GamePreferences.secondScreenEnabled = false
        }
    }

    private fun rearPresentation(): SecondaryDisplayPresentation {
        val field = activity.secondaryDisplayManager.javaClass.getDeclaredField("presentation")
        field.isAccessible = true
        return field.get(activity.secondaryDisplayManager) as SecondaryDisplayPresentation?
            ?: error("rear presentation was not created")
    }

    private fun captureRear(name: String, rear: SecondaryDisplayPresentation) {
        idle()
        captureView(name, rear.window!!.decorView, 1080, 1240)
    }

    private fun captureScreen(name: String, screen: Screen) {
        val graphics = activity.graphics as AndroidGraphics
        val viewport = Viewport().apply { update(1280, 720) }
        check(graphics.beginFrame(viewport)) { "framebuffer not ready" }
        screen.paint(1f / 60f)
        graphics.endFrame()
        save(name, graphics.frameBuffer!!)
    }

    private fun captureView(name: String, root: View, width: Int, height: Int) {
        root.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        root.layout(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        root.draw(Canvas(bitmap))
        save(name, bitmap)
    }

    private fun save(name: String, bitmap: Bitmap) {
        FileOutputStream(File(outDir, "$name.png")).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private fun waitForAssets() {
        val deadline = System.currentTimeMillis() + 60_000
        while (System.currentTimeMillis() < deadline) {
            idle()
            if (Assets.plain != null && Assets.game_bg != null && Assets.menu_bg != null) {
                return
            }
            Thread.sleep(50)
        }
        error("assets did not load")
    }

    private fun idle() {
        shadowOf(Looper.getMainLooper()).idle()
    }

    private fun invoke(target: Any, name: String, vararg args: Any) {
        val method = target.javaClass.declaredMethods.first {
            it.name == name && it.parameterCount == args.size
        }
        method.isAccessible = true
        method.invoke(target, *args)
    }
}
