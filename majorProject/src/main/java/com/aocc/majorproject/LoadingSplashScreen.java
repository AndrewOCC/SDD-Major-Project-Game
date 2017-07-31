package com.aocc.majorproject;

import com.aocc.framework.Graphics;
import com.aocc.framework.Screen;
import com.aocc.framework.Graphics.ImageFormat;

public class LoadingSplashScreen extends Screen {

	MajorProjectGame majorProjectGame;
	
	public LoadingSplashScreen(MajorProjectGame game) {
		super(game);
		majorProjectGame = game;
	}

	@Override
	public void update(float deltaTime) {
		Graphics g = game.getGraphics();
        
		// loads the splash screen image
        Assets.splash = g.newImage("splash.png", ImageFormat.RGB565);

        // displays the actual splash screen
        game.setScreen(new LoadingScreen(majorProjectGame));
        
        
	}

	@Override
	public void paint(float deltaTime) {
		// TODO Auto-generated method stub

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

	@Override
	public void backButton() {
		// TODO Auto-generated method stub

	}

}
