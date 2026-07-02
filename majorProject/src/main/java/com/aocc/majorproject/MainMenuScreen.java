package com.aocc.majorproject;

import java.util.List;

import com.aocc.framework.Graphics;
import com.aocc.framework.Screen;
import com.aocc.framework.Input.TouchEvent;
import com.aocc.majorproject.ui.MainMenuLayout;

public class MainMenuScreen extends Screen {

	MajorProjectGame majorProjectGame;

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
		    	if (MainMenuLayout.isPlay(event)) {
		    		Assets.tap.play(tapVol);
		        	game.setScreen(new GameScreen(majorProjectGame));
		        }
		    	if (MainMenuLayout.isTutorial(event)) {
		    		Assets.tap.play(tapVol);
		        	game.setScreen(new TutorialScreen(majorProjectGame));
		        }
		    	if (!majorProjectGame.isLoggedIn() && MainMenuLayout.isSignIn(event)) {
		    		Assets.tap.play(tapVol);
		    		signInPressed = i;
		    		majorProjectGame.onSignInButtonClicked();
		    	}
		    	if (MainMenuLayout.isLeaderboards(event)) {
		    		Assets.tap.play(tapVol);
		    		majorProjectGame.onShowLeaderboardsRequested("");
		        }
		    	if (MainMenuLayout.isAchievements(event)) {
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

        g.drawImage(Assets.gpg_icon_leaderboards,
                MainMenuLayout.leaderboardsDrawX(), MainMenuLayout.leaderboardsDrawY());
        g.drawImage(Assets.gpg_icon_achievements,
                MainMenuLayout.achievementsDrawX(), MainMenuLayout.achievementsDrawY());

        if (!majorProjectGame.isLoggedIn()) {
        	if (signInPressed >= 0) {
        		g.drawImage(Assets.sign_in_press,
                        MainMenuLayout.signInDrawX(), MainMenuLayout.signInDrawY());
        	} else {
        		g.drawImage(Assets.sign_in_base,
                        MainMenuLayout.signInDrawX(), MainMenuLayout.signInDrawY());
        	}
        }

        VersionOverlay.paint(g);
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
