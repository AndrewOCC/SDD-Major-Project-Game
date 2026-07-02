package com.aocc.majorproject;

/**
 * Selects 1× or 2× bitmap assets based on viewport scale.
 * <p>
 * High-density assets live under {@code assets/2x/} with the same filenames as 1×.
 * They are drawn at the same world size (pixel dimensions ÷ 2).
 */
public final class AssetScale {

    /** Use 2× assets when the viewport scale is at least this value. */
    public static final float TWO_X_THRESHOLD = 1.5f;

    public static final String TWO_X_FOLDER = "2x/";

    private AssetScale() {
    }

    public static int tierFromViewport(float viewportScale) {
        return viewportScale >= TWO_X_THRESHOLD ? 2 : 1;
    }

    /**
     * @param has2xAsset whether {@code 2x/<fileName>} exists in the asset pack
     */
    public static String resolvePath(String fileName, int requestedTier, boolean has2xAsset) {
        if (requestedTier >= 2 && has2xAsset) {
            return TWO_X_FOLDER + fileName;
        }
        return fileName;
    }

    public static int effectivePixelScale(int requestedTier, boolean has2xAsset) {
        return requestedTier >= 2 && has2xAsset ? 2 : 1;
    }
}
