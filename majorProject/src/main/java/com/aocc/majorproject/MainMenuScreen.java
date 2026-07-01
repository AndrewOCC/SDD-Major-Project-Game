package com.aocc.majorproject;

import java.util.List;

import android.graphics.Paint;

import com.aocc.framework.Graphics;
import com.aocc.framework.PersonalMethods;
import com.aocc.framework.Screen;
import com.aocc.framework.Input.TouchEvent;

public class MainMenuScreen extends Screen {

	MajorProjectGame majorProjectGame;
	private final MusicPlayerPill musicPlayerPill = new MusicPlayerPill();
	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

	int signInPressed = -1;
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
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
		int len = touchEvents.size();
		
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
				
			if (event.type == TouchEvent.TOUCH_UP) {
				if (musicPlayerPill.handleTouch(event, majorProjectGame)) {
					continue;
				}

		    	if (PersonalMethods.touchInBounds(event, 140, 435, 440, 200)) {
		    		Assets.tap.play(tapVol);
		        	game.setScreen(new GameScreen(majorProjectGame));
		        }
		    	
		    	if (PersonalMethods.touchInBounds(event, 700, 435, 440, 200)) {
		    		Assets.tap.play(tapVol);
		        	game.setScreen(new TutorialScreen(majorProjectGame));
		        }
		    	
		    	if (majorProjectGame.shouldShowPlayGamesSignInButton()
		    			&& PersonalMethods.touchInBounds(event, 7, 7, 180, 60)) {
		    		Assets.tap.play(tapVol);
		    		signInPressed = i;
		    		majorProjectGame.onSignInButtonClicked();
		    	}
		    	
		    	if (PersonalMethods.touchInBounds(event, 1175, 5, 100, 80)) {
		    		Assets.tap.play(tapVol);
		    		majorProjectGame.onShowLeaderboardsRequested("");
		        }
		    	
		    	if (PersonalMethods.touchInBounds(event, 1175, 105, 100, 80)) {
		    		Assets.tap.play(tapVol);
		    		majorProjectGame.onShowAchievementsRequested("");
		        }
		    	
			} else {
				signInPressed = -1;
			}
    	}
	}

	@Override
	public void paint(float deltaTime) {
		Graphics g = game.getGraphics();
        g.drawImage(Assets.menu_bg, 0, 0);

        g.drawImage(Assets.gpg_icon_leaderboards, 1175, 5);
        g.drawImage(Assets.gpg_icon_achievements, 1175, 105);

        if (majorProjectGame.shouldShowPlayGamesSignInButton()) {
        	if (signInPressed >= 0) {
        		g.drawImage(Assets.sign_in_press, 7, 7);
        	} else {
        		g.drawImage(Assets.sign_in_base, 7, 7);
        	}
        }

        paint.setTypeface(Assets.plain);
        musicPlayerPill.paint(g, paint, majorProjectGame);
        VersionOverlay.paint(g, paint);
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
		android.os.Process.killProcess(android.os.Process.myPid());
	}

}
