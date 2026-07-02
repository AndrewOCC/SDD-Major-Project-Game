package com.aocc.majorproject;

import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;

import com.aocc.framework.Screen;
import com.aocc.framework.implementation.AndroidGame;
import com.aocc.majorproject.display.SecondaryDisplayManager;
import com.aocc.majorproject.input.GamepadInput;

public class MajorProjectGame extends AndroidGame {

	public static int screenRotation;
	final int TAP_VOL = 10;

	private PlayGamesHelper playGamesHelper;
	private SecondaryDisplayManager secondaryDisplayManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		CrashReporter.install(this);
		GamePreferences.load(this);
		super.onCreate(savedInstanceState);
		CrashReporter.showPreviousCrashIfPresent(this);

		secondaryDisplayManager = new SecondaryDisplayManager(this);

		try {
			playGamesHelper = new PlayGamesHelper(this);
		} catch (RuntimeException e) {
			CrashReporter.log(this, "Play Games helper failed to initialize", e);
			playGamesHelper = null;
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
		return new LoadingScreen(this);
	}

	@Override
	public void setScreen(Screen screen) {
		super.setScreen(screen);
		if (secondaryDisplayManager != null) {
			secondaryDisplayManager.updateForScreen(screen);
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN && getGamepadInput().onKeyDown(event)) {
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void onDestroy() {
		if (secondaryDisplayManager != null) {
			secondaryDisplayManager.shutdown();
			secondaryDisplayManager = null;
		}
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
        getCurrentScreen().resume();
		screenRotation = getScreenRotation();
		if (secondaryDisplayManager != null) {
			secondaryDisplayManager.refresh();
		}
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

	public SecondaryDisplayManager getSecondaryDisplayManager() {
		return secondaryDisplayManager;
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

	private void showPlayGamesUnavailable() {
		runOnUiThread(() -> Toast.makeText(
				getApplicationContext(),
				getString(R.string.play_games_unavailable),
				Toast.LENGTH_LONG).show());
	}

	public void showSecondDisplayUnavailable() {
		runOnUiThread(() -> Toast.makeText(
				getApplicationContext(),
				getString(R.string.second_display_unavailable),
				Toast.LENGTH_LONG).show());
	}
}
