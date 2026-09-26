package com.aocc.framework.implementation

import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.preference.PreferenceManager
import com.aocc.framework.FileIO
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

class AndroidFileIO(context: Context) : FileIO {
    private val context: Context = context
    private val assets: AssetManager = context.assets
    private val externalStoragePath: String = context.filesDir.absolutePath + File.separator

    @Throws(IOException::class)
    override fun readAsset(file: String): InputStream {
        return assets.open(file)
    }

    @Throws(IOException::class)
    override fun readFile(file: String): InputStream {
        return FileInputStream(externalStoragePath + file)
    }

    @Throws(IOException::class)
    override fun writeFile(file: String): OutputStream {
        return FileOutputStream(externalStoragePath + file)
    }

    override fun getSharedPref(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}
