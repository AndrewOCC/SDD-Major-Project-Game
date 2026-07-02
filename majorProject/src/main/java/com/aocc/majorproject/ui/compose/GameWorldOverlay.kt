package com.aocc.majorproject.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.aocc.framework.GameConstants
import com.aocc.majorproject.ui.UiBounds
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Lays out children in 1280×720 world coordinates inside the letterboxed area,
 * computed from the overlay's actual size (matches canvas letterboxing).
 */
@Immutable
class WorldLayoutScope internal constructor(
    internal val worldWidthPx: Int,
    internal val worldHeightPx: Int,
) {
    fun textSize(worldPixels: Float): TextUnit {
        return (worldHeightPx * worldPixels / GameConstants.WORLD_HEIGHT).sp
    }
}

@Composable
fun GameWorldOverlay(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(WorldLayoutScope) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewWidth = constraints.maxWidth
        val viewHeight = constraints.maxHeight
        if (viewWidth <= 0 || viewHeight <= 0) {
            return@BoxWithConstraints
        }

        val scale = min(
            viewWidth.toFloat() / GameConstants.WORLD_WIDTH,
            viewHeight.toFloat() / GameConstants.WORLD_HEIGHT,
        )
        val contentWidthPx = (GameConstants.WORLD_WIDTH * scale).roundToInt()
        val contentHeightPx = (GameConstants.WORLD_HEIGHT * scale).roundToInt()
        val offsetXPx = ((viewWidth - contentWidthPx) / 2f).roundToInt()
        val offsetYPx = ((viewHeight - contentHeightPx) / 2f).roundToInt()

        val scope = remember(contentWidthPx, contentHeightPx) {
            WorldLayoutScope(contentWidthPx, contentHeightPx)
        }
        val density = LocalDensity.current

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetXPx, offsetYPx) }
                .size(
                    with(density) {
                        DpSize(contentWidthPx.toDp(), contentHeightPx.toDp())
                    },
                ),
        ) {
            content(scope)
        }
    }
}

@Composable
fun WorldLayoutScope.WorldBox(
    bounds: UiBounds,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val xPx = bounds.x * worldWidthPx / GameConstants.WORLD_WIDTH
    val yPx = bounds.y * worldHeightPx / GameConstants.WORLD_HEIGHT
    val wPx = bounds.width * worldWidthPx / GameConstants.WORLD_WIDTH
    val hPx = bounds.height * worldHeightPx / GameConstants.WORLD_HEIGHT

    Box(
        modifier = modifier
            .offset { IntOffset(xPx, yPx) }
            .size(
                with(density) {
                    DpSize(
                        maxOf(wPx, 1).toDp(),
                        maxOf(hPx, 1).toDp(),
                    )
                },
            ),
        content = content,
    )
}
