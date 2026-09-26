package com.aocc.framework

//THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

interface Audio {
    fun createMusic(file: String): Music

    fun createSound(file: String): Sound
}
