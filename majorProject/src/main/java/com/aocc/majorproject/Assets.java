package com.aocc.majorproject;

import android.graphics.Typeface;

import com.aocc.framework.Image;
import com.aocc.framework.Music;
import com.aocc.framework.Sound;

public class Assets {
	
	//creates the objects for the game's assets (these are loaded in LoadingScreen.java)
	public static Image noise;
	public static Image menu_bg;
	public static Image game_bg;
	public static Image sign_in_base;
	public static Image sign_in_press;
	public static Image sign_out_base;
	public static Image sign_out_press;
	public static Image gpg_icon_leaderboards;
	public static Image gpg_icon_achievements;
	public static Image splash;
	public static Image tilt_control_flat;
	public static Image tilt_control_tilted;
	public static Image tilt_control_custom;
	public static Image tilt_control_flat_2;
	public static Image tilt_control_tilted_2;
	public static Image tilt_control_custom_2;
	public static Image sound;
	public static Image sound_muted;
	public static Image music;
	public static Image music_muted;
	public static Image tutorial;
	
	
	public static Sound tap;
	public static Sound zap;
	public static Sound powerup;
	public static Sound burn;
	public static Music darude;
	
	// fonts
	public static Typeface plain;
	public static Typeface bold;
	
	public static void loadMusic(MajorProjectGame majorProjectGame){
		darude = majorProjectGame.getAudio().createMusic("darude-sandstorm.m4a");
        if (!majorProjectGame.isMusicActive()) {
            Assets.darude.setVolume(0.85f);
            Assets.darude.setLooping(true);
            Assets.darude.play();
        }
	}	
}
