package com.aocc.majorproject

/**
 * A formation that keeps positioning its member dots for a while after spawning
 * (e.g. an arrow that rotates to keep aiming at the player), then releases them.
 * Updated by [EnemyController] each frame until [isComplete] returns true.
 */
interface ActiveFormation {
    fun update(deltaSeconds: Float)
    fun isComplete(): Boolean
}
