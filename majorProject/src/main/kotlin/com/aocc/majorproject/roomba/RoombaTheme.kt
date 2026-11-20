package com.aocc.majorproject.roomba

import android.graphics.Color

/** Palette for the Roomba / apartment theme. */
object RoombaTheme {
    /** Ambient light on sprites this frame (see [DayCycle.ambientAt]); white = as rendered. */
    @Volatile
    var ambient: Int = Color.WHITE

    /** Multiplies [color] by [ambient], keeping its alpha. */
    fun lit(color: Int): Int {
        val a = ambient
        if (a == Color.WHITE) {
            return color
        }
        return Color.argb(
            Color.alpha(color),
            Color.red(color) * Color.red(a) / 255,
            Color.green(color) * Color.green(a) / 255,
            Color.blue(color) * Color.blue(a) / 255,
        )
    }

    val DUST: Int = Color.rgb(176, 166, 150)

    val ROOMBA_BODY: Int = Color.rgb(42, 44, 48)
    val ROOMBA_LID: Int = Color.rgb(62, 66, 72)
    val ROOMBA_BUMPER: Int = Color.rgb(28, 29, 32)
    val ROOMBA_BUTTON: Int = Color.rgb(30, 32, 35)
    val SUCTION: Int = Color.rgb(120, 214, 255)

    val MITE_BODY: Int = Color.rgb(124, 104, 140)
    val MITE_FUZZ: Int = Color.rgb(92, 76, 108)
    val MITE_BELLY: Int = Color.rgb(160, 142, 172)
    val MITE_PUPIL: Int = Color.rgb(220, 30, 40)

    val DOCK_BASE: Int = Color.rgb(48, 50, 56)
    val DOCK_PAD: Int = Color.rgb(70, 74, 82)
    val DOCK_GLOW: Int = Color.rgb(90, 220, 140)
    val BIN_EMPTY: Int = Color.rgb(255, 160, 60)
}
