package com.aocc.majorproject.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PanelBackground = Color(0xB3000000)
private val ColumnBackground = Color(0xFF444444)
private val Accent = Color(0xFFCCCCCC)
private val Selected = Color(0xFFFF5252)

@Composable
fun SettingsOverlayContent(
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onResume() },
    ) {
        if (showMenuButton) {
            TextButton(
                onClick = onMenu,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
            ) {
                Text("Menu", color = Color.White, fontSize = 18.sp)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { /* consume taps on the panel */ },
                shape = RoundedCornerShape(12.dp),
                color = PanelBackground,
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Settings",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        textAlign = TextAlign.Center,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        SettingsColumn(
                            title = "Sound",
                            modifier = Modifier.weight(0.35f),
                        ) {
                            IconToggleButton(
                                enabled = soundOn,
                                enabledIcon = Icons.AutoMirrored.Filled.VolumeUp,
                                disabledIcon = Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = "Sound effects",
                                onClick = onToggleSound,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            IconToggleButton(
                                enabled = musicOn,
                                enabledIcon = Icons.Filled.MusicNote,
                                disabledIcon = Icons.Filled.MusicOff,
                                contentDescription = "Music",
                                onClick = onToggleMusic,
                            )
                        }

                        SettingsColumn(
                            title = "Tilt Options",
                            modifier = Modifier.weight(0.65f),
                        ) {
                            TiltOptionRow(
                                label = "Flat",
                                selected = tiltMode == 1,
                                icon = Icons.Filled.Straighten,
                                onClick = onFlatTilt,
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            TiltOptionRow(
                                label = "Tilted",
                                selected = tiltMode == 2,
                                icon = Icons.Filled.ScreenRotation,
                                onClick = onTiltedTilt,
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            TiltOptionRow(
                                label = "Custom",
                                selected = tiltMode == 3,
                                icon = Icons.Filled.Tune,
                                onClick = onCustomTilt,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = prompt,
                color = Color.White,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SettingsColumn(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = ColumnBackground,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                color = Accent,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            content()
        }
    }
}

@Composable
private fun IconToggleButton(
    enabled: Boolean,
    enabledIcon: androidx.compose.ui.graphics.vector.ImageVector,
    disabledIcon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(if (enabled) Color(0xFF666666) else Color(0xFF333333)),
    ) {
        Icon(
            imageVector = if (enabled) enabledIcon else disabledIcon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
private fun TiltOptionRow(
    label: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (selected) Selected.copy(alpha = 0.25f) else Color(0xFF555555)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Selected else Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = if (selected) Selected else Color.White,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = if (selected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (selected) Selected else Color(0xFF888888),
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
fun MainMenuOverlayContent(
    loggedIn: Boolean,
    onPlay: () -> Unit,
    onTutorial: () -> Unit,
    onSignIn: () -> Unit,
    onLeaderboards: () -> Unit,
    onAchievements: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (!loggedIn) {
            TextButton(
                onClick = onSignIn,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
            ) {
                Text("Sign in", color = Color.White, fontSize = 16.sp)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MenuIconButton(label = "Leaderboards", onClick = onLeaderboards)
            MenuIconButton(label = "Achievements", onClick = onAchievements)
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 85.dp),
            horizontalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            MainMenuButton(label = "Play", onClick = onPlay)
            MainMenuButton(label = "Tutorial", onClick = onTutorial)
        }
    }
}

@Composable
private fun MainMenuButton(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xCC333333),
        modifier = Modifier
            .width(180.dp)
            .height(80.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun MenuIconButton(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(label, color = Color.White, fontSize = 14.sp)
    }
}

@Composable
fun GameMenuTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
