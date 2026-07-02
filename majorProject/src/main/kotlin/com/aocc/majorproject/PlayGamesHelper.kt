package com.aocc.majorproject

import android.app.Activity
import android.app.AlertDialog
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.AchievementsClient
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.LeaderboardsClient
import com.google.android.gms.games.PlayGames

/**
 * Thin wrapper around Play Games Services v2 for sign-in, leaderboards,
 * and achievements.
 */
class PlayGamesHelper(private val activity: Activity) {

    private val signInClient: GamesSignInClient = PlayGames.getGamesSignInClient(activity)
    private val leaderboardsClient: LeaderboardsClient = PlayGames.getLeaderboardsClient(activity)
    private val achievementsClient: AchievementsClient = PlayGames.getAchievementsClient(activity)

    @Volatile
    private var signedIn = false

    fun refreshSignInState() {
        signInClient.isAuthenticated.addOnCompleteListener { task ->
            signedIn = task.isSuccessful
                && task.result != null
                && task.result!!.isAuthenticated
        }
    }

    fun isSignedIn(): Boolean = signedIn

    fun signIn() {
        signInClient.signIn().addOnCompleteListener { task ->
            runOnUiThread {
                if (task.isSuccessful) {
                    signedIn = true
                    showToast(R.string.play_games_sign_in_success)
                    return@runOnUiThread
                }

                signedIn = false
                val exception = task.exception
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    if (statusCode == 12501 || statusCode == 12502) {
                        return@runOnUiThread
                    }
                }

                showAlert(activity.getString(R.string.play_games_sign_in_failed))
            }
        }
    }

    fun showLeaderboards(leaderboardId: String) {
        runOnUiThread {
            if (!signedIn) {
                showAlert(activity.getString(R.string.leaderboards_not_available))
                signIn()
                return@runOnUiThread
            }

            if (leaderboardId.isEmpty()) {
                leaderboardsClient.allLeaderboardsIntent
                    .addOnSuccessListener { intent -> activity.startActivity(intent) }
                    .addOnFailureListener {
                        showAlert(activity.getString(R.string.play_games_leaderboards_failed))
                    }
            } else {
                leaderboardsClient.getLeaderboardIntent(leaderboardId)
                    .addOnSuccessListener { intent -> activity.startActivity(intent) }
                    .addOnFailureListener {
                        showAlert(activity.getString(R.string.play_games_leaderboards_failed))
                    }
            }
        }
    }

    fun showAchievements() {
        runOnUiThread {
            if (!signedIn) {
                showAlert(activity.getString(R.string.achievements_not_available))
                signIn()
                return@runOnUiThread
            }

            achievementsClient.achievementsIntent
                .addOnSuccessListener { intent -> activity.startActivity(intent) }
                .addOnFailureListener {
                    showAlert(activity.getString(R.string.play_games_achievements_failed))
                }
        }
    }

    fun submitScore(leaderboardId: String, score: Long) {
        if (!signedIn) {
            return
        }

        try {
            leaderboardsClient.submitScore(leaderboardId, score)
            runOnUiThread { showToast(R.string.saved_toast) }
        } catch (e: RuntimeException) {
            runOnUiThread { showAlert(activity.getString(R.string.play_games_score_failed)) }
        }
    }

    fun unlockAchievement(achievementId: String) {
        if (!signedIn) {
            return
        }

        try {
            achievementsClient.unlock(achievementId)
        } catch (e: RuntimeException) {
            runOnUiThread { showToast(R.string.play_games_achievement_failed) }
        }
    }

    private fun runOnUiThread(action: Runnable) {
        activity.runOnUiThread(action)
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(activity)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(activity.applicationContext, messageResId, Toast.LENGTH_SHORT).show()
    }
}
