package com.aocc.majorproject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.aocc.framework.Image;
import com.aocc.framework.implementation.AndroidImage;
import com.aocc.framework.Graphics.ImageFormat;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Connects to the Spotify app via App Remote, plays Darude Sandstorm on loop,
 * and exposes player metadata for the in-game music pill UI.
 */
public class SpotifyMusicHelper {

    private static final int REPEAT_TRACK = 2;

    private final Activity activity;
    private SpotifyAppRemote spotifyAppRemote;
    private volatile boolean connected;
    private volatile boolean connecting;
    private volatile boolean wantsPlayback;
    private volatile boolean paused = true;
    private volatile String trackName;
    private volatile String artistName;
    private volatile Image albumArt;
    private String loadedImageUri;
    private final AtomicBoolean loadingArt = new AtomicBoolean(false);

    public SpotifyMusicHelper(Activity activity) {
        this.activity = activity;
        trackName = activity.getString(R.string.default_track_name);
        artistName = activity.getString(R.string.default_track_artist);
    }

    public boolean isConfigured() {
        String clientId = activity.getString(R.string.spotify_client_id);
        return clientId != null
                && !clientId.isEmpty()
                && !clientId.equals("YOUR_SPOTIFY_CLIENT_ID");
    }

    public boolean isConnected() {
        return connected && spotifyAppRemote != null;
    }

    public boolean isPaused() {
        return paused;
    }

    public String getTrackName() {
        return trackName != null ? trackName : activity.getString(R.string.default_track_name);
    }

    public String getArtistName() {
        return artistName != null ? artistName : activity.getString(R.string.default_track_artist);
    }

    public Image getAlbumArt() {
        return albumArt;
    }

    public void connect() {
        if (connecting || connected || !isConfigured()) {
            return;
        }

        connecting = true;
        ConnectionParams connectionParams = new ConnectionParams.Builder(
                activity.getString(R.string.spotify_client_id))
                .setRedirectUri(activity.getString(R.string.spotify_redirect_uri))
                .showAuthView(true)
                .build();

        SpotifyAppRemote.connect(activity, connectionParams, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote appRemote) {
                spotifyAppRemote = appRemote;
                connected = true;
                connecting = false;
                subscribeToPlayerState();
                Assets.onSpotifyConnected();
            }

            @Override
            public void onFailure(Throwable throwable) {
                connecting = false;
                connected = false;
                spotifyAppRemote = null;
                Exception exception = throwable instanceof Exception
                        ? (Exception) throwable
                        : new Exception(throwable);
                CrashReporter.log(activity, "Spotify connect failed", exception);
            }
        });
    }

    public void disconnect() {
        wantsPlayback = false;
        if (spotifyAppRemote != null) {
            SpotifyAppRemote.disconnect(spotifyAppRemote);
            spotifyAppRemote = null;
        }
        connected = false;
        connecting = false;
        paused = true;
    }

    public void pause() {
        wantsPlayback = false;
        paused = true;
        if (isConnected()) {
            spotifyAppRemote.getPlayerApi().pause();
        }
    }

    public void resumeOrPlay() {
        if (!MainMenuScreen.music) {
            return;
        }

        wantsPlayback = true;
        paused = false;
        if (isConnected()) {
            startPlayback();
        }
    }

    public void togglePlayPause() {
        if (!isConnected()) {
            return;
        }

        if (paused) {
            wantsPlayback = true;
            paused = false;
            spotifyAppRemote.getPlayerApi().resume();
        } else {
            wantsPlayback = false;
            paused = true;
            spotifyAppRemote.getPlayerApi().pause();
        }
    }

    public void skipPrevious() {
        if (isConnected()) {
            spotifyAppRemote.getPlayerApi().skipPrevious();
        }
    }

    public void skipNext() {
        if (isConnected()) {
            spotifyAppRemote.getPlayerApi().skipNext();
        }
    }

    private void startPlayback() {
        if (!isConnected()) {
            return;
        }

        String trackUri = activity.getString(R.string.spotify_track_uri);
        spotifyAppRemote.getPlayerApi().play(trackUri);
        spotifyAppRemote.getPlayerApi().setRepeat(REPEAT_TRACK);
        paused = false;
    }

    private void subscribeToPlayerState() {
        if (!isConnected()) {
            return;
        }

        spotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(this::updateFromPlayerState);
    }

    private void updateFromPlayerState(PlayerState playerState) {
        if (playerState == null) {
            return;
        }

        paused = playerState.isPaused;
        Track track = playerState.track;
        if (track != null) {
            trackName = track.name;
            if (track.artist != null) {
                artistName = track.artist.name;
            }
            if (track.imageUri != null) {
                loadAlbumArt(track.imageUri.raw);
            }
        }

        if (wantsPlayback && paused && track != null && track.duration > 0
                && playerState.playbackPosition >= track.duration - 750) {
            startPlayback();
        }
    }

    private void loadAlbumArt(String imageUri) {
        if (imageUri == null || imageUri.isEmpty() || imageUri.equals(loadedImageUri)) {
            return;
        }

        if (!loadingArt.compareAndSet(false, true)) {
            return;
        }

        new Thread(() -> {
            Bitmap bitmap = fetchBitmap(imageUri);
            activity.runOnUiThread(() -> {
                loadingArt.set(false);
                if (bitmap == null) {
                    return;
                }

                loadedImageUri = imageUri;
                if (albumArt instanceof AndroidImage) {
                    ((AndroidImage) albumArt).dispose();
                }
                albumArt = new AndroidImage(bitmap, ImageFormat.ARGB8888);
            });
        }).start();
    }

    private Bitmap fetchBitmap(String imageUri) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(imageUri);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            inputStream = connection.getInputStream();
            return BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            CrashReporter.log(activity, "Failed to load Spotify album art", e);
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ignored) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
