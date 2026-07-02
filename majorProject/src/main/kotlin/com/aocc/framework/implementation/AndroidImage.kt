package com.aocc.framework.implementation

import android.graphics.Bitmap
import com.aocc.framework.Graphics.ImageFormat
import com.aocc.framework.Image

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

class AndroidImage : Image {
    val bitmap: Bitmap
    override val format: ImageFormat
    /** 1 for standard assets; 2 when loaded from the 2× folder (logical size = pixels ÷ 2). */
    val pixelScale: Int

    constructor(bitmap: Bitmap, format: ImageFormat) : this(bitmap, format, 1)

    constructor(bitmap: Bitmap, format: ImageFormat, pixelScale: Int) {
        this.bitmap = bitmap
        this.format = format
        this.pixelScale = maxOf(1, pixelScale)
    }

    override val width: Int
        get() = bitmap.width / pixelScale

    override val height: Int
        get() = bitmap.height / pixelScale

    override fun dispose() {
        bitmap.recycle()
    }
}
