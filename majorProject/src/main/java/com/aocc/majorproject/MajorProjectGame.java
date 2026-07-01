package com.aocc.majorproject;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;

import com.aocc.framework.Screen;
import com.aocc.framework.implementation.AndroidGame;

public class MajorProjectGame extends AndroidGame {

	public static int screenRotation;
	final int TAP_VOL = 10;

	boolean firstTimeCreate = true;
	Typeface bold, plain;
	private PlayGamesHelper playGamesHelper;
	private SpotifyMusicHelper spotifyMusicHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		CrashReporter.install(this);
		super.onCreate(savedInstanceState);
		CrashReporter.showPreviousCrashIfPresent(this);

		try {
			playGamesHelper = new PlayGamesHelper(this);
		} catch (RuntimeException e) {
			CrashReporter.log(this, "Play Games helper failed to initialize", e);
			playGamesHelper = null;
		}

		try {
			spotifyMusicHelper = new SpotifyMusicHelper(this);
		} catch (RuntimeException e) {
			CrashReporter.log(this, "Spotify helper failed to initialize", e);
			spotifyMusicHelper = null;
		}

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				getCurrentScreen().backButton();
			}
		});
	}

	@Override
	public Screen getInitScreen() {
		if (firstTimeCreate) {
			Assets.loadMusic(this);
			firstTimeCreate = false;
		}

		return new LoadingSplashScreen(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (spotifyMusicHelper != null) {
			spotifyMusicHelper.connect();
		}
	}

	@Override
	public void onStop() {
		if (spotifyMusicHelper != null) {
			spotifyMusicHelper.disconnect();
		}
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
        getCurrentScreen().resume();
		screenRotation = getScreenRotation();
		if (playGamesHelper != null) {
			playGamesHelper.refreshSignInState();
		}
	}

	private int getScreenRotation() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			Display display = getDisplay();
			return display != null ? display.getRotation() : Display.DEFAULT_DISPLAY;
		}
		return getWindowManager().getDefaultDisplay().getRotation();
	}

	@Override
	public void onPause() {
        getCurrentScreen().pause();
		super.onPause();
		Assets.pauseMusic();
	}

	public void onShowLeaderboardsRequested(String ID) {
		if (playGamesHelper != null) {
			playGamesHelper.showLeaderboards(ID);
		} else {
			showPlayGamesUnavailable();
		}
	}

	public void onShowAchievementsRequested(String ID) {
		if (playGamesHelper != null) {
			playGamesHelper.showAchievements();
		} else {
			showPlayGamesUnavailable();
		}
	}

	public boolean isLoggedIn() {
		return playGamesHelper != null && playGamesHelper.isSignedIn();
	}

	public void onSignInButtonClicked() {
		if (playGamesHelper != null && !playGamesHelper.isSignedIn()) {
			playGamesHelper.signIn();
		} else if (playGamesHelper == null) {
			showPlayGamesUnavailable();
		}
	}

	public void onEnteredScore(int score) {
		if (playGamesHelper != null) {
			playGamesHelper.submitScore(getString(R.string.leaderboard_pacifist_mode), score);
		}
	}

	public void onAchievementUnlocked(String ID) {
		if (playGamesHelper != null && playGamesHelper.isSignedIn()) {
			playGamesHelper.unlockAchievement(ID);
        } else {
        	runOnUiThread(() -> Toast.makeText(
					getApplicationContext(),
					getString(R.string.ccccombo_unlocked),
					Toast.LENGTH_SHORT).show());
        }
	}

    public boolean isMusicActive() {
        return this.audioManager.isMusicActive();
    }

	public SpotifyMusicHelper getSpotifyMusicHelper() {
		return spotifyMusicHelper;
	}

	private void showPlayGamesUnavailable() {
		runOnUiThread(() -> Toast.makeText(
				getApplicationContext(),
				getString(R.string.play_games_unavailable),
				Toast.LENGTH_LONG).show());
	}
}
