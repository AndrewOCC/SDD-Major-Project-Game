package com.aocc.majorproject;

import java.util.List;

import android.graphics.Color;
import android.graphics.Paint;

import com.aocc.framework.Graphics;
import com.aocc.framework.Screen;
import com.aocc.framework.Input.TouchEvent;
import com.aocc.majorproject.input.GamepadInput;
import com.aocc.majorproject.ui.UiButton;

public class TutorialScreen extends Screen {

	MajorProjectGame majorProjectGame;
	private final UiButton menuButton = UiButton.menuAt(0, 0);

	/** Legacy tutorial art includes a 200x100 menu button graphic beneath our control. */
	private static final int LEGACY_MENU_ART_HEIGHT = 100;
	
	public TutorialScreen(MajorProjectGame game) {
		super(game);
		majorProjectGame = game;
	}
	
	@Override
	public void update(float deltaTime) {
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
		int len = touchEvents.size();
		
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
				
			if (event.type == TouchEvent.TOUCH_UP) {
				if (menuButton.touchInBounds(event)){
		    		goToMenu();
				}
			}
		}

		for (GamepadInput.Action action : majorProjectGame.getGamepadInput().consumeActions()) {
			if (action == GamepadInput.Action.CONFIRM || action == GamepadInput.Action.CANCEL) {
				goToMenu();
			}
		}
	}

	private void goToMenu() {
		Assets.tap.play(GamePreferences.getTapVolume());
		game.setScreen(new MainMenuScreen(majorProjectGame));
	}
	
	@Override
	public void paint(float deltaTime) {
		Graphics g = game.getGraphics();
		g.drawImage(Assets.tutorial, 0, 0);
		// Cover baked-in menu art so only the standard HUD button is visible.
		g.drawRect(0, 0, UiButton.MENU_WIDTH, LEGACY_MENU_ART_HEIGHT, Color.BLACK);
		menuButton.paint(g);
        VersionOverlay.paint(g);
	}

	@Override
	public void backButton() {
		game.setScreen(new MainMenuScreen(majorProjectGame));
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
}
