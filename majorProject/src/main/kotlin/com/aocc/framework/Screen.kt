package com.aocc.framework

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

abstract class Screen(@JvmField protected val game: Game) {

    abstract fun update(deltaTime: Float)

    abstract fun paint(deltaTime: Float)

    abstract fun pause()

    abstract fun resume()

    abstract fun dispose()

    abstract fun backButton()
}
