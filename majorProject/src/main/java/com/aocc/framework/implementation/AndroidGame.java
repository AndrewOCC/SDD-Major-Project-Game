package com.aocc.framework.implementation;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import com.aocc.framework.Audio;
import com.aocc.framework.FileIO;
import com.aocc.framework.Game;
import com.aocc.framework.Graphics;
import com.aocc.framework.Input;
import com.aocc.framework.Screen;
import com.google.example.games.basegameutils.BaseGameActivity;

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

// Note: this code was heavily modified for the purposes of the major project, including
// changes to the size of windows, SDK versions and handling orientation. Similarly,
// code from the google framework references this instead of 'Fragments' in order to allow
// the MajorProjectGame class to extend both this class and the GPG helper class. This
// is otherwise not possible


public abstract class AndroidGame extends BaseGameActivity implements Game {
    AndroidFastRenderView renderView;
    Graphics graphics;
    Audio audio;
    Input input;
    FileIO fileIO;
    Screen screen;
    public AudioManager audioManager;
    
	// Added to allow lint (android sdk checker) to ignore this section, as
	// code is executing different code for different sdk versions
    @SuppressWarnings("deprecation")
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        // handles audio services
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

    	//works out SDK version for version-specific code
    	final int version = android.os.Build.VERSION.SDK_INT;

        //parameters for the created window
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        float scaleX;
        float scaleY;
        
        //handles device orientation
        	
        // modified to set display to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);	
        //boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        //int frameBufferWidth = isPortrait ? 720: 1280;
        //int frameBufferHeight = isPortrait ? 1280: 720;
        int frameBufferWidth = 1280;
        int frameBufferHeight = 720;
        
        Bitmap frameBuffer = Bitmap.createBitmap(frameBufferWidth,
                frameBufferHeight, Config.RGB_565);
        
        // sets display variable for use
        Display display = getWindowManager().getDefaultDisplay();
        
        // handles resolution, checking the sdk version
        if (version >= 13) {
        	Point size = new Point();
            display.getSize(size);
            scaleX = (float) frameBufferWidth / size.x;
            scaleY = (float) frameBufferHeight / size.y;
        }
        
        else {		// for older versions of the SDK
        	scaleX = (float) frameBufferWidth
             / getWindowManager().getDefaultDisplay().getWidth();
            scaleY = (float) frameBufferHeight
                    / getWindowManager().getDefaultDisplay().getHeight();
        }

        //initiates the various android interfaces/implementations
        renderView = new AndroidFastRenderView(this, frameBuffer);
        graphics = new AndroidGraphics(getAssets(), frameBuffer);
        fileIO = new AndroidFileIO(this);
        audio = new AndroidAudio(this);
        input = new AndroidInput(this, renderView, scaleX, scaleY);
        
        //loads the 'initscreen'- in this case the loading screen
        screen = getInitScreen();
        setContentView(renderView);
        
        //keeps the screen on while app is active
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onResume() {
        super.onResume();
        screen.resume();
        renderView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        renderView.pause();
        screen.pause();

        if (isFinishing())
            screen.dispose();
    }

    
    // GETTERS AND SETTERS FOR THE FRAMEWORK
    
    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public FileIO getFileIO() {
        return fileIO;
    }

    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    @Override
    public void setScreen(Screen screen) {
        if (screen == null)
            throw new IllegalArgumentException("Screen must not be null");

        this.screen.pause();
        this.screen.dispose();
        screen.resume();
        screen.update(0);
        this.screen = screen;
    }
    
    public Screen getCurrentScreen() {

    	return screen;
    }
}