package com.aocc.majorproject.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.text.TextStyle
import com.aocc.majorproject.Assets
import com.aocc.majorproject.ui.MainMenuLayout
import com.aocc.majorproject.ui.UiBounds
import com.aocc.majorproject.ui.UiButton

private val OuterPanelColor = Color(0xB3000000)
private val InnerPanelColor = Color(0xFF444444)
private val MenuButtonColor = Color(0xFF444444)
private val MenuButtonPressedColor = Color(0xFFC3C3C3)

@Composable
fun SettingsOverlayContent(
    viewport: ViewportMetrics,
    prompt: String,
    showMenuButton: Boolean,
    soundOn: Boolean,
    musicOn: Boolean,
    tiltMode: Int,
    onResume: () -> Unit,
    onMenu: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit,
    onFlatTilt: () -> Unit,
    onTiltedTilt: () -> Unit,
    onCustomTilt: () -> Unit,
) {
    val fontFamily = rememberGameTypeface()
    val panelBounds = SettingsLayout.outerPanel
    val fontSizeTitle = worldTextSize(SettingsLayout.titleTextSize)
    val fontSizePrompt = worldTextSize(SettingsLayout.promptTextSize)
    val fontSizeTiltLabel = worldTextSize(30f)
    val fontSizeMenu = worldTextSize(UiButton.MENU_TEXT_SIZE)

    GameWorldOverlay(viewport = viewport) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onResume() },
        ) {
            WorldPanel(
                bounds = panelBounds,
                background = OuterPanelColor,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { /* absorb taps on the panel */ },
            )

            WorldCenteredText(
                text = "Settings",
                centerX = panelBounds.centerX(),
                centerY = SettingsLayout.settingsTitleCenterY,
                fontSize = fontSizeTitle,
                fontFamily = fontFamily,
            )

            val soundPanel = SettingsLayout.soundPanel
            WorldPanel(bounds = soundPanel, background = InnerPanelColor)
            WorldCenteredText(
                text = "Sound",
                centerX = soundPanel.centerX(),
                centerY = SettingsLayout.soundTitleCenterY,
                fontSize = fontSizeTitle,
                fontFamily = fontFamily,
            )

            val tiltPanel = SettingsLayout.tiltPanel
            WorldPanel(bounds = tiltPanel, background = InnerPanelColor)
            WorldCenteredText(
                text = "Tilt Options",
                centerX = tiltPanel.centerX(),
                centerY = SettingsLayout.tiltTitleCenterY,
                fontSize = fontSizeTitle,
                fontFamily = fontFamily,
            )

            WorldClickTarget(bounds = SettingsLayout.soundIcon, onClick = onToggleSound) {
                GameImage(
                    image = if (soundOn) Assets.sound else Assets.sound_muted,
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = "Sound effects",
                )
            }

            WorldClickTarget(bounds = SettingsLayout.musicIcon, onClick = onToggleMusic) {
                GameImage(
                    image = if (musicOn) Assets.music else Assets.music_muted,
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = "Music",
                )
            }

            TiltOption(
                bounds = SettingsLayout.flatTiltButton,
                label = "Flat",
                selected = tiltMode == 1,
                selectedImage = Assets.tilt_control_flat_2,
                defaultImage = Assets.tilt_control_flat,
                fontSize = fontSizeTiltLabel,
                fontFamily = fontFamily,
                onClick = onFlatTilt,
            )
            TiltOption(
                bounds = SettingsLayout.tiltedTiltButton,
                label = "Tilted",
                selected = tiltMode == 2,
                selectedImage = Assets.tilt_control_tilted_2,
                defaultImage = Assets.tilt_control_tilted,
                fontSize = fontSizeTiltLabel,
                fontFamily = fontFamily,
                onClick = onTiltedTilt,
            )
            TiltOption(
                bounds = SettingsLayout.customTiltButton,
                label = "Custom",
                selected = tiltMode == 3,
                selectedImage = Assets.tilt_control_custom,
                defaultImage = Assets.tilt_control_custom,
                showSelectionRing = true,
                fontSize = fontSizeTiltLabel,
                fontFamily = fontFamily,
                onClick = onCustomTilt,
            )

            WorldCenteredText(
                text = prompt,
                centerX = panelBounds.centerX(),
                centerY = SettingsLayout.promptCenterY,
                fontSize = fontSizePrompt,
                fontFamily = fontFamily,
            )

            if (showMenuButton) {
                GameMenuButton(
                    bounds = SettingsLayout.menuButton,
                    label = "Menu",
                    fontSize = fontSizeMenu,
                    fontFamily = fontFamily,
                    onClick = onMenu,
                )
            }
        }
    }
}

