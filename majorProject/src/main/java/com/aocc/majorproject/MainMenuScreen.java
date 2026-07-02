package com.aocc.majorproject;

import java.util.List;

import com.aocc.framework.Graphics;
import com.aocc.framework.Screen;
import com.aocc.framework.Input.TouchEvent;
import com.aocc.majorproject.input.GamepadInput;
import com.aocc.majorproject.ui.MainMenuLayout;
import com.aocc.majorproject.ui.UiBounds;
import com.aocc.majorproject.ui.UiSelectionHighlight;

public class MainMenuScreen extends Screen {

	MajorProjectGame majorProjectGame;

	int signInPressed = -1;
	private int selectedMenuIndex = 0;

	public MainMenuScreen(MajorProjectGame game) {
		super(game);
		majorProjectGame = game;
	}

	@Override
	public void update(float deltaTime) {
		handleGamepad(majorProjectGame.getGamepadInput().consumeActions());
		handleTouch(game.getInput().getTouchEvents());
	}

	private void handleTouch(List<TouchEvent> touchEvents) {
		int len = touchEvents.size();

		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);

			if (event.type == TouchEvent.TOUCH_UP) {
		    	if (MainMenuLayout.isPlay(event)) {
		    		startGame();
		        }
		    	if (MainMenuLayout.isTutorial(event)) {
		    		startTutorial();
		        }
		    	if (!majorProjectGame.isLoggedIn() && MainMenuLayout.isSignIn(event)) {
		    		playTap();
		    		signInPressed = i;
		    		majorProjectGame.onSignInButtonClicked();
		    	}
		    	if (MainMenuLayout.isLeaderboards(event)) {
		    		openLeaderboards();
		        }
		    	if (MainMenuLayout.isAchievements(event)) {
		    		openAchievements();
		        }
			} else {
				signInPressed = -1;
			}
    	}
	}

	private void handleGamepad(List<GamepadInput.Action> actions) {
		int itemCount = getMenuItemCount();
		for (GamepadInput.Action action : actions) {
			switch (action) {
				case UP:
					selectedMenuIndex = (selectedMenuIndex + itemCount - 1) % itemCount;
					break;
				case DOWN:
					selectedMenuIndex = (selectedMenuIndex + 1) % itemCount;
					break;
				case CONFIRM:
					activateMenuItem(selectedMenuIndex);
					break;
				case CANCEL:
					backButton();
					break;
				default:
					break;
			}
		}
	}

	private int getMenuItemCount() {
		return majorProjectGame.isLoggedIn() ? 4 : 5;
	}

	private void activateMenuItem(int index) {
		if (!majorProjectGame.isLoggedIn()) {
			switch (index) {
				case 0 -> startGame();
				case 1 -> startTutorial();
				case 2 -> {
					playTap();
					majorProjectGame.onSignInButtonClicked();
				}
				case 3 -> openLeaderboards();
				case 4 -> openAchievements();
				default -> { }
			}
		} else {
			switch (index) {
				case 0 -> startGame();
				case 1 -> startTutorial();
				case 2 -> openLeaderboards();
				case 3 -> openAchievements();
				default -> { }
			}
		}
	}

	private UiBounds getMenuItemBounds(int index) {
		if (!majorProjectGame.isLoggedIn()) {
			return switch (index) {
				case 0 -> MainMenuLayout.playButton();
				case 1 -> MainMenuLayout.tutorialButton();
				case 2 -> MainMenuLayout.signInButton();
				case 3 -> MainMenuLayout.leaderboardsButton();
				case 4 -> MainMenuLayout.achievementsButton();
				default -> null;
			};
		}
		return switch (index) {
			case 0 -> MainMenuLayout.playButton();
			case 1 -> MainMenuLayout.tutorialButton();
			case 2 -> MainMenuLayout.leaderboardsButton();
			case 3 -> MainMenuLayout.achievementsButton();
			default -> null;
		};
	}

	private void startGame() {
		playTap();
		game.setScreen(new GameScreen(majorProjectGame));
	}

	private void startTutorial() {
		playTap();
		game.setScreen(new TutorialScreen(majorProjectGame));
	}

	private void openLeaderboards() {
		playTap();
		majorProjectGame.onShowLeaderboardsRequested("");
	}

	private void openAchievements() {
		playTap();
		majorProjectGame.onShowAchievementsRequested("");
	}

	private void playTap() {
		Assets.tap.play(GamePreferences.getTapVolume());
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

		paintSelectionHighlight(g);
        VersionOverlay.paint(g);
	}

	private void paintSelectionHighlight(Graphics g) {
		UiBounds bounds = MainMenuLayout.highlightForIndex(
				selectedMenuIndex, !majorProjectGame.isLoggedIn());
		if (bounds == null) {
			return;
		}
		UiSelectionHighlight.paintRect(g, bounds);
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
