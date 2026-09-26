package com.aocc.majorproject;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AssetScaleTest {

    @Test
    public void tierFromViewport_uses1xBelowThreshold() {
        assertEquals(1, AssetScale.tierFromViewport(1.0f));
        assertEquals(1, AssetScale.tierFromViewport(1.49f));
    }

    @Test
    public void tierFromViewport_uses2xAtOrAboveThreshold() {
        assertEquals(2, AssetScale.tierFromViewport(1.5f));
        assertEquals(2, AssetScale.tierFromViewport(2.5f));
    }

    @Test
    public void resolvePath_prefers2xFolderWhenAvailable() {
        assertEquals("2x/sound.png",
                AssetScale.resolvePath("sound.png", 2, true));
    }

    @Test
    public void resolvePath_fallsBackTo1xWhen2xMissing() {
        assertEquals("sound.png",
                AssetScale.resolvePath("sound.png", 2, false));
    }

    @Test
    public void effectivePixelScale_matchesResolvedTier() {
        assertEquals(2, AssetScale.effectivePixelScale(2, true));
        assertEquals(1, AssetScale.effectivePixelScale(2, false));
        assertEquals(1, AssetScale.effectivePixelScale(1, true));
    }
}
