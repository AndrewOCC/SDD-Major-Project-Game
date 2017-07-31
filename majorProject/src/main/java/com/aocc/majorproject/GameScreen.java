package com.aocc.majorproject;

import java.util.LinkedList;
import java.util.Random;

import java.util.List;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.aocc.framework.Graphics;
import com.aocc.framework.PersonalMethods;
import com.aocc.framework.Screen;
import com.aocc.framework.Input.TouchEvent;
import com.aocc.framework.implementation.RotationHandler;

public class GameScreen extends Screen {
	// Defining GameState Enum
	enum GameState {Ready, Running, Paused, GameOver}
	GameState state = GameState.Ready;	// represents gamestate as enum
	
	// for non-static references within code, this holds the MajorProjectGame object
	MajorProjectGame majorProjectGame;
	
	// Variable Setup
	Paint paint;
	static Player player;
	EnemyController c;		// controller object for enemies
	Random r;				// object for random number generation
	PowerUp p;				// DEBUG powerup object (will most likely be replaced by controller)
	Point tempEnemyPoint;	// holds x/y values for new enemies before creation

	final int SOUND_X = 50;
	final int SOUND_Y = 200;
	final int SOUND_WIDTH = 100;

	final int TILT_MENU_X = 1000;
	final int TILT_MENU_Y = 200;

    final int ARENA_HEIGHT = 3;

    Button menuButton;
    Button retryButton;
    Button flatTiltButton;
    Button tiltedTiltButton;
    Button customTiltButton;

	
	private float facingAngle = 0;
	private static int updateCount = 0;
	private static int enemyCounter = 300;
	private static int score = 0;
	private static boolean scoreUploaded = false;

	private static int speed = 0;

	private static boolean gameOverFlag = false;
	
	static LinkedList<Enemy> e = new LinkedList<Enemy>();
	
