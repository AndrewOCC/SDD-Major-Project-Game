package com.aocc.majorproject.display

import android.app.Presentation
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.Display
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import com.aocc.framework.GameConstants
import com.aocc.framework.Image
import com.aocc.framework.implementation.AndroidImage
import com.aocc.majorproject.Assets
import com.aocc.majorproject.BuildConfig
import com.aocc.majorproject.MajorProjectGame

/**
 * Full-screen content on a secondary (rear) display: a background mirror plus, depending on
 * mode, a live score/combo strip, the game's own pause menu ([SecondaryPauseView]), or (during
 * gameplay, in debug builds) a parameters popup built from standard Android controls. This
 * window takes its own touch input, independently of the primary SurfaceView.
 */
class SecondaryDisplayPresentation(
    context: Context,
    display: Display,
    private val activity: MajorProjectGame,
) : Presentation(context, display) {

    private var imageView: ImageView? = null

    // Ready / Game Over placeholder label.
    private var overlayText: TextView? = null

    // Running: live score / combo strip.
    private var statsText: TextView? = null

    // Paused: the game's pause menu, drawn with the game renderer.
    private var pauseView: SecondaryPauseView? = null

    // Running, debug builds: parameters popup toggled from a bottom-right corner button.
    private var debugToggleButton: Button? = null
    private var debugPanel: LinearLayout? = null
    private var debugGodModeSwitch: Switch? = null
    private var debugSpeedSeekBar: SeekBar? = null
    private var debugSpeedLabel: TextView? = null
    private var suppressDebugSpeedListener = false

    private var pendingContent: PendingContent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(context)
        root.setBackgroundColor(Color.BLACK)

        imageView = ImageView(context).also { view ->
            view.scaleType = ImageView.ScaleType.CENTER_CROP
            root.addView(
                view,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        overlayText = TextView(context).also { textView ->
            textView.setTextColor(Color.WHITE)
            textView.textSize = 32f
            textView.gravity = Gravity.CENTER
            textView.setBackgroundColor(Color.argb(160, 0, 0, 0))
            textView.visibility = View.GONE
            root.addView(
                textView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        statsText = TextView(context).also { textView ->
            textView.setTextColor(Color.WHITE)
            textView.textSize = 26f
            textView.gravity = Gravity.CENTER
            textView.setBackgroundColor(Color.argb(140, 0, 0, 0))
            textView.setPadding(16, 12, 16, 12)
            textView.typeface = Assets.plain
            textView.visibility = View.GONE
            root.addView(
                textView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.TOP
                )
            )
        }

        pauseView = SecondaryPauseView(context, activity).also { view ->
            view.visibility = View.GONE
            root.addView(
                view,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        if (BuildConfig.DEBUG) {
            root.addView(buildDebugToggleButton(), FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.END
            ).apply { rightMargin = 24; bottomMargin = 24 })
            root.addView(buildDebugPanel(), FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.END
            ).apply { rightMargin = 24; bottomMargin = 96 })
        }

        setContentView(root)
        applyPendingContent()
    }

    private fun buildDebugToggleButton(): Button {
        val button = Button(context).apply {
            text = "⚙"
            setOnClickListener {
                val panel = debugPanel ?: return@setOnClickListener
                panel.visibility = if (panel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
        }
        debugToggleButton = button
        return button
    }

    private fun buildDebugPanel(): LinearLayout {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.argb(220, 20, 20, 20))
            setPadding(24, 24, 24, 24)
            visibility = View.GONE
        }
        layout.addView(TextView(context).apply {
            text = "Debug parameters"
            setTextColor(Color.WHITE)
            textSize = 18f
        })
        val godMode = Switch(context).apply {
            text = "God mode"
            setTextColor(Color.WHITE)
            setOnClickListener { activity.applySecondaryDebugAction(SecondaryDebugAction.ToggleGodMode) }
        }
        layout.addView(godMode)
        debugGodModeSwitch = godMode

        val speedRow = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        speedRow.addView(TextView(context).apply {
            text = "Speed"
            setTextColor(Color.WHITE)
            setPadding(0, 0, 16, 0)
        })
        val speedLabel = TextView(context).apply {
            text = "0"
            setTextColor(Color.WHITE)
            setPadding(16, 0, 0, 0)
        }
        val speedSeekBar = SeekBar(context).apply {
            max = GameConstants.SPEED_RAMP_MAX
            layoutParams = LinearLayout.LayoutParams(320, LinearLayout.LayoutParams.WRAP_CONTENT)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    speedLabel.text = progress.toString()
                    if (fromUser && !suppressDebugSpeedListener) {
                        activity.applySecondaryDebugAction(SecondaryDebugAction.SetSpeed(progress))
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
        }
        speedRow.addView(speedSeekBar)
        speedRow.addView(speedLabel)
        layout.addView(speedRow)
        debugSpeedSeekBar = speedSeekBar
        debugSpeedLabel = speedLabel

        layout.addView(Button(context).apply {
            text = "+1000 Score"
            setOnClickListener { activity.applySecondaryDebugAction(SecondaryDebugAction.AddScore) }
        })

        debugPanel = layout
        return layout
    }

    fun setMode(mode: SecondaryDisplayMode, background: Image?, overlayLabel: String?) {
        pendingContent = PendingContent(mode, background, overlayLabel)
        applyPendingContent()
    }

    /** Live score / combo mirrored to the rear screen during gameplay. */
    fun setStatsLabel(statsLabel: String?) {
        val current = pendingContent ?: return
        pendingContent = PendingContent(current.mode, current.background, statsLabel)
        setStatsLabelInternal(statsLabel)
    }

    fun setDebugState(debugState: SecondaryDebugState?) {
        debugGodModeSwitch?.isChecked = debugState?.godMode == true
        if (debugState != null) {
            suppressDebugSpeedListener = true
            debugSpeedSeekBar?.progress = debugState.speed
            debugSpeedLabel?.text = debugState.speed.toString()
            suppressDebugSpeedListener = false
        }
    }

    private fun applyPendingContent() {
        val imageView = imageView ?: return
        val pending = pendingContent ?: return

        val bitmap = toBitmap(pending.background)
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
            imageView.setBackgroundColor(Color.BLACK)
        } else {
            imageView.setImageDrawable(null)
            imageView.setBackgroundColor(Color.BLACK)
        }

        // Draws only while the game is actually paused, so Ready / Game Over keep their label.
        pauseView?.let { view ->
            view.visibility = if (pending.mode == SecondaryDisplayMode.PAUSE_MENU) View.VISIBLE else View.GONE
            view.invalidate()
        }

        val showDebugControls = BuildConfig.DEBUG && pending.mode == SecondaryDisplayMode.BACKGROUND
        debugToggleButton?.visibility = if (showDebugControls) View.VISIBLE else View.GONE
        if (!showDebugControls) {
            debugPanel?.visibility = View.GONE
        }

        if (pending.mode == SecondaryDisplayMode.BACKGROUND) {
            statsText?.typeface = Assets.plain
            setStatsLabelInternal(pending.overlayLabel)
            setOverlayLabelInternal(null)
        } else {
            setStatsLabelInternal(null)
            setOverlayLabelInternal(pending.overlayLabel)
        }
    }

    private fun setStatsLabelInternal(label: String?) {
        val view = statsText ?: return
        if (!label.isNullOrEmpty()) {
            view.text = label
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    private fun setOverlayLabelInternal(label: String?) {
        val view = overlayText ?: return
        if (!label.isNullOrEmpty()) {
            view.text = label
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    private fun toBitmap(image: Image?): Bitmap? {
        if (image is AndroidImage) {
            return image.bitmap
        }
        return null
    }

    private class PendingContent(
        val mode: SecondaryDisplayMode,
        val background: Image?,
        val overlayLabel: String?,
    )
}
