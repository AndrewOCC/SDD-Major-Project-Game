package com.aocc.majorproject.display

/** Snapshot of debug parameters mirrored onto the rear display's parameters popup. */
data class SecondaryDebugState(
    val godMode: Boolean,
    val speed: Int,
)

/** Action requested from the rear display's parameters popup. */
sealed class SecondaryDebugAction {
    object ToggleGodMode : SecondaryDebugAction()
    data class SetSpeed(val speed: Int) : SecondaryDebugAction()
    object AddScore : SecondaryDebugAction()
}
