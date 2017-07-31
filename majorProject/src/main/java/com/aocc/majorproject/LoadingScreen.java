package com.aocc.majorproject;

import android.graphics.Typeface;

import com.aocc.framework.Graphics;
import com.aocc.framework.Screen;
import com.aocc.framework.Graphics.ImageFormat;


public class LoadingScreen extends Screen {
	
	MajorProjectGame majorProjectGame;
	
    public LoadingScreen(MajorProjectGame game) {
        super(game);
        majorProjectGame = game;
        
    }


    @Override
    public void update(float deltaTime) {
        Graphics g = game.getGraphics();
        
    	//loads the resources created in the Assets class
        Assets.noise = g.newImage("noise.png", ImageFormat.ARGB4444);
        Assets.menu_bg = g.newImage("menu-bg.png", ImageFormat.RGB565);
        
        //fonts used here are "Yerevan" and "Radian" by "Objects Dart (Darren Rigby)"
        Assets.game_bg = g.newImage("game-bg.png", ImageFormat.RGB565);
        Assets.sign_in_base = g.newImage("Red-signin_Medium_base.png", ImageFormat.ARGB4444);
        Assets.sign_in_press = g.newImage("Red-signin_Medium_press.png", ImageFormat.ARGB4444);
        Assets.sign_out_base = g.newImage("Red-signout_Medium_base.png", ImageFormat.ARGB4444);
        Assets.sign_out_press = g.newImage("Red-signout_Medium_press.png", ImageFormat.ARGB4444);
        Assets.gpg_icon_leaderboards = g.newImage("gpg-icon-leaderboards.png", ImageFormat.RGB565);
        Assets.gpg_icon_achievements = g.newImage("gpg-icon-achievements.png", ImageFormat.RGB565);
        Assets.tilt_control_flat = g.newImage("tilt-button-flat.png", ImageFormat.ARGB4444);
        Assets.tilt_control_tilted = g.newImage("tilt-button-tilted.png", ImageFormat.ARGB4444);
        Assets.tilt_control_custom = g.newImage("tilt-button-custom.png", ImageFormat.ARGB4444);
        Assets.tilt_control_flat_2 = g.newImage("tilt-button-flat-2.png", ImageFormat.ARGB4444);
        Assets.tilt_control_tilted_2 = g.newImage("tilt-button-tilted-2.png", ImageFormat.ARGB4444);
        Assets.tilt_control_custom_2 = g.newImage("tilt-button-custom-2.png", ImageFormat.ARGB4444);
        Assets.sound = g.newImage("sound.png", ImageFormat.ARGB4444);
        Assets.sound_muted = g.newImage("sound-muted.png", ImageFormat.ARGB4444);
        Assets.music = g.newImage("music.png", ImageFormat.ARGB4444);
        Assets.music = g.newImage("music.png", ImageFormat.ARGB4444);
        Assets.music_muted = g.newImage("music-muted.png", ImageFormat.ARGB4444);
        Assets.tutorial = g.newImage("tutorial.png", ImageFormat.RGB565);
        
        Assets.tap = game.getAudio().createSound("tap.wav");
        Assets.zap = game.getAudio().createSound("zap.wav");
        Assets.burn = game.getAudio().createSound("burn.wav");
        Assets.powerup = game.getAudio().createSound("powerup.wav");
        
        Assets.plain = Typeface.createFromAsset(majorProjectGame.getAssets(), "fonts/power_clear.ttf"); 
    	Assets.bold = Typeface.create(Assets.plain, Typeface.BOLD);


        //loads the main menu screen
        game.setScreen(new MainMenuScreen(majorProjectGame));


    }


    @Override
    public void paint(float deltaTime) {


    }


    @Override
    public void pause() {


    }


    @Override
    public void resume() {


    }


    @Override
    public void dispose() {


    }


    @Override
    public void backButton() {


    }
}
