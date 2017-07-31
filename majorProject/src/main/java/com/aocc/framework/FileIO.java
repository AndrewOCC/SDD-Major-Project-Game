package com.aocc.framework;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.SharedPreferences;

//THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

public interface FileIO {
    public InputStream readFile(String file) throws IOException;

    public OutputStream writeFile(String file) throws IOException;
    
    public InputStream readAsset(String file) throws IOException;
    
    public SharedPreferences getSharedPref();
}