	public GameScreen(MajorProjectGame game) {		//starts up the game
		super(game);
		
		majorProjectGame = game;
		
		// Initialise game objects
		player = new Player();
		c = new EnemyController();
		r = new Random();
		p = new PowerUp(1);
		tempEnemyPoint = new Point();

        // buttons
        menuButton = new Button(0,0,4,0,"Menu");
        retryButton = new Button(540, 500,4,0,"Retry");
        flatTiltButton = new Button(TILT_MENU_X, TILT_MENU_Y, 3, 0, "Flat");
        tiltedTiltButton = new Button(TILT_MENU_X, TILT_MENU_Y+150, 3, 0, "Tilted");
        customTiltButton = new Button(TILT_MENU_X, TILT_MENU_Y+300, 3, 0, "Custom");



		
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
		// Any touch event starts the game
		int len = touchEvents.size();
		
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
		    if (event.type == TouchEvent.TOUCH_UP){
		    	
		    	if (flatTiltButton.touchInBounds(event)){
		    		Assets.tap.play(MainMenuScreen.tapVol);
		    		player.setxBias(0);
		    		player.setyBias(0);
		    		player.setTiltMode(1);

		    	} else if (tiltedTiltButton.touchInBounds(event)){
		    		Assets.tap.play(MainMenuScreen.tapVol);
		    		player.setxBias(0);
	    			player.setyBias(-0.30f);
		    		player.setTiltMode(2);

			    } else if (customTiltButton.touchInBounds(event)){
			    	Assets.tap.play(MainMenuScreen.tapVol);
		    		player.setxBias(-PersonalMethods.limitInside(RotationHandler.getRotationX(),-90,90)/90);
		    		player.setyBias(-PersonalMethods.limitInside(RotationHandler.getRotationY(),-90,90)/90);
		    		player.setTiltMode(3);

			    } else if (PersonalMethods.touchInBounds(event, TILT_MENU_X - 40, TILT_MENU_Y - 20,
			    		250, 360 + flatTiltButton.getWidth())){
			    	
			    } else if (PersonalMethods.touchInBounds(event, SOUND_X, SOUND_Y, SOUND_WIDTH, SOUND_WIDTH)){
			    	if (MainMenuScreen.sound == false){
			    		MainMenuScreen.sound = true;
			    		MainMenuScreen.tapVol = 10;
			    	} else if (MainMenuScreen.sound == true) {
			    		MainMenuScreen.sound = false;
			    		MainMenuScreen.tapVol = 0;
			    	}
			    } else if (PersonalMethods.touchInBounds(event, SOUND_X, SOUND_Y + 200, SOUND_WIDTH, SOUND_WIDTH)){
			    	if (MainMenuScreen.music == false){
			    		MainMenuScreen.music = true;
			    		Assets.darude.setVolume(0.25f);
			    	} else if (MainMenuScreen.music == true) {
			    		MainMenuScreen.music = false;
			    		Assets.darude.setVolume(0);
			    	}
			    } else {
			    	Assets.tap.play(MainMenuScreen.tapVol);
			    	state = GameState.Running;
		        }
			    	
	    	}
		}

	}
	
	private void updateRunning(List<TouchEvent> touchEvents, float deltaTime) {
		// User Input
		
		// update count: allows events to occur every few updates an on-update basis
		updateCount++;
		enemyCounter++;
		
		// Calls individual update methods for each object
		player.update();
		c.update();
		p.update();
		
		// Check important events
		
		// UPDATING SPEED
		if (speed < 25 && updateCount % 500 == 0) {
			speed ++;
		}
		
		
		// ENEMIES
		if (enemyCounter > EnemyController.nextEnemySpawn){ // determines if enough time has passed for a new enemy
			
			tempEnemyPoint.x = r.nextInt(1180) + 50;
			tempEnemyPoint.y = r.nextInt(620) + 50;
			
			PersonalMethods.limitOutside(tempEnemyPoint, (int)player.getCenterX(), (int)player.getCenterY(), 100);
			
			
			if (r.nextInt(10) == 9){
				c.addEnemy(tempEnemyPoint.x, tempEnemyPoint.y, 2);
			} else {
				c.addEnemy(tempEnemyPoint.x, tempEnemyPoint.y, 1);
			}
			enemyCounter = 0;
		}
		// CHECKING FOR ACHIEVEMENTS

		if (player.getCombo() == 200){
			majorProjectGame.onAchievementUnlocked(
					majorProjectGame.getString(R.string.achievement_ccccombo));
		}


		// Game Logic
		if (gameOverFlag == true) {
			state = GameState.GameOver;
		}	
		
	}


	private void updatePaused(List<TouchEvent> touchEvents) {
		// Paused events
		// Restarts after any user input
		//creates list of all touch events
		int len = touchEvents.size();
		
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
		    if (event.type == TouchEvent.TOUCH_UP) {

                // 'Menu' Button
		    	if (PersonalMethods.touchInBounds(event, 0, 0, 200, 100)){
		    		Assets.tap.play(MainMenuScreen.tapVol);
					reset();
					goToMenu();
					return;

                // Tilt Settings
		    	} else if (flatTiltButton.touchInBounds(event)){
		    		Assets.tap.play(MainMenuScreen.tapVol);
		    		player.setxBias(0);
		    		player.setyBias(0);
		    		player.setTiltMode(1);
		    	} else if (tiltedTiltButton.touchInBounds(event)){
		    		Assets.tap.play(MainMenuScreen.tapVol);
		    		player.setxBias(0);
	    			player.setyBias(-0.20f);
		    		player.setTiltMode(2);
			    } else if (customTiltButton.touchInBounds(event)){
			    	Assets.tap.play(MainMenuScreen.tapVol);
		    		player.setxBias(-PersonalMethods.limitInside(RotationHandler.getRotationX(),-90,90)/90);
		    		player.setyBias(-PersonalMethods.limitInside(RotationHandler.getRotationY(),-90,90)/90);
		    		player.setTiltMode(3);
			    } else if (PersonalMethods.touchInBounds(event, TILT_MENU_X - 40, TILT_MENU_Y - 20,
			    		250, 360 + tiltedTiltButton.getWidth())){

                // Sound Settings
			    } else if (PersonalMethods.touchInBounds(event, SOUND_X, SOUND_Y, SOUND_WIDTH, SOUND_WIDTH)){
			    	// Sound Effects button
                    if (MainMenuScreen.sound == false){
			    		MainMenuScreen.sound = true;
			    		MainMenuScreen.tapVol = 10;
			    	} else if (MainMenuScreen.sound == true) {
			    		MainMenuScreen.sound = false;
			    		MainMenuScreen.tapVol = 0;
			    	}
			    } else if (PersonalMethods.touchInBounds(event, SOUND_X, SOUND_Y + 200, SOUND_WIDTH, SOUND_WIDTH)){
			    	// Music button
                    if (MainMenuScreen.music == false){
			    		MainMenuScreen.music = true;
			    		Assets.darude.setVolume(0.25f);
			    	} else if (MainMenuScreen.music == true) {
			    		MainMenuScreen.music = false;
			    		Assets.darude.setVolume(0);
			    	}
			    }  else {
                    // Tap anywhere to resume game
                    Assets.tap.play(MainMenuScreen.tapVol);
                    state = GameState.Running;
                    if (MainMenuScreen.music == true){
                        Assets.darude.setVolume(0.85f);
                    }
		        }
			    	
	    	}
		}
		
	}


	private void updateGameOver(List<TouchEvent> touchEvents) {


		if (scoreUploaded = false){
			majorProjectGame.onEnteredScore(score);
			scoreUploaded = true;
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

	}

	private void drawReadyUI() {
		Graphics g = game.getGraphics();
		
		g.drawARGB(155, 0, 0, 0);
        
		// Tilt settings rectangle
		g.drawRect(TILT_MENU_X - 40, TILT_MENU_Y - 20, 275, tiltedTiltButton.getWidth(), Color.DKGRAY);
        g.drawString("Tilt Options", TILT_MENU_X + 85, TILT_MENU_Y - 50, Color.WHITE, paint);


        // sound control buttons
		if(MainMenuScreen.sound == true){
			g.drawImage(Assets.sound, SOUND_X, SOUND_Y);
		} else {
			g.drawImage(Assets.sound_muted, SOUND_X, SOUND_Y);
		}
		
		if(MainMenuScreen.music == true){
			g.drawImage(Assets.music, SOUND_X, SOUND_Y + 200);
		} else {
			g.drawImage(Assets.music_muted, SOUND_X, SOUND_Y + 200);
		}

        // tilt setting buttons
        flatTiltButton.paint(g,paint, player);
        tiltedTiltButton.paint(g, paint, player);
        customTiltButton.paint(g, paint, player);
		

        
        //Text
        paint.setTypeface(Assets.plain);
        paint.setTextSize(50);
        g.drawString("Press anywhere to start", 640, 300, Color.WHITE, paint);

	
	}


	private void drawRunningUI() {
		Graphics g = game.getGraphics();
		
		paint.setTextSize(40);
		
		// no screen clear needed, wallpaper does this
		
		//draws rest of screen
		
		//powerups
		p.paint(g, paint);
		
		//enemies
		c.paint(g, paint);
		
		//main character
		player.paint(g, paint);
		
		//UI
		paint.setTypeface(Assets.plain);
		g.drawRect(480, 0, 320, 50, Color.argb(100, 255, 255, 255));
        g.drawString("SCORE: " + score, 640, 40, Color.WHITE, paint);
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
		
		// Background Rectangle
		g.drawRect(TILT_MENU_X - 40, TILT_MENU_Y - 20, 275, 360 + tiltedTiltButton.getWidth(), Color.DKGRAY);
        g.drawString("Tilt Options", TILT_MENU_X + 85, TILT_MENU_Y - 50, Color.WHITE, paint);
		
		// sound control buttons
		if(MainMenuScreen.sound == true){
			g.drawImage(Assets.sound, SOUND_X, SOUND_Y);
		} else {
			g.drawImage(Assets.sound_muted, SOUND_X, SOUND_Y);
		}
		
		if(MainMenuScreen.music == true){
			g.drawImage(Assets.music, SOUND_X, SOUND_Y + 200);
		} else {
			g.drawImage(Assets.music_muted, SOUND_X, SOUND_Y + 200);
		}
		
		// buttons
        menuButton.paint(g, paint, player);
        flatTiltButton.paint(g, paint, player);
        tiltedTiltButton.paint(g, paint, player);
        customTiltButton.paint(g, paint, player);


        // text
        paint.setTypeface(Assets.plain);

        paint.setTextSize(50);
		g.drawString("Press anywhere to resume", 640, 300, Color.WHITE, paint);
		
		g.drawString("Tilt Options", TILT_MENU_X + 85, TILT_MENU_Y - 50, Color.WHITE, paint);
		
		
	}


	private void drawGameOverUI() {
		Graphics g = game.getGraphics();
		
		// alpha bg
		g.drawARGB(155, 0, 0, 0);


        // buttons
        menuButton.paint(g, paint, player);
        retryButton.paint(g,paint, player);

		// buttons
		g.drawRect(0, 0, 200, 100, Color.rgb(195, 195, 195));
        g.drawRect(540, 500, 200, 100, Color.rgb(195, 195, 195));
		g.drawImage(Assets.gpg_icon_leaderboards, 1175, 5);
		
		//text
		paint.setTypeface(Assets.plain);
		paint.setTextSize(50);
		g.drawString("Menu", 100, 70, Color.WHITE, paint);
        g.drawString("Retry", 640, 570, Color.WHITE, paint);
		paint.setTextSize(100);
		g.drawString("Game Over!", 640, 200, Color.WHITE, paint);
		paint.setTextSize(60);
		g.drawString("Score: " + score, 640, 400, Color.WHITE, paint);

		
	}

	//OTHER METHODS
	@Override
	public void pause() {
		if (state == GameState.Running){
            state = GameState.Paused;
            if (MainMenuScreen.music == true){
            	Assets.darude.setVolume(0.25f); 
            }
		}
	}

	@Override
	public void resume() {
        if (MainMenuScreen.music == true) {
            Assets.darude.play();
            Assets.darude.setVolume(0.25f);
        }
	}

	@Override
	public void dispose() {
	}
	
	public void reset() {	// sets all objects to null for new game
		paint = null;
		player = null;
		c = null;
		r = null;
		p = null;
        e = null;
		tempEnemyPoint = null;
		state = null;
		score = 0;
		facingAngle = 0;
		enemyCounter = 0;
		scoreUploaded = false;
		
		gameOverFlag = false;
		updateCount = 0;
		
		// Garbage collector clears memory
		System.gc();
	}

    public void restart() {
        game.setScreen(new GameScreen(majorProjectGame));
    }

	private void goToMenu() {
		game.setScreen(new MainMenuScreen(majorProjectGame));
		if (MainMenuScreen.music == true){
        	Assets.darude.setVolume(0.85f); 
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

    public static int getEnemyCounter() {
        return enemyCounter;
    }

    public static void setEnemyCounter(int enemyCounter) {
        GameScreen.enemyCounter = enemyCounter;
    }

    public static int getScore() {
        return score;
    }

    public static void setScore(int score) {
        GameScreen.score = score;
    }

	public static int getUpdateCount() {
		return updateCount;
	}


	public static Player getPlayer() {
		return player;
	}


	public static void setPlayer(Player player) {
		GameScreen.player = player;
	}


	public boolean isGameOverFlag() {
		return gameOverFlag;
	}


	public float getFacingAngle() {
		return facingAngle;
	}


	public static void setGameOverFlag(boolean gameOverFlag) {
		GameScreen.gameOverFlag = gameOverFlag;
	}


	public void setFacingAngle(float facingAngle) {
		this.facingAngle = facingAngle;
	}


	public static void setUpdateCount(int updateCount) {
		GameScreen.updateCount = updateCount;
	}


	public static int getSpeed() {
		return speed;
	}

	public static void setSpeed(int speed) {
		GameScreen.speed = speed;
	}
	
}