@Composable
fun MainMenuOverlayContent(
    viewport: ViewportMetrics,
    loggedIn: Boolean,
    onPlay: () -> Unit,
    onTutorial: () -> Unit,
    onSignIn: () -> Unit,
    onLeaderboards: () -> Unit,
    onAchievements: () -> Unit,
) {
    GameWorldOverlay(viewport = viewport) {
        Box(modifier = Modifier.fillMaxSize()) {
            WorldClickTarget(bounds = MainMenuLayout.playButton(), onClick = onPlay)
            WorldClickTarget(bounds = MainMenuLayout.tutorialButton(), onClick = onTutorial)

            WorldClickTarget(
                bounds = MainMenuLayout.leaderboardsButton(),
                onClick = onLeaderboards,
            ) {
                GameImage(
                    image = Assets.gpg_icon_leaderboards,
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = "Leaderboards",
                )
            }

            WorldClickTarget(
                bounds = MainMenuLayout.achievementsButton(),
                onClick = onAchievements,
            ) {
                GameImage(
                    image = Assets.gpg_icon_achievements,
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = "Achievements",
                )
            }

            if (!loggedIn) {
                SignInButton(
                    bounds = MainMenuLayout.signInButton(),
                    onClick = onSignIn,
                )
            }
        }
    }
}

@Composable
private fun WorldPanel(
    bounds: UiBounds,
    background: Color,
    modifier: Modifier = Modifier,
) {
    WorldBox(bounds = bounds, modifier = modifier.background(background)) {}
}

@Composable
private fun WorldClickTarget(
    bounds: UiBounds,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    WorldBox(
        bounds = bounds,
        modifier = modifier.clickable(onClick = onClick),
        content = content,
    )
}

@Composable
private fun TiltOption(
    bounds: UiBounds,
    label: String,
    selected: Boolean,
    selectedImage: com.aocc.framework.Image?,
    defaultImage: com.aocc.framework.Image?,
    showSelectionRing: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    onClick: () -> Unit,
) {
    WorldClickTarget(bounds = bounds, onClick = onClick) {
        if (selected && showSelectionRing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, Color.Red, CircleShape),
            )
        }
        GameImage(
            image = if (selected) selectedImage else defaultImage,
            modifier = Modifier.fillMaxSize(),
            contentDescription = label,
        )
    }

    val labelBounds = UiBounds(
        bounds.x + bounds.width + SettingsLayout.tiltLabelOffsetX,
        bounds.y,
        160,
        bounds.height,
    )
    WorldBox(bounds = labelBounds) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
            BasicText(
                text = label,
                style = TextStyle(
                    color = Color.Black,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                ),
            )
        }
    }
}

@Composable
private fun GameMenuButton(
    bounds: UiBounds,
    label: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed = interactionSource.collectIsPressedAsState().value

    WorldBox(
        bounds = bounds,
        modifier = Modifier
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .background(if (pressed) MenuButtonPressedColor else MenuButtonColor),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            BasicText(
                text = label,
                style = TextStyle(
                    color = Color.White,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

@Composable
private fun SignInButton(
    bounds: UiBounds,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed = interactionSource.collectIsPressedAsState().value

    WorldClickTarget(bounds = bounds, onClick = onClick) {
        GameImage(
            image = if (pressed) Assets.sign_in_press else Assets.sign_in_base,
            modifier = Modifier.fillMaxSize(),
            contentDescription = "Sign in",
        )
    }
}

@Composable
private fun WorldCenteredText(
    text: String,
    centerX: Int,
    centerY: Int,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
) {
    val bounds = UiBounds(centerX - 250, centerY - 24, 500, 48)
    WorldBox(bounds = bounds) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            BasicText(
                text = text,
                style = TextStyle(
                    color = Color.White,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}
