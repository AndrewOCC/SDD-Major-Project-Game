package com.aocc.majorproject.ui.compose

import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.FragmentActivity
import com.aocc.majorproject.ui.ComposeOverlayBridge

/**
 * Hosts the Jetpack Compose overlay layer.
 *
 * Architecture note
 * -----------------
 * Canvas (SurfaceView) owns all world-space UI: settings panel, HUD, game-over screen.
 * These are letterbox-aligned by the game Viewport and must stay on canvas.
 *
 * Compose is reserved for FULL-SCREEN modals that replace the visible canvas — e.g. a
 * future "How to Play" page, extended settings, leaderboard browser. Such screens cover
 * the entire window independently of game world coordinates, so they never need to match
 * the letterboxed game area. Activate them by calling [showModal] and dismiss with [hide].
 *
 * The overlay view is GONE (zero-size, non-interactive) whenever nothing is shown so the
 * SurfaceView's touch events are never intercepted.
 */
class ComposeOverlayHost(activity: FragmentActivity) : ComposeOverlayBridge {

    val view: ComposeView = ComposeView(activity).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { /* reserved for future full-screen modals */ }
        visibility = View.GONE
        isClickable = false
    }

    // --- ComposeOverlayBridge (settings kept on canvas) ---

    override fun showMainMenu(listener: ComposeOverlayBridge.MainMenuListener) = Unit
    override fun showSettings(
        prompt: String,
        showMenuButton: Boolean,
        listener: ComposeOverlayBridge.SettingsListener,
    ) = Unit
    override fun hide() {
        view.visibility = View.GONE
        view.isClickable = false
    }
    override fun refreshSettings() = Unit
}
