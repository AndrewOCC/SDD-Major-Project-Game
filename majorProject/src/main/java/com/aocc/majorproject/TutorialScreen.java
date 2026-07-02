package com.aocc.majorproject;

import java.util.List;

import android.graphics.Color;
import android.graphics.Paint;

import com.aocc.framework.Graphics;
import com.aocc.framework.Screen;
import com.aocc.framework.Input.TouchEvent;
import com.aocc.majorproject.ui.UiButton;

public class TutorialScreen extends Screen {

	MajorProjectGame majorProjectGame;
	Paint paint;
	private final UiButton menuButton = new UiButton(0, 0, 200, 100, "Menu");
	
	public TutorialScreen(MajorProjectGame game) {
		super(game);
		majorProjectGame = game;
		// Define a paint object
		paint = new Paint();
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
	}
	
	@Override
	public void update(float deltaTime) {
		//creates list of all touch events
		
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
		
		int len = touchEvents.size();
		
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);	// **add error detection
				
			if (event.type == TouchEvent.TOUCH_UP) {
		
				if (menuButton.touchInBounds(event)){
		    		Assets.tap.play(MainMenuScreen.tapVol);
					game.setScreen(new MainMenuScreen(majorProjectGame));
				}
			}
		}
	}
	
	@Override
	public void paint(float deltaTime) {
		Graphics g = game.getGraphics();
		g.drawImage(Assets.tutorial, 0, 0);
		paint.setTypeface(Assets.plain);
		menuButton.paint(g, paint);

        VersionOverlay.paint(g, paint);
	}
	

	@Override
	public void backButton() {
		game.setScreen(new MainMenuScreen(majorProjectGame));
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
}
