package com.aocc.majorproject;

import com.aocc.framework.GameConstants;
import com.aocc.framework.implementation.RotationHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class PlayerTest {

    private static final float ONE_FRAME = 1f / GameConstants.REFERENCE_FPS;

    private GameSession session;
    private Player player;

    @Before
    public void setUp() {
        session = new GameSession();
        setRotation(0f, 0f);
        player = session.getPlayer();
    }

    @Test
    public void update_clampsPlayerInsideLeftBoundary() {
        player.setDefaultX(0);
        player.setDefaultY(320);
        setRotation(-90f, 0f);

        player.update(ONE_FRAME);

        assertEquals(3f, player.getDefaultX(), 0.001f);
        assertEquals(0f, player.getVelocityX(), 0.001f);
    }

    @Test
    public void update_clampsPlayerInsideRightBoundary() {
        player.setDefaultX(1220);
        player.setDefaultY(320);
        setRotation(90f, 0f);

        player.update(ONE_FRAME);

        assertTrue(player.getDefaultX() < 1280);
        assertEquals(0f, player.getVelocityX(), 0.001f);
    }

    @Test
    public void update_decrementsShieldEachFrame() {
        player.setShield(100);

        player.update(ONE_FRAME);

        assertEquals(99, player.getShield());
    }

    @Test
    public void update_setsGameOverWhenHealthReachesZero() {
        player.setHealth(0);

        player.update(ONE_FRAME);

        assertTrue(session.isGameOverFlag());
    }

    @Test
    public void update_clampsHealthToMaximumValue() {
        player.setHealth(10);

        player.update(ONE_FRAME);

        assertEquals(5, player.getHealth());
    }

    private static void setRotation(float x, float y) {
        setStaticField("screenX", x);
        setStaticField("screenY", y);
    }

    private static void setStaticField(String name, float value) {
        try {
            java.lang.reflect.Field field = RotationHandler.class.getDeclaredField(name);
            field.setAccessible(true);
            field.setFloat(null, value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
