package com.aocc.majorproject;

import java.util.List;

import android.util.Log;

import com.aocc.framework.Graphics;
import com.aocc.framework.PersonalMethods;
import com.aocc.framework.Screen;
import com.aocc.framework.Input.TouchEvent;

public class MainMenuScreen extends Screen {

	// for non-static references within code, this holds the MajorProjectGame object
	MajorProjectGame majorProjectGame;

	// stores whether sign in/out button is pressed
	int signInOutPressed = -1;
	public static int tapVol = 10;

    public static boolean music;
    public static boolean sound = true;


	public MainMenuScreen(MajorProjectGame game) {
		super(game);
		majorProjectGame = game;

        if (majorProjectGame.isMusicActive()) {
            music = false;
        } else {
            music = true;
        }

	}
    
	@Override
	public void update(float deltaTime) {
		
		//creates list of all touch events
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
		int len = touchEvents.size();
		
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);	// **add error detection
				
			if (event.type == TouchEvent.TOUCH_UP) {
					
		    	// Play button
		    	if (PersonalMethods.touchInBounds(event, 140, 435, 440, 200)) {
		    		Assets.tap.play(tapVol);
		    		//start game
		        	game.setScreen(new GameScreen(majorProjectGame));
		        }
		    	
		    	if (PersonalMethods.touchInBounds(event, 700, 435, 440, 200)) {
		    		Assets.tap.play(tapVol);
		    		// loads tutorial
		        	game.setScreen(new TutorialScreen(majorProjectGame));
		        }
		    	
		    	// Sign In/Out button
		    	if (PersonalMethods.touchInBounds(event, 7, 7, 180, 60)) {
		    		Assets.tap.play(tapVol);
		    		signInOutPressed = i;
		    		if (majorProjectGame.isLoggedIn()) {
			        	try{
			        		majorProjectGame.onSignOutButtonClicked();
			            }
			            catch (Exception e) {	// error checking
			                Log.d("MajorProjectGame", "Unable to log out" + e);
			            }
			        } else {
			        	majorProjectGame.onSignInButtonClicked();
			        }
		    	}
		    	
		    	// Highscores button
		    	if (PersonalMethods.touchInBounds(event, 1175, 5, 100, 80)) {
		    		Assets.tap.play(tapVol);
		    		if (majorProjectGame.isLoggedIn()){
		    			majorProjectGame.onShowLeaderboardsRequested("");	
		    		}
		        }
		    	
		    	// Achievements button
		    	if (PersonalMethods.touchInBounds(event, 1175, 105, 100, 80)) {
		    		Assets.tap.play(tapVol);
		    		if (majorProjectGame.isLoggedIn()){
		    			majorProjectGame.onShowAchievementsRequested("");
		    		}
		        	
		        }
		    	
			} else{
				signInOutPressed = -1;
			}
    	}
	}

	@Override
	public void paint(float deltaTime) {
		Graphics g = game.getGraphics();
        g.drawImage(Assets.menu_bg, 0, 0);
        if (majorProjectGame.isLoggedIn()){
        	g.drawImage(Assets.gpg_icon_leaderboards, 1175, 5);
            g.drawImage(Assets.gpg_icon_achievements, 1175, 105);
		}
        
        // draws sign in button if user logged out
        if (majorProjectGame.isLoggedIn()){
        	if (signInOutPressed < 0){
        		g.drawImage(Assets.sign_out_base, 7, 7);
        	} else {
        		g.drawImage(Assets.sign_out_press, 7, 7);
        	}
        			
        } else {
        	if (signInOutPressed > 0){
        		g.drawImage(Assets.sign_in_base, 7, 7);
        	} else {
        		g.drawImage(Assets.sign_in_press, 7, 7);
        	}
        }
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void backButton() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}

}
