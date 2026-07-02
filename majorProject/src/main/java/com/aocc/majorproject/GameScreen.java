package com.aocc.majorproject;

import java.util.Random;

import java.util.List;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.aocc.framework.GameConstants;
import com.aocc.framework.Graphics;
import com.aocc.framework.PersonalMethods;
import com.aocc.framework.Screen;
import com.aocc.framework.Input.TouchEvent;
import com.aocc.majorproject.ui.ComboMeter;
import com.aocc.majorproject.ui.ScoreBar;
import com.aocc.majorproject.ui.SettingsPanel;
import com.aocc.majorproject.ui.UiBanner;
import com.aocc.majorproject.ui.UiButton;

public class GameScreen extends Screen {
	// Defining GameState Enum
	enum GameState {Ready, Running, Paused, GameOver}
	GameState state = GameState.Ready;	// represents gamestate as enum
	
	// for non-static references within code, this holds the MajorProjectGame object
	MajorProjectGame majorProjectGame;
	
	// Variable Setup
	Paint paint;
	private final GameSession session = new GameSession();
	private Player player;
	EnemyController c;
	Random r;				// object for random number generation
	PowerUp p;				// DEBUG powerup object (will most likely be replaced by controller)
	Point tempEnemyPoint;	// holds x/y values for new enemies before creation

    final int ARENA_HEIGHT = 3;

    UiButton menuButton;
    UiButton retryButton;
    private final SettingsPanel settingsPanel = new SettingsPanel();

    private final ScoreBar scoreBar = new ScoreBar();
    private final ComboMeter comboMeter = new ComboMeter();
    private final UiBanner promptBanner = new UiBanner(50f);
    private final UiBanner gameOverBanner = new UiBanner(100f);
    private final UiBanner scoreBanner = new UiBanner(60f);

	private float facingAngle = 0;

	public GameScreen(MajorProjectGame game) {
		super(game);
		
		majorProjectGame = game;
		
		// Initialise game objects
		player = session.getPlayer();
		c = new EnemyController(session);
		r = new Random();
		p = new PowerUp(1, session);
		tempEnemyPoint = new Point();

        // buttons
        menuButton = UiButton.menuAt(0, 0);
        retryButton = new UiButton(540, 500, UiButton.MENU_WIDTH, UiButton.MENU_HEIGHT, "Retry");
		
		// Define a paint object
		paint = new Paint();
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        
	}

	// this update method splits into four classes, each for a different gamestate.
	@Override
	public void update(float deltaTime) {
		//Log.d("MajorProject", "tapVol: " + MainMenuScreen.tapVol);
		
		//gathers touch events to pass into the subclasses themselves
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();

        if (state == GameState.Ready)
            updateReady(touchEvents);
        if (state == GameState.Running)
            updateRunning(touchEvents, deltaTime);
        if (state == GameState.Paused)
            updatePaused(touchEvents);
        if (state == GameState.GameOver)
            updateGameOver(touchEvents);

	}

