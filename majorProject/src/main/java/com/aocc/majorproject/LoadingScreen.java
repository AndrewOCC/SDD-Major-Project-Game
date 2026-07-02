package com.aocc.majorproject;

import android.graphics.Color;
import android.graphics.Paint;

import com.aocc.framework.GameConstants;
import com.aocc.framework.Graphics;
import com.aocc.framework.Screen;
import com.aocc.majorproject.ui.UiText;

public class LoadingScreen extends Screen {

    private static final int BAR_WIDTH = 480;
    private static final int BAR_HEIGHT = 24;
    private static final int BAR_X = (GameConstants.WORLD_WIDTH - BAR_WIDTH) / 2;
    private static final int BAR_Y = 620;

    private final MajorProjectGame majorProjectGame;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final AssetLoader assetLoader;

    private float progress;
    private boolean transitionPending;
    private boolean loadFailed;

    public LoadingScreen(MajorProjectGame game) {
        super(game);
        majorProjectGame = game;
        assetLoader = new AssetLoader(game, new AssetLoader.Listener() {
            @Override
            public void onProgress(int loaded, int total) {
                progress = loaded / (float) total;
            }

            @Override
            public void onComplete() {
                transitionPending = true;
            }

            @Override
            public void onError(Exception error) {
                loadFailed = true;
                CrashReporter.log(majorProjectGame, "Asset loading failed", error);
            }
        });
        assetLoader.start();
    }

    @Override
    public void update(float deltaTime) {
        if (transitionPending) {
            game.setScreen(new MainMenuScreen(majorProjectGame));
        }
    }

    @Override
    public void paint(float deltaTime) {
        Graphics g = game.getGraphics();

        if (Assets.splash != null) {
            g.drawImage(Assets.splash, 0, 0);
        } else {
            g.drawRect(0, 0, GameConstants.WORLD_WIDTH, GameConstants.WORLD_HEIGHT, Color.BLACK);
        }

        g.drawRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, Color.argb(120, 255, 255, 255));
        int fillWidth = Math.max(1, Math.round(BAR_WIDTH * progress));
        g.drawRect(BAR_X, BAR_Y, fillWidth, BAR_HEIGHT, Color.argb(220, 80, 180, 255));

        if (Assets.plain != null) {
            paint.setTypeface(Assets.plain);
        }
        paint.setTextSize(28f);
        String label = loadFailed ? "Load failed — retrying may help" : "Loading…";
        UiText.drawCentered(g, paint, label, GameConstants.WORLD_WIDTH / 2, BAR_Y - 24,
                Color.WHITE);
        UiText.drawCentered(g, paint, Math.round(progress * 100) + "%",
                GameConstants.WORLD_WIDTH / 2, BAR_Y + BAR_HEIGHT / 2, Color.WHITE);

        VersionOverlay.paint(g);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        assetLoader.shutdown();
    }

    @Override
    public void backButton() {
    }
}
