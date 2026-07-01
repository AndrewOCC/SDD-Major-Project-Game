package com.aocc.majorproject;

import android.app.Activity;
import android.app.AlertDialog;

import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayGames;

/**
 * Thin wrapper around Play Games Services v2 for sign-in, leaderboards,
 * and achievements.
 */
public class PlayGamesHelper {

    private final Activity activity;
    private final GamesSignInClient signInClient;
    private final LeaderboardsClient leaderboardsClient;
    private final AchievementsClient achievementsClient;
    private volatile boolean signedIn;
    private boolean userSignedOut;

    public PlayGamesHelper(Activity activity) {
        this.activity = activity;
        signInClient = PlayGames.getGamesSignInClient(activity);
        leaderboardsClient = PlayGames.getLeaderboardsClient(activity);
        achievementsClient = PlayGames.getAchievementsClient(activity);
    }

    public void refreshSignInState() {
        if (userSignedOut) {
            signedIn = false;
            return;
        }
        signInClient.isAuthenticated().addOnCompleteListener(task -> {
            signedIn = task.isSuccessful() && task.getResult().isAuthenticated();
        });
    }

    public boolean isSignedIn() {
        return signedIn;
    }

    public void signIn() {
        userSignedOut = false;
        signInClient.signIn().addOnCompleteListener(task -> {
            signedIn = task.isSuccessful();
        });
    }

    public void signOut() {
        userSignedOut = true;
        signedIn = false;
    }

    public void showLeaderboards(String leaderboardId) {
        if (!signedIn) {
            showAlert(activity.getString(R.string.leaderboards_not_available));
            return;
        }

        if (leaderboardId == null || leaderboardId.isEmpty()) {
            leaderboardsClient.getAllLeaderboardsIntent()
                    .addOnSuccessListener(intent -> activity.startActivity(intent));
        } else {
            leaderboardsClient.getLeaderboardIntent(leaderboardId)
                    .addOnSuccessListener(intent -> activity.startActivity(intent));
        }
    }

    public void showAchievements() {
        if (!signedIn) {
            showAlert(activity.getString(R.string.leaderboards_not_available));
            return;
        }

        achievementsClient.getAchievementsIntent()
                .addOnSuccessListener(intent -> activity.startActivity(intent));
    }

    public void submitScore(String leaderboardId, long score) {
        if (signedIn) {
            leaderboardsClient.submitScore(leaderboardId, score);
        }
    }

    public void unlockAchievement(String achievementId) {
        if (signedIn) {
            achievementsClient.unlock(achievementId);
        }
    }

    public void showAlert(String message) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
