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
import com.aocc.framework.implementation.AndroidGame;
import com.aocc.majorproject.ui.ComboMeter;
import com.aocc.majorproject.ui.ComposeOverlayBridge;
import com.aocc.majorproject.ui.ScoreBar;
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

    UiButton retryButton;
    private final ComposeOverlayBridge overlay;

    private final ScoreBar scoreBar = new ScoreBar();
    private final ComboMeter comboMeter = new ComboMeter();
    private final UiBanner gameOverBanner = new UiBanner(100f);
    private final UiBanner scoreBanner = new UiBanner(60f);

	private float facingAngle = 0;

	public GameScreen(MajorProjectGame game) {
		super(game);
		
		majorProjectGame = game;
		overlay = ((AndroidGame) game).getComposeOverlay();
		
		// Initialise game objects
		player = session.getPlayer();
		c = new EnemyController(session);
		r = new Random();
		p = new PowerUp(1, session);
		tempEnemyPoint = new Point();

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
		// Ready/pause settings and tap-to-start are handled by the Compose overlay.
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
			hideOverlay();
		}	
		
	}


	private void updatePaused(List<TouchEvent> touchEvents) {
		// Pause settings and tap-to-resume are handled by the Compose overlay.
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
		    	if (PersonalMethods.touchInBounds(event, 0, 0, UiButton.MENU_WIDTH, UiButton.MENU_HEIGHT)) {
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
	}


	private void drawGameOverUI() {
		Graphics g = game.getGraphics();
		
		g.drawARGB(155, 0, 0, 0);

        retryButton.paint(g);
		g.drawImage(Assets.gpg_icon_leaderboards, 1175, 5);
		
		paint.setTypeface(Assets.plain);
		gameOverBanner.paint(g, paint, "Game Over!", GameConstants.WORLD_WIDTH / 2, 200);
		scoreBanner.paint(g, paint, "Score: " + session.getScore(), GameConstants.WORLD_WIDTH / 2, 400);

		UiButton menuButton = UiButton.menuAt(0, 0);
		menuButton.paint(g);
	}


	//OTHER METHODS
	@Override
	public void pause() {
		if (state == GameState.Running){
            state = GameState.Paused;
            if (MainMenuScreen.music == true){
            	Assets.setMusicVolume(0.25f); 
            }
            showSettingsOverlay("Press anywhere to resume", true);
		}
	}

	@Override
	public void resume() {
        if (MainMenuScreen.music == true) {
            Assets.playMusic();
            Assets.setMusicVolume(0.25f);
        }
        syncOverlay();
	}

	@Override
	public void dispose() {
		hideOverlay();
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
			resumeRunningFromOverlay();
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

	private void syncOverlay() {
		if (state == GameState.Ready) {
			showSettingsOverlay("Press anywhere to start", false);
		} else if (state == GameState.Paused) {
			showSettingsOverlay("Press anywhere to resume", true);
		} else {
			hideOverlay();
		}
	}

	private void showSettingsOverlay(String prompt, boolean showMenuButton) {
		overlay.showSettings(prompt, showMenuButton, settingsListener);
	}

	private void hideOverlay() {
		overlay.hide();
	}

	private void resumeRunningFromOverlay() {
		Assets.tap.play(MainMenuScreen.tapVol);
		state = GameState.Running;
		hideOverlay();
		if (MainMenuScreen.music) {
			Assets.setMusicVolume(0.85f);
		}
	}

	private final ComposeOverlayBridge.SettingsListener settingsListener =
			new ComposeOverlayBridge.SettingsListener() {
				@Override
				public void onResumeGame() {
					if (state == GameState.Ready || state == GameState.Paused) {
						resumeRunningFromOverlay();
					}
				}

				@Override
				public void onMenu() {
					if (state == GameState.Paused) {
						Assets.tap.play(MainMenuScreen.tapVol);
						reset();
						goToMenu();
					}
				}

				@Override
				public void onToggleSound() {
					GameSettings.toggleSound();
					overlay.refreshSettings();
				}

				@Override
				public void onToggleMusic() {
					GameSettings.toggleMusic();
					overlay.refreshSettings();
				}

				@Override
				public void onFlatTilt() {
					GameSettings.applyFlatTilt(player);
					overlay.refreshSettings();
				}

				@Override
				public void onTiltedTilt() {
					GameSettings.applyTiltedTilt(player);
					overlay.refreshSettings();
				}

				@Override
				public void onCustomTilt() {
					GameSettings.applyCustomTilt(player);
					overlay.refreshSettings();
				}

				@Override
				public boolean isSoundOn() {
					return MainMenuScreen.sound;
				}

				@Override
				public boolean isMusicOn() {
					return MainMenuScreen.music;
				}

				@Override
				public int getTiltMode() {
					return player.getTiltMode();
				}
			};
	
}
