package com.aocc.majorproject.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aocc.framework.GameConstants
import com.aocc.framework.Viewport
import com.aocc.majorproject.ui.UiBounds
import kotlin.math.roundToInt

data class ViewportMetrics(
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float,
) {
    companion object {
        fun from(viewport: Viewport) = ViewportMetrics(
            scale = viewport.scale,
            offsetX = viewport.getLetterboxDestRect().left.toFloat(),
            offsetY = viewport.getLetterboxDestRect().top.toFloat(),
        )
    }
}

data class GameWorldMetrics(
    val width: Dp,
    val height: Dp,
)

val LocalGameWorldMetrics = compositionLocalOf {
    GameWorldMetrics(0.dp, 0.dp)
}

@Composable
fun GameWorldOverlay(
    viewport: ViewportMetrics,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val worldWidthPx = GameConstants.WORLD_WIDTH * viewport.scale
    val worldHeightPx = GameConstants.WORLD_HEIGHT * viewport.scale
    val worldWidthDp = with(density) { worldWidthPx.toDp() }
    val worldHeightDp = with(density) { worldHeightPx.toDp() }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset {
                    androidx.compose.ui.unit.IntOffset(
                        viewport.offsetX.roundToInt(),
                        viewport.offsetY.roundToInt(),
                    )
                }
                .size(worldWidthDp, worldHeightDp),
        ) {
            CompositionLocalProvider(
                LocalGameWorldMetrics provides GameWorldMetrics(worldWidthDp, worldHeightDp),
            ) {
                content()
            }
        }
    }
}

@Composable
fun WorldBox(
    bounds: UiBounds,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val metrics = LocalGameWorldMetrics.current
    val xDp = metrics.width * bounds.x / GameConstants.WORLD_WIDTH
    val yDp = metrics.height * bounds.y / GameConstants.WORLD_HEIGHT
    val wDp = metrics.width * bounds.width / GameConstants.WORLD_WIDTH
    val hDp = metrics.height * bounds.height / GameConstants.WORLD_HEIGHT

    Box(
        modifier = modifier
            .offset(x = xDp, y = yDp)
            .size(wDp, hDp),
        content = content,
    )
}

@Composable
fun worldTextSize(worldPixels: Float): TextUnit {
    val metrics = LocalGameWorldMetrics.current
    val density = LocalDensity.current
    return remember(metrics, worldPixels) {
        with(density) {
            (metrics.height.toPx() * worldPixels / GameConstants.WORLD_HEIGHT).toSp()
        }
    }
}
