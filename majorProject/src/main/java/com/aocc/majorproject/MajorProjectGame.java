package com.aocc.majorproject;

// password: o@YMudFr1FDDHEruKJeA

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.aocc.framework.Screen;
import com.aocc.framework.implementation.AndroidGame;
import com.google.android.gms.games.Games;

public class MajorProjectGame extends AndroidGame {
	
	public static int screenRotation;
	final int TAP_VOL = 10;

	boolean firstTimeCreate = true;
	Typeface bold, plain;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		//enableDebugLog(true);
		super.onCreate(savedInstanceState);
		
	}
	
	@Override
	public Screen getInitScreen() {
		
		
		if (firstTimeCreate) {
			Assets.loadMusic(this);
			firstTimeCreate = false;
		}
		
		//creates splash screen loading screen
		return new LoadingSplashScreen(this);
		
	}

	@Override
	public void onBackPressed() {
		getCurrentScreen().backButton();
	}
	
	public void onResume() {
		super.onResume();
        getCurrentScreen().resume();
		//Assets.darude.play();
		WindowManager windowMgr = (WindowManager) this.getSystemService(Activity.WINDOW_SERVICE);
		screenRotation = windowMgr.getDefaultDisplay().getRotation();
	}
	
	public void onPause() {
        getCurrentScreen().pause();
		super.onPause();
		Assets.darude.pause();
	}
	

	//@Override
	public void onSignInFailed() {
		// TODO Auto-generated method stub
		
	}

	//@Override
	public void onSignInSucceeded() {
		// TODO Auto-generated method stub
		
	}

	public void onShowAchievementsRequested() {
		// TODO Auto-generated method stub
		
	}
	
	public void onShowLeaderboardsRequested(String ID) {
        if (isSignedIn()) {
        	if (ID == ""){
        		startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(getApiClient())
        				, 5001);
        	} else {
        		startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(), ID)
        				, 5000);
        	}
        } else {
            showAlert(getString(R.string.leaderboards_not_available));
        }	
	}
	
	public void onShowAchievementsRequested(String ID) {
        if (isSignedIn()) {
        	if (ID == ""){
        		startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), 5001);
        	} else {
        		startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), 5000);
        	}
        } else {
            showAlert(getString(R.string.leaderboards_not_available));
        }	
	}
	

	public boolean isLoggedIn(){
		return isSignedIn();
	}
	
	public void onSignInButtonClicked() {
		if (!isSignedIn()) {
			beginUserInitiatedSignIn();
		}
	}

	public void onSignOutButtonClicked() {
		if (isSignedIn()) {
			signOut();
		}
		
	}

	public void onEnteredScore(int score) {
		if (isSignedIn()) {
			Games.Leaderboards.submitScore(getApiClient(), getString(R.string.leaderboard_pacifist_mode), score);
			this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), getString(R.string.saved_toast), Toast.LENGTH_SHORT).show();
                }
            });
		}
	}
	
	public void onAchievementUnlocked(String ID) {
		if (isSignedIn()) {
			Games.Achievements.unlock(getApiClient(), ID);
        } else {
        	this.runOnUiThread(new Runnable() {
				  public void run() {
					  Toast.makeText(getApplicationContext(), getString(R.string.ccccombo_unlocked), Toast.LENGTH_SHORT).show();
				  }
				}
			);
        }	
	}

    public boolean isMusicActive() {
        return this.audioManager.isMusicActive();
    }
	
}
