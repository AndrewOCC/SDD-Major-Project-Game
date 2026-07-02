package com.aocc.framework

//THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

interface Game {
    val audio: Audio

    val input: Input

    val fileIO: FileIO

    val graphics: Graphics

    fun setScreen(screen: Screen)

    val currentScreen: Screen

    val initScreen: Screen
}
