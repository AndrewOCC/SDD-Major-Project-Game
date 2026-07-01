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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		playGamesHelper = new PlayGamesHelper(this);

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
		Assets.darude.pause();
	}

	public void onShowLeaderboardsRequested(String ID) {
		playGamesHelper.showLeaderboards(ID);
	}

	public void onShowAchievementsRequested(String ID) {
		playGamesHelper.showAchievements();
	}

	public boolean isLoggedIn() {
		return playGamesHelper.isSignedIn();
	}

	public void onSignInButtonClicked() {
		if (!playGamesHelper.isSignedIn()) {
			playGamesHelper.signIn();
		}
	}

	public void onSignOutButtonClicked() {
		if (playGamesHelper.isSignedIn()) {
			playGamesHelper.signOut();
		}
	}

	public void onEnteredScore(int score) {
		if (playGamesHelper.isSignedIn()) {
			playGamesHelper.submitScore(getString(R.string.leaderboard_pacifist_mode), score);
			runOnUiThread(() -> Toast.makeText(
					getApplicationContext(),
					getString(R.string.saved_toast),
					Toast.LENGTH_SHORT).show());
		}
	}

	public void onAchievementUnlocked(String ID) {
		if (playGamesHelper.isSignedIn()) {
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
}
