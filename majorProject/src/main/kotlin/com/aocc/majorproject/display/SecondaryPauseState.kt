package com.aocc.majorproject.display

/** Snapshot of pause-menu state mirrored onto the secondary (rear) display, if any. */
data class SecondaryPauseState(
    val soundOn: Boolean,
    val musicOn: Boolean,
    val secondScreenOn: Boolean,
    val tiltMode: Int,
    val resumeCountdownSeconds: Float,
    val showQuitConfirm: Boolean,
)
