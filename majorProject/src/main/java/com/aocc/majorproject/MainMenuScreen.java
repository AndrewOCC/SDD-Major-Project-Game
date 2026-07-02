package com.aocc.majorproject;

import java.util.ArrayList;
import java.util.List;

import com.aocc.framework.Graphics;
import com.aocc.framework.Screen;
import com.aocc.framework.Input.TouchEvent;
import com.aocc.majorproject.input.GamepadInput;
import com.aocc.majorproject.ui.MainMenuLayout;
import com.aocc.majorproject.ui.SpatialFocusNavigator;
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
		List<UiBounds> focusItems = buildMenuFocusBounds();
		for (GamepadInput.Action action : actions) {
			SpatialFocusNavigator.Direction direction = SpatialFocusNavigator.directionFrom(action);
			if (direction != null) {
				selectedMenuIndex = SpatialFocusNavigator.findNext(
						selectedMenuIndex, direction, focusItems);
				continue;
			}
			if (action == GamepadInput.Action.CONFIRM) {
				activateMenuItem(selectedMenuIndex);
				continue;
			}
			if (action == GamepadInput.Action.CANCEL) {
				backButton();
			}
		}
	}

	private List<UiBounds> buildMenuFocusBounds() {
		List<UiBounds> items = new ArrayList<>(getMenuItemCount());
		boolean loggedIn = majorProjectGame.isLoggedIn();
		for (int i = 0; i < getMenuItemCount(); i++) {
			items.add(MainMenuLayout.highlightForIndex(i, !loggedIn));
		}
		return items;
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
