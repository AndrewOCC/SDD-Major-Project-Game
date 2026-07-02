package com.aocc.framework

import com.aocc.framework.Graphics.ImageFormat

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

interface Image {
    val width: Int
    val height: Int
    val format: ImageFormat
    fun dispose()
}
