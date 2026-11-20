package com.aocc.majorproject.roomba

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomLayoutTest {

    private val layout = RoomLayout.parse(
        """{"wall":30,"furniture":[
            {"type":"sofa","x":100,"y":100,"w":100,"h":50,"solid":true},
            {"type":"lounge","x":0,"y":0,"w":10,"h":10,"solid":true,"footprint":[400,400,40,40]},
            {"type":"rug","x":0,"y":0,"w":10,"h":10}],
           "outlets":[{"x":290,"wall":1},{"y":200,"wall":0}]}"""
    )

    @Test
    fun parse_readsSolidsFootprintsAndOutlets() {
        assertEquals(30, layout.wall)
        assertEquals(2, layout.solids.size)
        assertTrue(layout.isSolid(420f, 420f))
        assertFalse(layout.isSolid(5f, 5f))
        assertEquals(290, layout.outlets[0].along)
        assertEquals(200, layout.outlets[1].along)
    }

    @Test
    fun resolveCircle_pushesOutOfFurniture() {
        val out = FloatArray(2)
        // Approaching the sofa's top edge from above, overlapping by 10.
        assertTrue(layout.resolveCircle(150f, 80f, 30f, out))
        assertEquals(150f, out[0], 0.01f)
        assertEquals(70f, out[1], 0.01f)
        assertFalse(layout.resolveCircle(150f, 300f, 30f, out))
    }

    @Test
    fun shippedApartment_parsesWithOutlets() {
        val json = File("src/main/assets/rooms/apartment.json").readText()
        val apartment = RoomLayout.parse(json)
        assertTrue(apartment.solids.isNotEmpty())
        assertTrue(apartment.outlets.isNotEmpty())
        for (outlet in apartment.outlets) {
            // A docked Roomba must not overlap furniture.
            val (x, y) = when (outlet.wall) {
                0 -> Pair(apartment.wall + 52f, outlet.along.toFloat())
                1 -> Pair(outlet.along.toFloat(), apartment.wall + 52f)
                2 -> Pair(1280f - apartment.wall - 52f, outlet.along.toFloat())
                else -> Pair(outlet.along.toFloat(), 720f - apartment.wall - 52f)
            }
            assertFalse("outlet at $x,$y is blocked", apartment.overlapsSolid(x, y, 30f))
        }
    }
}
