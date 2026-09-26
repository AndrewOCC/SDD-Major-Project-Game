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
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.ToggleButton
import com.aocc.framework.GameConstants
import com.aocc.framework.Image
import com.aocc.framework.implementation.AndroidImage
import com.aocc.majorproject.Assets
import com.aocc.majorproject.BuildConfig
import com.aocc.majorproject.MajorProjectGame
import com.aocc.majorproject.ui.PauseMenuPanel
import kotlin.math.ceil

/**
 * Full-screen content on a secondary (rear) display: a background mirror plus, depending on
 * mode, a live score/combo strip, a fully interactive pause menu, or (during gameplay, in
 * debug builds) a parameters popup — all built from standard Android views so this window can
 * receive its own touch input independently of the primary SurfaceView.
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

    // Paused: interactive controls mirroring the primary pause menu.
    private var pauseControls: LinearLayout? = null
    private var soundToggle: ToggleButton? = null
    private var musicToggle: ToggleButton? = null
    private var secondScreenToggle: ToggleButton? = null
    private var flatRadio: RadioButton? = null
    private var tiltedRadio: RadioButton? = null
    private var customRadio: RadioButton? = null
    private var resumeButton: Button? = null
    private var quitButton: Button? = null
    private var suppressTiltListener = false

    private var quitConfirmLayout: LinearLayout? = null

    // Running, debug builds: parameters popup toggled from a bottom-right corner button.
    private var debugToggleButton: Button? = null
    private var debugPanel: LinearLayout? = null
    private var debugGodModeSwitch: Switch? = null
    private var debugSpeedSeekBar: SeekBar? = null
    private var debugSpeedLabel: TextView? = null
    private var suppressDebugSpeedListener = false

    private var pendingContent: PendingContent? = null
    private var pendingPauseState: SecondaryPauseState? = null

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

        root.addView(buildPauseControls(), FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER
        ))
        root.addView(buildQuitConfirmLayout(), FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER
        ))

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

    private fun buildPauseControls(): LinearLayout {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.argb(210, 0, 0, 0))
            setPadding(32, 32, 32, 32)
            visibility = View.GONE
        }

        val toggleRow = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        val sound = ToggleButton(context).apply {
            textOn = "Sound: On"
            textOff = "Sound: Off"
            setOnClickListener { activity.activateSecondaryPauseItem(PauseMenuPanel.Item.SOUND) }
        }
        val music = ToggleButton(context).apply {
            textOn = "Music: On"
            textOff = "Music: Off"
            setOnClickListener { activity.activateSecondaryPauseItem(PauseMenuPanel.Item.MUSIC) }
        }
        val secondScreen = ToggleButton(context).apply {
            textOn = "2nd Screen: On"
            textOff = "2nd Screen: Off"
            setOnClickListener { activity.activateSecondaryPauseItem(PauseMenuPanel.Item.SECOND_SCREEN) }
        }
        toggleRow.addView(sound)
        toggleRow.addView(music)
        toggleRow.addView(secondScreen)
        layout.addView(toggleRow)
        soundToggle = sound
        musicToggle = music
        secondScreenToggle = secondScreen

        val tiltGroup = RadioGroup(context).apply { orientation = RadioGroup.HORIZONTAL }
        // RadioGroup tracks the checked child by id, so each button needs a real (non-default) one.
        val flat = RadioButton(context).apply { text = "Flat"; id = View.generateViewId() }
        val tilted = RadioButton(context).apply { text = "Tilted"; id = View.generateViewId() }
        val custom = RadioButton(context).apply { text = "Custom"; id = View.generateViewId() }
        tiltGroup.addView(flat)
        tiltGroup.addView(tilted)
        tiltGroup.addView(custom)
        tiltGroup.setOnCheckedChangeListener { _, checkedId ->
            if (suppressTiltListener) {
                return@setOnCheckedChangeListener
            }
            val item = when (checkedId) {
                flat.id -> PauseMenuPanel.Item.TILT_FLAT
                tilted.id -> PauseMenuPanel.Item.TILT_TILTED
                else -> PauseMenuPanel.Item.TILT_CUSTOM
            }
            activity.activateSecondaryPauseItem(item)
        }
        layout.addView(tiltGroup)
        flatRadio = flat
        tiltedRadio = tilted
        customRadio = custom

        val buttonRow = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        val resume = Button(context).apply {
            text = "Resume"
            setOnClickListener { activity.activateSecondaryPauseItem(PauseMenuPanel.Item.RESUME) }
        }
        val quit = Button(context).apply {
            text = "Quit"
            setOnClickListener { activity.activateSecondaryPauseItem(PauseMenuPanel.Item.QUIT) }
        }
        buttonRow.addView(resume)
        buttonRow.addView(quit)
        layout.addView(buttonRow)
        resumeButton = resume
        quitButton = quit

        pauseControls = layout
        return layout
    }

    private fun buildQuitConfirmLayout(): LinearLayout {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.argb(230, 0, 0, 0))
            setPadding(32, 32, 32, 32)
            visibility = View.GONE
        }
        layout.addView(TextView(context).apply {
            text = "Quit and return to the menu?"
            setTextColor(Color.WHITE)
            textSize = 22f
            gravity = Gravity.CENTER
        })
        val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        row.addView(Button(context).apply {
            text = "Yes"
            setOnClickListener { activity.confirmSecondaryQuit(true) }
        })
        row.addView(Button(context).apply {
            text = "No"
            setOnClickListener { activity.confirmSecondaryQuit(false) }
        })
        layout.addView(row)
        quitConfirmLayout = layout
        return layout
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
        pendingPauseState = null
        applyPendingContent()
        applyPauseState()
    }

    /** Live score / combo mirrored to the rear screen during gameplay. */
    fun setStatsLabel(statsLabel: String?) {
        val current = pendingContent ?: return
        pendingContent = PendingContent(current.mode, current.background, statsLabel)
        setStatsLabelInternal(statsLabel)
    }

    /** Mirrors the primary pause menu's live state onto the rear screen's native controls. */
    fun setPauseState(pauseState: SecondaryPauseState?) {
        pendingPauseState = pauseState
        applyPauseState()
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

    private fun applyPauseState() {
        val controls = pauseControls ?: return
        val confirmLayout = quitConfirmLayout ?: return
        val state = pendingPauseState
        val isPauseMode = pendingContent?.mode == SecondaryDisplayMode.PAUSE_MENU

        if (state == null || !isPauseMode) {
            controls.visibility = View.GONE
            confirmLayout.visibility = View.GONE
            return
        }

        overlayText?.visibility = View.GONE

        if (state.showQuitConfirm) {
            controls.visibility = View.GONE
            confirmLayout.visibility = View.VISIBLE
            return
        }

        confirmLayout.visibility = View.GONE
        controls.visibility = View.VISIBLE

        soundToggle?.isChecked = state.soundOn
        musicToggle?.isChecked = state.musicOn
        secondScreenToggle?.isChecked = state.secondScreenOn

        suppressTiltListener = true
        when (state.tiltMode) {
            1 -> flatRadio?.isChecked = true
            3 -> customRadio?.isChecked = true
            else -> tiltedRadio?.isChecked = true
        }
        suppressTiltListener = false

        resumeButton?.text = if (state.resumeCountdownSeconds > 0f) {
            ceil(state.resumeCountdownSeconds).toInt().coerceAtLeast(1).toString()
        } else {
            "Resume"
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
