package com.aocc.majorproject.ui.compose

import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import com.aocc.majorproject.Assets

@Composable
fun rememberGameTypeface(): FontFamily {
    return remember(Assets.plain) {
        val typeface = Assets.plain ?: Typeface.DEFAULT
        FontFamily(typeface)
    }
}
