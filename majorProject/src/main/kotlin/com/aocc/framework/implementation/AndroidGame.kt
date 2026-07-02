package com.aocc.framework.implementation

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.graphics.Rect
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.aocc.framework.Audio
import com.aocc.framework.FileIO
import com.aocc.framework.Game
import com.aocc.framework.Graphics
import com.aocc.framework.Input
import com.aocc.framework.Screen
import com.aocc.framework.Viewport
import com.aocc.majorproject.input.GamepadInput
import com.aocc.majorproject.ui.ComposeOverlayBridge
import com.aocc.majorproject.ui.compose.ComposeOverlayHost

// Note: this code was heavily modified for the purposes of the major project, including
// changes to the size of windows, SDK versions and handling orientation. Similarly,
// code from the google framework references this instead of 'Fragments' in order to allow
// the MajorProjectGame class to extend both this class and the GPG helper class. This
// is otherwise not possible

abstract class AndroidGame : FragmentActivity(), Game {

    private lateinit var renderView: AndroidFastRenderView
    private lateinit var graphicsImpl: AndroidGraphics
    private lateinit var audioImpl: AndroidAudio
    private lateinit var inputImpl: AndroidInput
    private lateinit var fileIOImpl: AndroidFileIO
    private lateinit var currentScreenImpl: Screen
    @JvmField
    var audioManager: AudioManager? = null
    val viewport = Viewport()
    private val gamepadInput = GamepadInput()
    private lateinit var composeOverlayHost: ComposeOverlayHost

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            viewport.update(maxOf(1, bounds.width()), maxOf(1, bounds.height()))
        } else {
            val size = Point()
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            display.getSize(size)
            viewport.update(maxOf(1, size.x), maxOf(1, size.y))
        }

        graphicsImpl = AndroidGraphics(assets)
        renderView = AndroidFastRenderView(this, graphicsImpl)
        fileIOImpl = AndroidFileIO(this)
        audioImpl = AndroidAudio(this)
        inputImpl = AndroidInput(this, renderView, viewport)

        composeOverlayHost = ComposeOverlayHost(this)

        val root = FrameLayout(this)
        root.addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
            val width = right - left
            val height = bottom - top
            if (width > 0 && height > 0) {
                viewport.update(width, height)
                updateInputViewport(viewport)
            }
        }
        root.addView(
            renderView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        renderView.isFocusable = true
        renderView.isFocusableInTouchMode = true
        renderView.requestFocus()
        root.addView(
            composeOverlayHost.view,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        currentScreenImpl = initScreen
        setContentView(root)
        configureImmersiveWindow()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun configureImmersiveWindow() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (insetsController != null) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onResume() {
        super.onResume()
        configureImmersiveWindow()
        currentScreenImpl.resume()
        renderView.resume()
    }

    override fun onPause() {
        super.onPause()
        renderView.pause()
        currentScreenImpl.pause()

        if (isFinishing) {
            currentScreenImpl.dispose()
        }
    }

    // GETTERS AND SETTERS FOR THE FRAMEWORK

    override val input: Input
        get() = inputImpl

    override val fileIO: FileIO
        get() = fileIOImpl

    override val graphics: Graphics
        get() = graphicsImpl

    override val audio: Audio
        get() = audioImpl

    override fun setScreen(screen: Screen) {
        currentScreenImpl.pause()
        currentScreenImpl.dispose()
        screen.resume()
        screen.update(0f)
        currentScreenImpl = screen
    }

    override val currentScreen: Screen
        get() = currentScreenImpl

    fun updateInputViewport(viewport: Viewport) {
        inputImpl.updateViewport(viewport)
    }

    fun getComposeOverlay(): ComposeOverlayBridge {
        return composeOverlayHost
    }

    fun getGamepadInput(): GamepadInput {
        return gamepadInput
    }
}
