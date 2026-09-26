package com.aocc.majorproject.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import com.aocc.framework.Image
import com.aocc.framework.implementation.AndroidImage

fun Image?.toComposeBitmap() =
    (this as? AndroidImage)?.bitmap?.asImageBitmap()

@Composable
fun GameImage(
    image: Image?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val bitmap = remember(image) { image.toComposeBitmap() }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
        )
    }
}
