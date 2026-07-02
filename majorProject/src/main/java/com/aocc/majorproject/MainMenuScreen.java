package com.aocc.majorproject;

import com.aocc.framework.Graphics;
import com.aocc.framework.Screen;
import com.aocc.framework.implementation.AndroidGame;
import com.aocc.majorproject.ui.ComposeOverlayBridge;

public class MainMenuScreen extends Screen {

	MajorProjectGame majorProjectGame;
	private final ComposeOverlayBridge overlay;

	int signInPressed = -1;
	public static int tapVol = 10;

    public static boolean music;
    public static boolean sound = true;


	public MainMenuScreen(MajorProjectGame game) {
		super(game);
		majorProjectGame = game;
		overlay = ((AndroidGame) game).getComposeOverlay();

        if (majorProjectGame.isMusicActive()) {
            music = false;
        } else {
            music = true;
        }

	}
    
	@Override
	public void update(float deltaTime) {
		// Main menu navigation is handled by the Compose overlay.
	}

	@Override
	public void paint(float deltaTime) {
		Graphics g = game.getGraphics();
        g.drawImage(Assets.menu_bg, 0, 0);
        VersionOverlay.paint(g);
	}

	@Override
	public void pause() {
		overlay.hide();
	}

	@Override
	public void resume() {
		overlay.showMainMenu(mainMenuListener);
	}

	@Override
	public void dispose() {
		overlay.hide();
	}

	@Override
	public void backButton() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	private final ComposeOverlayBridge.MainMenuListener mainMenuListener =
			new ComposeOverlayBridge.MainMenuListener() {
				@Override
				public void onPlay() {
					Assets.tap.play(tapVol);
					game.setScreen(new GameScreen(majorProjectGame));
				}

				@Override
				public void onTutorial() {
					Assets.tap.play(tapVol);
					game.setScreen(new TutorialScreen(majorProjectGame));
				}

				@Override
				public void onSignIn() {
					Assets.tap.play(tapVol);
					majorProjectGame.onSignInButtonClicked();
				}

				@Override
				public void onLeaderboards() {
					Assets.tap.play(tapVol);
					majorProjectGame.onShowLeaderboardsRequested("");
				}

				@Override
				public void onAchievements() {
					Assets.tap.play(tapVol);
					majorProjectGame.onShowAchievementsRequested("");
				}

				@Override
				public boolean isLoggedIn() {
					return majorProjectGame.isLoggedIn();
				}
			};

}
