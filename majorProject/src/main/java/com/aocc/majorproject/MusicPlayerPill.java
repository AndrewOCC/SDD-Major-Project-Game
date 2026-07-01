package com.aocc.majorproject;

import android.graphics.Color;
import android.graphics.Paint;

import com.aocc.framework.Graphics;
import com.aocc.framework.Image;
import com.aocc.framework.Input.TouchEvent;
import com.aocc.framework.PersonalMethods;

public class MusicPlayerPill {

    public static final int PILL_X = 340;
    public static final int PILL_Y = 610;
    public static final int PILL_W = 600;
    public static final int PILL_H = 90;
    public static final int PILL_RADIUS = 45;

    private static final int ART_X = PILL_X + 10;
    private static final int ART_Y = PILL_Y + 10;
    private static final int ART_SIZE = 70;

    private static final int PREV_X = PILL_X + 390;
    private static final int NEXT_X = PILL_X + 510;
    private static final int PLAY_X = PILL_X + 450;
    private static final int CONTROL_Y = PILL_Y + 20;
    private static final int CONTROL_SIZE = 50;

    public boolean handleTouch(TouchEvent event, MajorProjectGame game) {
        if (event.type != TouchEvent.TOUCH_UP) {
            return false;
        }

        if (!PersonalMethods.touchInBounds(event, PILL_X, PILL_Y, PILL_W, PILL_H)) {
            return false;
        }

        Assets.tap.play(MainMenuScreen.tapVol);

        if (PersonalMethods.touchInBounds(event, PREV_X, CONTROL_Y, CONTROL_SIZE, CONTROL_SIZE)) {
            skipPrevious(game);
            return true;
        }

        if (PersonalMethods.touchInBounds(event, PLAY_X, CONTROL_Y, CONTROL_SIZE, CONTROL_SIZE)) {
            togglePlayPause(game);
            return true;
        }

        if (PersonalMethods.touchInBounds(event, NEXT_X, CONTROL_Y, CONTROL_SIZE, CONTROL_SIZE)) {
            skipNext(game);
            return true;
        }

        return true;
    }

    public void paint(Graphics g, Paint paint, MajorProjectGame game) {
        g.drawRoundRect(PILL_X, PILL_Y, PILL_W, PILL_H, PILL_RADIUS,
                Color.argb(210, 25, 25, 25));

        String title = game.getString(R.string.default_track_name);
        String artist = game.getString(R.string.default_track_artist);
        boolean paused = !MainMenuScreen.music;
        Image albumArt = null;

        SpotifyMusicHelper spotify = game.getSpotifyMusicHelper();
        if (spotify != null && spotify.isConnected()) {
            title = truncate(spotify.getTrackName(), 24);
            artist = truncate(spotify.getArtistName(), 28);
            paused = spotify.isPaused();
            albumArt = spotify.getAlbumArt();
        }

        drawAlbumArt(g, paint, albumArt, title);

        paint.setTypeface(Assets.plain);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(24);
        g.drawString(title, PILL_X + 95, PILL_Y + 38, Color.WHITE, paint);

        paint.setTextSize(18);
        g.drawString(artist, PILL_X + 95, PILL_Y + 64, Color.argb(220, 200, 200, 200), paint);

        drawControlButton(g, paint, PREV_X, "|<");
        drawControlButton(g, paint, PLAY_X, paused ? ">" : "||");
        drawControlButton(g, paint, NEXT_X, ">|");
    }

    private void drawAlbumArt(Graphics g, Paint paint, Image albumArt, String title) {
        if (albumArt != null) {
            g.drawCircularImage(albumArt, ART_X, ART_Y, ART_SIZE);
            return;
        }

        g.drawCircle(ART_X + ART_SIZE / 2f, ART_Y + ART_SIZE / 2f, ART_SIZE / 2f,
                Color.argb(255, 70, 70, 70));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(28);
        String initial = title.isEmpty() ? "?" : title.substring(0, 1).toUpperCase();
        g.drawString(initial, ART_X + ART_SIZE / 2, ART_Y + ART_SIZE / 2 + 10,
                Color.WHITE, paint);
    }

    private void drawControlButton(Graphics g, Paint paint, int x, String label) {
        g.drawCircle(x + CONTROL_SIZE / 2f, CONTROL_Y + CONTROL_SIZE / 2f,
                CONTROL_SIZE / 2f, Color.argb(255, 55, 55, 55));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(22);
        g.drawString(label, x + CONTROL_SIZE / 2, CONTROL_Y + CONTROL_SIZE / 2 + 8,
                Color.WHITE, paint);
    }

    private void togglePlayPause(MajorProjectGame game) {
        SpotifyMusicHelper spotify = game.getSpotifyMusicHelper();
        if (spotify != null && spotify.isConnected()) {
            spotify.togglePlayPause();
            return;
        }

        if (MainMenuScreen.music) {
            MainMenuScreen.music = false;
            Assets.setMusicVolume(0);
        } else {
            MainMenuScreen.music = true;
            Assets.setMusicVolume(0.85f);
            Assets.playMusic();
        }
    }

    private void skipPrevious(MajorProjectGame game) {
        SpotifyMusicHelper spotify = game.getSpotifyMusicHelper();
        if (spotify != null && spotify.isConnected()) {
            spotify.skipPrevious();
            return;
        }

        Assets.playMusic();
    }

    private void skipNext(MajorProjectGame game) {
        SpotifyMusicHelper spotify = game.getSpotifyMusicHelper();
        if (spotify != null && spotify.isConnected()) {
            spotify.skipNext();
            return;
        }

        Assets.playMusic();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value != null ? value : "";
        }
        return value.substring(0, maxLength - 1) + "…";
    }
}
