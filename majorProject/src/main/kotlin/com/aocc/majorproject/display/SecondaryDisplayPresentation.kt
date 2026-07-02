package com.aocc.majorproject.display

import android.app.Presentation
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.Display
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.aocc.framework.Image
import com.aocc.framework.implementation.AndroidImage

/** Full-screen content on a secondary display. */
class SecondaryDisplayPresentation(
    context: Context,
    display: Display,
) : Presentation(context, display) {

    private var imageView: ImageView? = null
    private var overlayText: TextView? = null
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
            textView.visibility = TextView.GONE
            root.addView(
                textView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        setContentView(root)
        applyPendingContent()
    }

    fun setMode(mode: SecondaryDisplayMode, background: Image?, overlayLabel: String?) {
        pendingContent = PendingContent(mode, background, overlayLabel)
        applyPendingContent()
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

        val overlayLabel = pending.overlayLabel
        val overlayText = overlayText ?: return
        if (!overlayLabel.isNullOrEmpty()) {
            overlayText.text = overlayLabel
            overlayText.visibility = TextView.VISIBLE
        } else {
            overlayText.visibility = TextView.GONE
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
