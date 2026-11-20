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
import com.aocc.majorproject.TutorialScreen
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

        // An afternoon in fast-forward: time of day, the homecoming and the trip back to the dock.
        val evening = GameScreen(activity)
        invoke(evening, "changeState", GameScreen.GameState.Running)
        evening.getPlayer().debugInvincible = true
        for ((name, seconds) in listOf("08-golden" to 70f, "09-sunset" to 112f, "10-dusk" to 150f)) {
            setField(evening, "levelTime", seconds)
            repeat(30) { evening.update(1f / 60f) }
            captureScreen(name, evening)
        }
        setField(evening, "levelTime", 169.9f)
        repeat(60 * 3) { evening.update(1f / 60f) }
        captureScreen("11-countdown", evening)
        repeat((60 * 7.3f).toInt()) { evening.update(1f / 60f) }
        captureScreen("12-door-open", evening)
        repeat(60 * 2) { evening.update(1f / 60f) }
        captureScreen("13-lights-trundle", evening)
        var frames = 0
        while (evening.state == GameScreen.GameState.Running && frames < 60 * 40) {
            evening.update(1f / 60f)
            frames++
        }
        captureScreen("14-home-sweet-home", evening)

        val tutorial = TutorialScreen(activity)
        for (lesson in TutorialScreen.Lesson.entries) {
            tutorial.startLesson(lesson.ordinal)
            repeat(75) { tutorial.update(1f / 60f) }
            captureScreen("07-tutorial-${lesson.ordinal + 1}-${lesson.name.lowercase()}", tutorial)
        }
    }

    @Test
    fun dualScreen() {
        // Approximates the AYN Thor bottom panel (1080x1240 @ xhdpi).
        ShadowDisplayManager.addDisplay("w540dp-h620dp-xhdpi")
        GamePreferences.secondScreenEnabled = true
        try {
            val menu = MainMenuScreen(activity)
            activity.setScreen(menu)
            idle()
            val rear = rearPresentation()
            captureScreen("30-dual-top-menu", menu)
            captureRear("31-rear-menu", rear)
            // Other panel shapes must fill edge to edge (no bars) without clipping controls.
            captureRear("38-rear-menu-tall", rear, 1080, 1600)
            captureRear("39-rear-menu-wide", rear, 1240, 1080)

            val tutorial = TutorialScreen(activity)
            activity.setScreen(tutorial)
            idle()
            captureScreen("36-dual-top-tutorial", tutorial)
            captureRear("37-rear-tutorial", rear)
            tutorial.startLesson(TutorialScreen.Lesson.LINGER.ordinal)
            repeat(30) { tutorial.update(1f / 60f) }
            captureScreen("36b-dual-top-tutorial-linger", tutorial)
            captureRear("37b-rear-tutorial-linger", rear)
            tutorial.startLesson(TutorialScreen.Lesson.DONE.ordinal)
            captureRear("37c-rear-tutorial-done", rear)

            val game = GameScreen(activity)
            activity.setScreen(game)
            idle()
            captureScreen("32-dual-top-ready", game)
            captureRear("33-rear-ready", rear)

            invoke(game, "changeState", GameScreen.GameState.Running)
            repeat(120) { game.update(1f / 60f); idle() }
            captureScreen("20-dual-top-running", game)
            captureRear("21-rear-running", rear)
            setField(game, "levelTime", 169.9f)
            repeat(60 * 2) { game.update(1f / 60f); idle() }
            captureRear("21b-rear-countdown", rear)

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

            invoke(game, "confirmQuit", false)
            invoke(game, "changeState", GameScreen.GameState.GameOver)
            idle()
            captureScreen("34-dual-top-gameover", game)
            captureRear("35-rear-gameover", rear)
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

    private fun captureRear(name: String, rear: SecondaryDisplayPresentation, width: Int = 1080, height: Int = 1240) {
        idle()
        captureView(name, rear.window!!.decorView, width, height)
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

    private fun setField(target: Any, name: String, value: Any) {
        val field = target.javaClass.getDeclaredField(name)
        field.isAccessible = true
        field.set(target, value)
    }

    private fun invoke(target: Any, name: String, vararg args: Any) {
        val method = target.javaClass.declaredMethods.first {
            it.name == name && it.parameterCount == args.size
        }
        method.isAccessible = true
        method.invoke(target, *args)
    }
}
