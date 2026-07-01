package com.aocc.majorproject;

import android.graphics.Typeface;

import com.aocc.framework.Image;
import com.aocc.framework.Music;
import com.aocc.framework.NoOpMusic;
import com.aocc.framework.Sound;

public class Assets {

	private static final String[] MUSIC_FILES = {
			"darude-sandstorm.m4a",
			"game-music.ogg",
	};
	
	public static Image noise;
	public static Image menu_bg;
	public static Image game_bg;
	public static Image sign_in_base;
	public static Image sign_in_press;
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

	private static MajorProjectGame musicGame;
	
	public static Typeface plain;
	public static Typeface bold;
	
	public static void loadMusic(MajorProjectGame majorProjectGame) {
		musicGame = majorProjectGame;
		for (String musicFile : MUSIC_FILES) {
			try {
				darude = majorProjectGame.getAudio().createMusic(musicFile);
				if (!majorProjectGame.isMusicActive()) {
					darude.setVolume(0.85f);
					darude.setLooping(true);
					darude.play();
				}
				return;
			} catch (RuntimeException e) {
				CrashReporter.log(majorProjectGame, "Failed to load music: " + musicFile, e);
			}
		}

		darude = new NoOpMusic();
	}

	public static boolean hasPlayableMusic() {
		if (shouldUseSpotify()) {
			return true;
		}
		return darude != null && !(darude instanceof NoOpMusic);
	}

	public static void onSpotifyConnected() {
		if (MainMenuScreen.music) {
			pauseLocalMusic();
			resumeSpotifyPlayback();
		}
	}

	public static void setMusicVolume(float volume) {
		if (shouldUseSpotify()) {
			pauseLocalMusic();
			if (volume > 0) {
				resumeSpotifyPlayback();
			} else {
				pauseSpotifyPlayback();
			}
			return;
		}

		if (darude != null) {
			darude.setVolume(volume);
		}
	}

	public static void playMusic() {
		if (shouldUseSpotify()) {
			pauseLocalMusic();
			resumeSpotifyPlayback();
			return;
		}

		if (darude != null && MainMenuScreen.music) {
			darude.play();
		}
	}

	public static void pauseMusic() {
		pauseSpotifyPlayback();
		if (darude != null) {
			darude.pause();
		}
	}

	private static boolean shouldUseSpotify() {
		return musicGame != null
				&& musicGame.getSpotifyMusicHelper() != null
				&& musicGame.getSpotifyMusicHelper().isConnected()
				&& MainMenuScreen.music;
	}

	private static void pauseLocalMusic() {
		if (darude != null) {
			darude.pause();
			darude.setVolume(0);
		}
	}

	private static void resumeSpotifyPlayback() {
		if (musicGame != null && musicGame.getSpotifyMusicHelper() != null) {
			musicGame.getSpotifyMusicHelper().resumeOrPlay();
		}
	}

	private static void pauseSpotifyPlayback() {
		if (musicGame != null && musicGame.getSpotifyMusicHelper() != null) {
			musicGame.getSpotifyMusicHelper().pause();
		}
	}
}
