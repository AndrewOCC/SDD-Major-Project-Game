package com.aocc.majorproject

/**
 * Selects 1× or 2× bitmap assets based on viewport scale.
 *
 * High-density assets live under `assets/2x/` with the same filenames as 1×.
 * They are drawn at the same world size (pixel dimensions ÷ 2).
 */
object AssetScale {

    /** Use 2× assets when the viewport scale is at least this value. */
    const val TWO_X_THRESHOLD = 1.5f

    const val TWO_X_FOLDER = "2x/"

    @JvmStatic
    fun tierFromViewport(viewportScale: Float): Int {
        return if (viewportScale >= TWO_X_THRESHOLD) 2 else 1
    }

    /**
     * @param has2xAsset whether `2x/<fileName>` exists in the asset pack
     */
    @JvmStatic
    fun resolvePath(fileName: String, requestedTier: Int, has2xAsset: Boolean): String {
        return if (requestedTier >= 2 && has2xAsset) {
            TWO_X_FOLDER + fileName
        } else {
            fileName
        }
    }

    @JvmStatic
    fun effectivePixelScale(requestedTier: Int, has2xAsset: Boolean): Int {
        return if (requestedTier >= 2 && has2xAsset) 2 else 1
    }
}
