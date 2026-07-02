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
import com.aocc.majorproject.input.GamepadInput;
import com.aocc.majorproject.ui.ComboMeter;
import com.aocc.majorproject.ui.ScoreBar;
import com.aocc.majorproject.ui.SettingsPanel;
import com.aocc.majorproject.ui.UiBanner;
import com.aocc.majorproject.ui.UiButton;
import com.aocc.majorproject.ui.UiBounds;

public class GameScreen extends Screen {

	public enum GameState {Ready, Running, Paused, GameOver}
	GameState state = GameState.Ready;

	MajorProjectGame majorProjectGame;

	Paint paint;
	private final GameSession session = new GameSession();
	private Player player;
	EnemyController c;
	Random r;
	PowerUp p;
	Point tempEnemyPoint;

    final int ARENA_HEIGHT = 3;

    UiButton menuButton;
    UiButton retryButton;
    private final SettingsPanel settingsPanel = new SettingsPanel();

    private final ScoreBar scoreBar = new ScoreBar();
    private final ComboMeter comboMeter = new ComboMeter();
    // Prompt placed below the settings panel (panel bottom = PANEL_Y + PANEL_HEIGHT = 590).
    static final int PROMPT_Y = SettingsPanel.PANEL_Y + SettingsPanel.PANEL_HEIGHT + 65;
    private final UiBanner promptBanner = new UiBanner(50f);
    private final UiBanner gameOverBanner = new UiBanner(100f);
    private final UiBanner scoreBanner = new UiBanner(60f);

	private float facingAngle = 0;
	private int gameOverSelection = 0;
	private static final int GAME_OVER_ITEM_COUNT = 3;
	private static final int HIGHLIGHT_PADDING = 4;

	public GameScreen(MajorProjectGame game) {
		super(game);

		majorProjectGame = game;
		settingsPanel.setGame(majorProjectGame);

		player = session.getPlayer();
		GamePreferences.applyTiltTo(player);
		c = new EnemyController(session);
		r = new Random();
		p = new PowerUp(1, session);
		tempEnemyPoint = new Point();

        menuButton = UiButton.menuAt(0, 0);
        retryButton = new UiButton(540, 500, UiButton.MENU_WIDTH, UiButton.MENU_HEIGHT, "Retry");

		paint = new Paint();
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
	}

	@Override
	public void update(float deltaTime) {
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
		List<GamepadInput.Action> gamepadActions = majorProjectGame.getGamepadInput().consumeActions();

        if (state == GameState.Ready) {
            updateReady(touchEvents);
            handleGamepadReady(gamepadActions);
        }
        if (state == GameState.Running) {
            updateRunning(touchEvents, deltaTime);
        }
        if (state == GameState.Paused) {
            updatePaused(touchEvents);
            handleGamepadPaused(gamepadActions);
        }
        if (state == GameState.GameOver) {
            updateGameOver(touchEvents);
            handleGamepadGameOver(gamepadActions);
        }
	}

	private void updateReady(List<TouchEvent> touchEvents) {
		for (int i = 0; i < touchEvents.size(); i++) {
			TouchEvent event = touchEvents.get(i);
		    if (event.type == TouchEvent.TOUCH_UP) {
		    	if (settingsPanel.handleTouch(event, player)) {
		    		continue;
		    	}
		    	playTap();
		    	changeState(GameState.Running);
	    	}
		}
	}

	private void handleGamepadReady(List<GamepadInput.Action> actions) {
		for (GamepadInput.Action action : actions) {
			if (action == GamepadInput.Action.CANCEL) {
				goToMenu();
				return;
			}
			if (settingsPanel.handleGamepad(action, player)) {
				continue;
			}
			if (action == GamepadInput.Action.CONFIRM) {
				playTap();
				changeState(GameState.Running);
			}
		}
	}

