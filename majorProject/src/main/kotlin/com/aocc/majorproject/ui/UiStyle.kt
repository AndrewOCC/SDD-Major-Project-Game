package com.aocc.majorproject.ui

import android.graphics.Color

/**
 * Mid-century modern palette: warm charcoal panels, thin cream rules, a mustard accent.
 * Flat shapes and hairlines rather than chunky slabs.
 */
object UiStyle {
    val PANEL: Int = Color.argb(196, 26, 23, 20)
    val PANEL_SOLID: Int = Color.argb(236, 30, 27, 24)
    val PANEL_INNER: Int = Color.argb(120, 58, 52, 46)
    val SCRIM: Int = Color.argb(150, 14, 12, 10)
    val RULE: Int = Color.argb(200, 238, 228, 210)
    val RULE_SOFT: Int = Color.argb(90, 238, 228, 210)
    val TEXT: Int = Color.rgb(244, 238, 228)
    val TEXT_MUTED: Int = Color.rgb(190, 180, 166)
    val ACCENT: Int = Color.rgb(222, 158, 44)
    val ACCENT_PRESSED: Int = Color.rgb(176, 118, 26)
    val TEAL: Int = Color.rgb(54, 128, 128)

    const val RULE_WIDTH = 2f
    const val LETTER_SPACING = 0.02f
}