	private void updateReady(List<TouchEvent> touchEvents) {
		int len = touchEvents.size();
		
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
		    if (event.type == TouchEvent.TOUCH_UP){
		    	if (settingsPanel.handleTouch(event, player)) {
		    		continue;
		    	}
		    	Assets.tap.play(MainMenuScreen.tapVol);
		    	state = GameState.Running;
	    	}
		}
	}
	
	private void updateRunning(List<TouchEvent> touchEvents, float deltaSeconds) {
		float step = GameConstants.secondsToSteps(deltaSeconds);

		// User Input
		
		// update count: allows events to occur every few updates an on-update basis
		session.addUpdateCount(step);
		session.addEnemyCounter(step);
		
		// Calls individual update methods for each object
		player.update(deltaSeconds);
		c.update(deltaSeconds);
		p.update(deltaSeconds);
		
		// Check important events
		
		// UPDATING SPEED
		if (session.getSpeed() < 25 && session.getUpdateCount() >= GameConstants.SPEED_RAMP_INTERVAL_FRAMES) {
			session.incrementSpeed();
			session.subtractUpdateCount(GameConstants.SPEED_RAMP_INTERVAL_FRAMES);
			c.increaseEnemyTopSpeed();
		}
		
		
		// ENEMIES
		if (session.getEnemyCounter() > c.getNextEnemySpawn()){
			
			tempEnemyPoint.x = r.nextInt(GameConstants.WORLD_WIDTH - 100) + 50;
			tempEnemyPoint.y = r.nextInt(GameConstants.WORLD_HEIGHT - 100) + 50;
			
			PersonalMethods.limitOutside(tempEnemyPoint, (int)player.getCenterX(), (int)player.getCenterY(), 100);
			
			
			if (r.nextInt(10) == 9){
				c.addEnemy(tempEnemyPoint.x, tempEnemyPoint.y, 2);
			} else {
				c.addEnemy(tempEnemyPoint.x, tempEnemyPoint.y, 1);
			}
			session.resetEnemyCounter();
		}
		// CHECKING FOR ACHIEVEMENTS

		if (player.getCombo() == 200){
			majorProjectGame.onAchievementUnlocked(
					majorProjectGame.getString(R.string.achievement_ccccombo));
		}


		// Game Logic
		if (session.isGameOverFlag()) {
			state = GameState.GameOver;
		}	
		
	}


	private void updatePaused(List<TouchEvent> touchEvents) {
		int len = touchEvents.size();
		
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
		    if (event.type == TouchEvent.TOUCH_UP) {
		    	if (menuButton.touchInBounds(event)) {
		    		Assets.tap.play(MainMenuScreen.tapVol);
					reset();
					goToMenu();
					return;
		    	}
		    	if (settingsPanel.handleTouch(event, player)) {
		    		continue;
		    	}
                Assets.tap.play(MainMenuScreen.tapVol);
                state = GameState.Running;
                if (MainMenuScreen.music == true){
                    Assets.setMusicVolume(0.85f);
                }
	    	}
		}
	}


	private void updateGameOver(List<TouchEvent> touchEvents) {


		if (!session.isScoreUploaded()){
			majorProjectGame.onEnteredScore(session.getScore());
			session.setScoreUploaded(true);
		}


		int len = touchEvents.size();
		
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
		    if (event.type == TouchEvent.TOUCH_UP) {
		    	
		    	//Menu button
		    	if (menuButton.touchInBounds(event)) {
		    		Assets.tap.play(MainMenuScreen.tapVol);
					// Game over events
					reset();
					goToMenu();
					return;
		    	}

                //Retry button
                if (retryButton.touchInBounds(event)) {
                    Assets.tap.play(MainMenuScreen.tapVol);
                    reset();
                    restart();
                }

		    	//High Scores Button
		    	if (PersonalMethods.touchInBounds(event, 1175, 5, 100, 80)) {
		    		Assets.tap.play(MainMenuScreen.tapVol);
		    		majorProjectGame.onShowLeaderboardsRequested(
		    				majorProjectGame.getString(R.string.leaderboard_pacifist_mode));
		    	}
		    }
		}
	}

	
	//DRAWING UI

	@Override
	public void paint(float deltaTime) {
		Graphics g = game.getGraphics();
		
		g.drawImage(Assets.game_bg, 0, 0);
		
		if (state == GameState.Ready)
            drawReadyUI();
        if (state == GameState.Running)
            drawRunningUI();
        if (state == GameState.Paused)
            drawPausedUI();
        if (state == GameState.GameOver)
            drawGameOverUI();

        VersionOverlay.paint(g);
	}

	private void drawReadyUI() {
		Graphics g = game.getGraphics();
		
		g.drawARGB(155, 0, 0, 0);
		settingsPanel.paint(g, paint, player);

        paint.setTypeface(Assets.plain);
        promptBanner.paint(g, paint, "Press anywhere to start",
                GameConstants.WORLD_WIDTH / 2, 300);
	}


	private void drawRunningUI() {
		Graphics g = game.getGraphics();
		
		//powerups
		p.paint(g, paint);
		
		//enemies
		c.paint(g, paint);
		
		//main character
		player.paint(g, paint);
		
		paint.setTypeface(Assets.plain);
		comboMeter.paint(g, paint, player.getCombo());
		scoreBar.paint(g, paint, session.getScore());
	}


	private void drawPausedUI() {
		Graphics g = game.getGraphics();
        
		// clears screen
		g.drawRect(0, 0, 1281, 721, Color.BLACK);
		
		paint.setTextSize(40);
		//draws gameplay on-screen
		p.paint(g, paint);
		c.paint(g, paint);
		player.paint(g, paint);
		
		// alpha bg
		g.drawARGB(155, 0, 0, 0);
		settingsPanel.paint(g, paint, player);

        menuButton.paint(g);


        paint.setTypeface(Assets.plain);
		promptBanner.paint(g, paint, "Press anywhere to resume",
                GameConstants.WORLD_WIDTH / 2, 300);
	}


	private void drawGameOverUI() {
		Graphics g = game.getGraphics();
		
		g.drawARGB(155, 0, 0, 0);

        menuButton.paint(g);
        retryButton.paint(g);
		g.drawImage(Assets.gpg_icon_leaderboards, 1175, 5);
		
		paint.setTypeface(Assets.plain);
		gameOverBanner.paint(g, paint, "Game Over!", GameConstants.WORLD_WIDTH / 2, 200);
		scoreBanner.paint(g, paint, "Score: " + session.getScore(), GameConstants.WORLD_WIDTH / 2, 400);
	}

	//OTHER METHODS
	@Override
	public void pause() {
		if (state == GameState.Running){
            state = GameState.Paused;
            if (MainMenuScreen.music == true){
            	Assets.setMusicVolume(0.25f); 
            }
		}
	}

	@Override
	public void resume() {
        if (MainMenuScreen.music == true) {
            Assets.playMusic();
            Assets.setMusicVolume(0.25f);
        }
	}

	@Override
	public void dispose() {
	}
	
	public void reset() {
		paint = null;
		player = null;
		c = null;
		r = null;
		p = null;
		tempEnemyPoint = null;
		state = null;
		facingAngle = 0;
		session.resetForNewRun();
		System.gc();
	}

    public void restart() {
        game.setScreen(new GameScreen(majorProjectGame));
    }

	private void goToMenu() {
		game.setScreen(new MainMenuScreen(majorProjectGame));
		if (MainMenuScreen.music == true){
        	Assets.setMusicVolume(0.85f); 
        }
	}

	@Override
	public void backButton() {
		if (state == GameState.Running) {
			pause();
		} else if (state == GameState.Paused) {
			resume();
		} else if (state == GameState.GameOver) {
			// Game over events
			reset();
			goToMenu();
			return;
		} else {
			goToMenu();
		}
	}

	public boolean isGameOverFlag() {
		return session.isGameOverFlag();
	}


	public float getFacingAngle() {
		return facingAngle;
	}


	public void setFacingAngle(float facingAngle) {
		this.facingAngle = facingAngle;
	}
	
}
