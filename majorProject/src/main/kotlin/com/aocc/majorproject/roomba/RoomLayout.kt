package com.aocc.majorproject.roomba

import android.graphics.RectF
import com.aocc.framework.GameConstants
import org.json.JSONObject
import kotlin.math.sqrt

/**
 * A room's gameplay geometry, read from the same `rooms/<name>.json` the Blender render uses:
 * wall thickness, solid furniture footprints and the outlets a charging dock can plug into.
 */
class RoomLayout(
    val wall: Int,
    val solids: List<RectF>,
    val outlets: List<Outlet>,
) {

    /** A dock position: [along] is the x (top/bottom walls) or y (left/right walls) of the pad. */
    class Outlet(val wall: Int, val along: Int)

    val innerLeft: Float get() = wall.toFloat()
    val innerTop: Float get() = wall.toFloat()
    val innerRight: Float get() = (GameConstants.WORLD_WIDTH - wall).toFloat()
    val innerBottom: Float get() = (GameConstants.WORLD_HEIGHT - wall).toFloat()

    fun isSolid(x: Float, y: Float): Boolean = solids.any { it.contains(x, y) }

    /** Whether a circle at ([x], [y]) of [radius] overlaps any furniture. */
    fun overlapsSolid(x: Float, y: Float, radius: Float): Boolean =
        solids.any { distanceSq(it, x, y) < radius * radius }

    /**
     * Pushes a circle out of any furniture it overlaps. Returns the corrected centre in [out]
     * (x at index 0, y at index 1) and whether a push happened.
     */
    fun resolveCircle(x: Float, y: Float, radius: Float, out: FloatArray): Boolean {
        var cx = x
        var cy = y
        var pushed = false
        for (rect in solids) {
            val nearestX = cx.coerceIn(rect.left, rect.right)
            val nearestY = cy.coerceIn(rect.top, rect.bottom)
            val dx = cx - nearestX
            val dy = cy - nearestY
            val distSq = dx * dx + dy * dy
            if (distSq >= radius * radius) {
                continue
            }
            pushed = true
            if (distSq > 1e-4f) {
                val dist = sqrt(distSq)
                cx = nearestX + dx / dist * radius
                cy = nearestY + dy / dist * radius
            } else {
                // Centre inside the rectangle: leave by the nearest edge.
                val toLeft = cx - rect.left
                val toRight = rect.right - cx
                val toTop = cy - rect.top
                val toBottom = rect.bottom - cy
                when (minOf(minOf(toLeft, toRight), minOf(toTop, toBottom))) {
                    toLeft -> cx = rect.left - radius
                    toRight -> cx = rect.right + radius
                    toTop -> cy = rect.top - radius
                    else -> cy = rect.bottom + radius
                }
            }
        }
        out[0] = cx
        out[1] = cy
        return pushed
    }

    private fun distanceSq(rect: RectF, x: Float, y: Float): Float {
        val dx = x - x.coerceIn(rect.left, rect.right)
        val dy = y - y.coerceIn(rect.top, rect.bottom)
        return dx * dx + dy * dy
    }

    companion object {
        /** Bare room used before assets load (and in tests that don't load them). */
        val EMPTY = RoomLayout(
            wall = 30,
            solids = emptyList(),
            outlets = listOf(Outlet(1, 640), Outlet(3, 640), Outlet(0, 360), Outlet(2, 360)),
        )

        fun parse(json: String): RoomLayout {
            val root = JSONObject(json)
            val solids = ArrayList<RectF>()
            val furniture = root.optJSONArray("furniture")
            if (furniture != null) {
                for (i in 0 until furniture.length()) {
                    val f = furniture.getJSONObject(i)
                    if (!f.optBoolean("solid", false)) {
                        continue
                    }
                    val footprint = f.optJSONArray("footprint")
                    if (footprint != null) {
                        val x = footprint.getDouble(0).toFloat()
                        val y = footprint.getDouble(1).toFloat()
                        solids.add(RectF(x, y, x + footprint.getDouble(2).toFloat(), y + footprint.getDouble(3).toFloat()))
                    } else {
                        val x = f.getDouble("x").toFloat()
                        val y = f.getDouble("y").toFloat()
                        solids.add(RectF(x, y, x + f.getDouble("w").toFloat(), y + f.getDouble("h").toFloat()))
                    }
                }
            }
            val outlets = ArrayList<Outlet>()
            val outletArray = root.optJSONArray("outlets")
            if (outletArray != null) {
                for (i in 0 until outletArray.length()) {
                    val o = outletArray.getJSONObject(i)
                    val wall = o.getInt("wall")
                    val along = if (wall == 0 || wall == 2) o.getInt("y") else o.getInt("x")
                    outlets.add(Outlet(wall, along))
                }
            }
            return RoomLayout(root.getInt("wall"), solids, outlets.ifEmpty { EMPTY.outlets })
        }
    }
}