	private void updateRunning(List<TouchEvent> touchEvents, float deltaSeconds) {
		float step = GameConstants.secondsToSteps(deltaSeconds);

		session.addUpdateCount(step);
		session.addEnemyCounter(step);

		player.update(deltaSeconds);
		c.update(deltaSeconds);
		p.update(deltaSeconds);

		if (session.getSpeed() < 25 && session.getUpdateCount() >= GameConstants.SPEED_RAMP_INTERVAL_FRAMES) {
			session.incrementSpeed();
			session.subtractUpdateCount(GameConstants.SPEED_RAMP_INTERVAL_FRAMES);
			c.increaseEnemyTopSpeed();
		}

		if (session.getEnemyCounter() > c.getNextEnemySpawn()) {
			tempEnemyPoint.x = r.nextInt(GameConstants.WORLD_WIDTH - 100) + 50;
			tempEnemyPoint.y = r.nextInt(GameConstants.WORLD_HEIGHT - 100) + 50;
			PersonalMethods.limitOutside(tempEnemyPoint, (int)player.getCenterX(), (int)player.getCenterY(), 100);
			if (r.nextInt(10) == 9) {
				c.addEnemy(tempEnemyPoint.x, tempEnemyPoint.y, 2);
			} else {
				c.addEnemy(tempEnemyPoint.x, tempEnemyPoint.y, 1);
			}
			session.resetEnemyCounter();
		}

		if (player.getCombo() == 200) {
			majorProjectGame.onAchievementUnlocked(
					majorProjectGame.getString(R.string.achievement_ccccombo));
		}

		if (session.isGameOverFlag()) {
			changeState(GameState.GameOver);
		}
	}

	private void updatePaused(List<TouchEvent> touchEvents) {
		for (int i = 0; i < touchEvents.size(); i++) {
			TouchEvent event = touchEvents.get(i);
		    if (event.type == TouchEvent.TOUCH_UP) {
		    	if (menuButton.touchInBounds(event)) {
		    		playTap();
					reset();
					goToMenu();
					return;
		    	}
		    	if (settingsPanel.handleTouch(event, player)) {
		    		continue;
		    	}
                playTap();
                resumeFromPause();
	    	}
		}
	}

	private void handleGamepadPaused(List<GamepadInput.Action> actions) {
		for (GamepadInput.Action action : actions) {
			if (action == GamepadInput.Action.CANCEL) {
				resumeFromPause();
				return;
			}
			if (settingsPanel.handleGamepad(action, player)) {
				continue;
			}
			if (action == GamepadInput.Action.CONFIRM) {
				resumeFromPause();
			}
		}
	}

	private void resumeFromPause() {
		playTap();
		changeState(GameState.Running);
		if (GamePreferences.music) {
			Assets.setMusicVolume(0.85f);
		}
	}

	private void updateGameOver(List<TouchEvent> touchEvents) {
		if (!session.isScoreUploaded()) {
			majorProjectGame.onEnteredScore(session.getScore());
			session.setScoreUploaded(true);
		}

		for (int i = 0; i < touchEvents.size(); i++) {
			TouchEvent event = touchEvents.get(i);
		    if (event.type == TouchEvent.TOUCH_UP) {
		    	if (menuButton.touchInBounds(event)) {
		    		playTap();
					reset();
					goToMenu();
					return;
		    	}
                if (retryButton.touchInBounds(event)) {
                    playTap();
                    reset();
                    restart();
                }
		    	if (PersonalMethods.touchInBounds(event, 1175, 5, 100, 80)) {
		    		playTap();
		    		majorProjectGame.onShowLeaderboardsRequested(
		    				majorProjectGame.getString(R.string.leaderboard_pacifist_mode));
		    	}
		    }
		}
	}

	private void handleGamepadGameOver(List<GamepadInput.Action> actions) {
		for (GamepadInput.Action action : actions) {
			switch (action) {
				case UP:
					gameOverSelection = (gameOverSelection + GAME_OVER_ITEM_COUNT - 1) % GAME_OVER_ITEM_COUNT;
					break;
				case DOWN:
					gameOverSelection = (gameOverSelection + 1) % GAME_OVER_ITEM_COUNT;
					break;
				case CONFIRM:
					activateGameOverItem(gameOverSelection);
					break;
				case CANCEL:
					playTap();
					reset();
					goToMenu();
					break;
				default:
					break;
			}
		}
	}

