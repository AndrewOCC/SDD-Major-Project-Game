package com.aocc.framework

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

class Pool<T>(private val factory: PoolObjectFactory<T>, private val maxSize: Int) {

    fun interface PoolObjectFactory<T> {
        fun createObject(): T
    }

    private val freeObjects: MutableList<T> = ArrayList(maxSize)

    fun newObject(): T {
        return if (freeObjects.isEmpty()) {
            factory.createObject()
        } else {
            freeObjects.removeAt(freeObjects.size - 1)
        }
    }

    fun free(`object`: T) {
        if (freeObjects.size < maxSize) {
            freeObjects.add(`object`)
        }
    }
}
