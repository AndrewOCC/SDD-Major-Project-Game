package com.aocc.majorproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.AuthenticationResult;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.tasks.Task;

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
    private volatile boolean signInStatusKnown;
    private volatile boolean signInInProgress;

    public PlayGamesHelper(Activity activity) {
        this.activity = activity;
        signInClient = PlayGames.getGamesSignInClient(activity);
        leaderboardsClient = PlayGames.getLeaderboardsClient(activity);
        achievementsClient = PlayGames.getAchievementsClient(activity);
    }

    public void initializeSignIn() {
        checkAuthentication(true);
    }

    public void refreshSignInState() {
        checkAuthentication(true);
    }

    public boolean isSignInStatusKnown() {
        return signInStatusKnown;
    }

    public boolean isSignedIn() {
        return signedIn;
    }

    public void signIn() {
        requestSignIn(true);
    }

    public void showLeaderboards(String leaderboardId) {
        runOnUiThread(() -> {
            if (!signedIn) {
                showAlert(activity.getString(R.string.leaderboards_not_available));
                signIn();
                return;
            }

            if (leaderboardId == null || leaderboardId.isEmpty()) {
                leaderboardsClient.getAllLeaderboardsIntent()
                        .addOnSuccessListener(intent -> activity.startActivity(intent))
                        .addOnFailureListener(e -> showAlert(
                                activity.getString(R.string.play_games_leaderboards_failed)));
            } else {
                leaderboardsClient.getLeaderboardIntent(leaderboardId)
                        .addOnSuccessListener(intent -> activity.startActivity(intent))
                        .addOnFailureListener(e -> showAlert(
                                activity.getString(R.string.play_games_leaderboards_failed)));
            }
        });
    }

    public void showAchievements() {
        runOnUiThread(() -> {
            if (!signedIn) {
                showAlert(activity.getString(R.string.achievements_not_available));
                signIn();
                return;
            }

            achievementsClient.getAchievementsIntent()
                    .addOnSuccessListener(intent -> activity.startActivity(intent))
                    .addOnFailureListener(e -> showAlert(
                            activity.getString(R.string.play_games_achievements_failed)));
        });
    }

    public void submitScore(String leaderboardId, long score) {
        if (!signedIn) {
            return;
        }

        try {
            leaderboardsClient.submitScore(leaderboardId, score);
            runOnUiThread(() -> showToast(R.string.saved_toast));
        } catch (RuntimeException e) {
            runOnUiThread(() -> showAlert(activity.getString(R.string.play_games_score_failed)));
        }
    }

    public void unlockAchievement(String achievementId) {
        if (!signedIn) {
            return;
        }

        try {
            achievementsClient.unlock(achievementId);
        } catch (RuntimeException e) {
            runOnUiThread(() -> showToast(R.string.play_games_achievement_failed));
        }
    }

    private void checkAuthentication(boolean attemptSignInIfNeeded) {
        signInClient.isAuthenticated().addOnCompleteListener(task -> {
            updateSignedInFromTask(task);
            if (!signedIn && attemptSignInIfNeeded) {
                requestSignIn(false);
            }
        });
    }

    private void requestSignIn(boolean userInitiated) {
        if (signInInProgress) {
            return;
        }

        signInInProgress = true;
        signInClient.signIn().addOnCompleteListener(task -> runOnUiThread(() -> {
            signInInProgress = false;
            updateSignedInFromTask(task);

            if (signedIn) {
                if (userInitiated) {
                    showToast(R.string.play_games_sign_in_success);
                }
                return;
            }

            if (!userInitiated) {
                return;
            }

            Exception exception = task.getException();
            if (exception instanceof ApiException) {
                int statusCode = ((ApiException) exception).getStatusCode();
                if (statusCode == 12501 || statusCode == 12502) {
                    return;
                }
            }

            showAlert(activity.getString(R.string.play_games_sign_in_failed));
        }));
    }

    private void updateSignedInFromTask(Task<AuthenticationResult> task) {
        signInStatusKnown = true;
        signedIn = task.isSuccessful()
                && task.getResult() != null
                && task.getResult().isAuthenticated();
    }

    private void runOnUiThread(Runnable action) {
        activity.runOnUiThread(action);
    }

    private void showAlert(String message) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showToast(int messageResId) {
        Toast.makeText(activity.getApplicationContext(), messageResId, Toast.LENGTH_SHORT).show();
    }
}