	private void activateGameOverItem(int index) {
		switch (index) {
			case 0:
				playTap();
				reset();
				goToMenu();
				break;
			case 1:
				playTap();
				reset();
				restart();
				break;
			case 2:
				playTap();
				majorProjectGame.onShowLeaderboardsRequested(
						majorProjectGame.getString(R.string.leaderboard_pacifist_mode));
				break;
			default:
				break;
		}
	}

	private UiBounds getGameOverItemBounds(int index) {
		return switch (index) {
			case 0 -> menuButton.getBounds();
			case 1 -> retryButton.getBounds();
			case 2 -> new UiBounds(1175, 5, 100, 80);
			default -> null;
		};
	}

	@Override
	public void paint(float deltaTime) {
		Graphics g = game.getGraphics();
		g.drawImage(Assets.game_bg, 0, 0);

		if (state == GameState.Ready)   drawReadyUI();
		if (state == GameState.Running) drawRunningUI();
		if (state == GameState.Paused)  drawPausedUI();
		if (state == GameState.GameOver) drawGameOverUI();

        VersionOverlay.paint(g);
	}

	private void drawReadyUI() {
		Graphics g = game.getGraphics();
		g.drawARGB(155, 0, 0, 0);
		settingsPanel.paint(g, paint, player);
        paint.setTypeface(Assets.plain);
        promptBanner.paint(g, paint, "Press anywhere to start",
                GameConstants.WORLD_WIDTH / 2, PROMPT_Y);
	}

	private void drawRunningUI() {
		Graphics g = game.getGraphics();
		p.paint(g, paint);
		c.paint(g, paint);
		player.paint(g, paint);
        paint.setTypeface(Assets.plain);
		comboMeter.paint(g, paint, player.getCombo());
		scoreBar.paint(g, paint, session.getScore());
	}

	private void drawPausedUI() {
		Graphics g = game.getGraphics();
		g.drawRect(0, 0, 1281, 721, Color.BLACK);
		paint.setTextSize(40);
		p.paint(g, paint);
		c.paint(g, paint);
		player.paint(g, paint);
		g.drawARGB(155, 0, 0, 0);
		settingsPanel.paint(g, paint, player);
        menuButton.paint(g);
        paint.setTypeface(Assets.plain);
		promptBanner.paint(g, paint, "Press anywhere to resume",
                GameConstants.WORLD_WIDTH / 2, PROMPT_Y);
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
		paintGameOverHighlight(g);
	}

	private void paintGameOverHighlight(Graphics g) {
		UiBounds bounds = getGameOverItemBounds(gameOverSelection);
		if (bounds == null) {
			return;
		}
		g.drawRect(bounds.x - HIGHLIGHT_PADDING, bounds.y - HIGHLIGHT_PADDING,
				bounds.width + HIGHLIGHT_PADDING * 2, bounds.height + HIGHLIGHT_PADDING * 2,
				Color.rgb(255, 220, 80));
	}

	@Override
	public void pause() {
		if (state == GameState.Running) {
            changeState(GameState.Paused);
            if (GamePreferences.music) {
            	Assets.setMusicVolume(0.25f);
            }
		}
	}

	@Override
	public void resume() {
        if (GamePreferences.music) {
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
		if (GamePreferences.music) {
        	Assets.setMusicVolume(0.85f);
        }
	}

	@Override
	public void backButton() {
		if (state == GameState.Running) {
			pause();
		} else if (state == GameState.Paused) {
			resumeFromPause();
		} else if (state == GameState.GameOver) {
			reset();
			goToMenu();
		} else {
			goToMenu();
		}
	}

	private void changeState(GameState newState) {
		state = newState;
		majorProjectGame.getSecondaryDisplayManager().updateForGameState(newState);
	}

	private void playTap() {
		Assets.tap.play(GamePreferences.getTapVolume());
	}

	public GameState getState() {
		return state;
	}

	public boolean isGameOverFlag() { return session.isGameOverFlag(); }
	public float getFacingAngle() { return facingAngle; }
	public void setFacingAngle(float facingAngle) { this.facingAngle = facingAngle; }
}
