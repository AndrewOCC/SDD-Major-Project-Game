package com.aocc.framework

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class PoolTest {

    private lateinit var pool: Pool<StringBuilder>
    private var createdCount: Int = 0

    @Before
    fun setUp() {
        createdCount = 0
        pool = Pool({
            createdCount++
            StringBuilder("object-$createdCount")
        }, 2)
    }

    @Test
    fun newObject_createsFreshObjectWhenPoolIsEmpty() {
        val first = pool.newObject()
        val second = pool.newObject()

        assertNotNull(first)
        assertNotNull(second)
        assertNotSame(first, second)
        assertEquals(2, createdCount)
    }

    @Test
    fun freeAndNewObject_reusesFreedInstance() {
        val first = pool.newObject()
        pool.free(first)

        val reused = pool.newObject()

        assertSame(first, reused)
        assertEquals(1, createdCount)
    }

    @Test
    fun free_doesNotStoreMoreThanMaxSizeObjects() {
        val first = pool.newObject()
        val second = pool.newObject()
        val third = pool.newObject()

        pool.free(first)
        pool.free(second)
        pool.free(third)

        val reusedOne = pool.newObject()
        val reusedTwo = pool.newObject()
        val createdAgain = pool.newObject()

        assertSame(second, reusedOne)
        assertSame(first, reusedTwo)
        assertNotSame(first, createdAgain)
        assertEquals(4, createdCount)
    }
}
