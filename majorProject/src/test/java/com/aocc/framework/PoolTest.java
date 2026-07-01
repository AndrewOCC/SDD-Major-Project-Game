package com.aocc.framework;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class PoolTest {

    private Pool<StringBuilder> pool;
    private int createdCount;

    @Before
    public void setUp() {
        createdCount = 0;
        pool = new Pool<>(() -> {
            createdCount++;
            return new StringBuilder("object-" + createdCount);
        }, 2);
    }

    @Test
    public void newObject_createsFreshObjectWhenPoolIsEmpty() {
        StringBuilder first = pool.newObject();
        StringBuilder second = pool.newObject();

        assertNotNull(first);
        assertNotNull(second);
        assertNotSame(first, second);
        assertEquals(2, createdCount);
    }

    @Test
    public void freeAndNewObject_reusesFreedInstance() {
        StringBuilder first = pool.newObject();
        pool.free(first);

        StringBuilder reused = pool.newObject();

        assertSame(first, reused);
        assertEquals(1, createdCount);
    }

    @Test
    public void free_doesNotStoreMoreThanMaxSizeObjects() {
        StringBuilder first = pool.newObject();
        StringBuilder second = pool.newObject();
        StringBuilder third = pool.newObject();

        pool.free(first);
        pool.free(second);
        pool.free(third);

        StringBuilder reusedOne = pool.newObject();
        StringBuilder reusedTwo = pool.newObject();
        StringBuilder createdAgain = pool.newObject();

        assertSame(second, reusedOne);
        assertSame(first, reusedTwo);
        assertNotSame(first, createdAgain);
        assertEquals(4, createdCount);
    }

    private static void assertEquals(int expected, int actual) {
        org.junit.Assert.assertEquals(expected, actual);
    }
}
