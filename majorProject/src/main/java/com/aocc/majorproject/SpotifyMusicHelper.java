package com.aocc.majorproject;

import android.app.Activity;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

/**
 * Connects to the Spotify app via App Remote and plays a configured playlist.
 * Falls back to bundled game music when Spotify is unavailable or not configured.
 */
public class SpotifyMusicHelper {

    private final Activity activity;
    private SpotifyAppRemote spotifyAppRemote;
    private volatile boolean connected;
    private volatile boolean connecting;
    private volatile boolean wantsPlayback;

    public SpotifyMusicHelper(Activity activity) {
        this.activity = activity;
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
    }

    public void pause() {
        wantsPlayback = false;
        if (isConnected()) {
            spotifyAppRemote.getPlayerApi().pause();
        }
    }

    public void resumeOrPlay() {
        if (!MainMenuScreen.music) {
            return;
        }

        wantsPlayback = true;
        if (isConnected()) {
            startPlayback();
        }
    }

    private void startPlayback() {
        if (!isConnected()) {
            return;
        }

        spotifyAppRemote.getPlayerApi().play(activity.getString(R.string.spotify_playlist_uri));
    }
}
