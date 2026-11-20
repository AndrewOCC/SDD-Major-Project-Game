package com.aocc.majorproject.display

import android.app.Presentation
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Display
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import com.aocc.framework.GameConstants
import com.aocc.majorproject.BuildConfig
import com.aocc.majorproject.MajorProjectGame

/**
 * The rear display in dual-screen mode: every control the game offers, drawn by
 * [SecondaryControlsView], plus (during gameplay, in debug builds) a parameters popup built
 * from standard Android controls. This window takes its own touch input, independently of
 * the primary SurfaceView.
 */
class SecondaryDisplayPresentation(
    context: Context,
    display: Display,
    private val activity: MajorProjectGame,
) : Presentation(context, display) {

    // Running, debug builds: parameters popup toggled from a bottom-right corner button.
    private var debugToggleButton: Button? = null
    private var debugPanel: LinearLayout? = null
    private var debugGodModeSwitch: Switch? = null
    private var debugSpeedSeekBar: SeekBar? = null
    private var debugSpeedLabel: TextView? = null
    private var suppressDebugSpeedListener = false

    private var mode = SecondaryDisplayMode.OFF

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(context)
        root.setBackgroundColor(Color.BLACK)

        root.addView(
            SecondaryControlsView(context, activity),
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

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
        applyMode()
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

    fun setMode(mode: SecondaryDisplayMode) {
        this.mode = mode
        applyMode()
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

    private fun applyMode() {
        // The debug popup is a gameplay tool: only offered while a run is in progress.
        val showDebugControls = BuildConfig.DEBUG && mode == SecondaryDisplayMode.BACKGROUND
        debugToggleButton?.visibility = if (showDebugControls) View.VISIBLE else View.GONE
        if (!showDebugControls) {
            debugPanel?.visibility = View.GONE
        }
    }
}
