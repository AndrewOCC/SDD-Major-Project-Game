package com.aocc.framework.implementation;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.aocc.framework.Audio;
import com.aocc.framework.FileIO;
import com.aocc.framework.Game;
import com.aocc.framework.GameConstants;
import com.aocc.framework.Graphics;
import com.aocc.framework.Input;
import com.aocc.framework.Screen;
import com.aocc.framework.Viewport;
import com.aocc.majorproject.ui.ComposeOverlayBridge;
import com.aocc.majorproject.ui.compose.ComposeOverlayHost;
import androidx.fragment.app.FragmentActivity;

// Note: this code was heavily modified for the purposes of the major project, including
// changes to the size of windows, SDK versions and handling orientation. Similarly,
// code from the google framework references this instead of 'Fragments' in order to allow
// the MajorProjectGame class to extend both this class and the GPG helper class. This
// is otherwise not possible


public abstract class AndroidGame extends FragmentActivity implements Game {
    AndroidFastRenderView renderView;
    Graphics graphics;
    Audio audio;
    Input input;
    FileIO fileIO;
    Screen screen;
    public AudioManager audioManager;
    private final Viewport viewport = new Viewport();
    private ComposeOverlayHost composeOverlayHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Bitmap frameBuffer = Bitmap.createBitmap(GameConstants.WORLD_WIDTH,
                GameConstants.WORLD_HEIGHT, Config.RGB_565);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Rect bounds = getWindowManager().getCurrentWindowMetrics().getBounds();
            viewport.update(Math.max(1, bounds.width()), Math.max(1, bounds.height()));
        } else {
            Point size = new Point();
            Display display = getWindowManager().getDefaultDisplay();
            display.getSize(size);
            viewport.update(Math.max(1, size.x), Math.max(1, size.y));
        }

        renderView = new AndroidFastRenderView(this, frameBuffer);
        graphics = new AndroidGraphics(getAssets(), frameBuffer);
        fileIO = new AndroidFileIO(this);
        audio = new AndroidAudio(this);
        input = new AndroidInput(this, renderView, viewport);

        composeOverlayHost = new ComposeOverlayHost(this);

        FrameLayout root = new FrameLayout(this);
        root.addView(renderView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(composeOverlayHost.getView(), new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        screen = getInitScreen();
        setContentView(root);
        configureImmersiveWindow();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void configureImmersiveWindow() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat insetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars());
            insetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        configureImmersiveWindow();
        if (screen != null) {
            screen.resume();
        }
        if (renderView != null) {
            renderView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (renderView != null) {
            renderView.pause();
        }
        if (screen != null) {
            screen.pause();
        }

        if (isFinishing() && screen != null)
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

    public Viewport getViewport() {
        return viewport;
    }

    public void updateInputViewport(Viewport viewport) {
        if (input instanceof AndroidInput) {
            ((AndroidInput) input).updateViewport(viewport);
        }
    }

    public ComposeOverlayBridge getComposeOverlay() {
        return composeOverlayHost;
    }
}
