package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FocusPreset
import com.example.data.model.FocusSession
import com.example.data.model.SoundscapeChannel
import com.example.data.model.TeaCatalog
import com.example.ui.components.CelestialTimerDial
import com.example.ui.components.ConstellationDivider
import com.example.ui.components.LunarCard
import com.example.ui.components.LunarChip
import com.example.ui.components.LunarPrimaryButton
import com.example.ui.components.LunarSecondaryButton
import com.example.ui.state.UiState
import com.example.ui.theme.LocalLunarColors
import com.example.ui.theme.LunarTextStyles
import com.example.ui.viewmodel.FocusViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusScreen(
    viewModel: FocusViewModel,
    onShowSnackbar: (String) -> Unit
) {
    val lunar = LocalLunarColors.current
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val sessionsState by viewModel.sessionsState.collectAsStateWithLifecycle()

    LaunchedEffect(timerState.feedbackMessage) {
        val msg = timerState.feedbackMessage
        if (!msg.isNullOrBlank()) {
            onShowSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    val totalFocusMinutes = when (val s = sessionsState) {
        is UiState.Success -> s.data.sumOf { it.durationMinutes }
        else -> 0L
    }
    val totalCups = when (val s = sessionsState) {
        is UiState.Success -> s.data.size
        else -> 0
    }

    var intentionFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header stats strip
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MOON ROOM · FOCUS",
                            style = LunarTextStyles.StatusMono,
                            color = if (lunar.isDark) lunar.gold else lunar.deepLavender
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Moon Room & Tea Steep",
                            style = MaterialTheme.typography.headlineMedium,
                            color = lunar.primaryText
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = lunar.cardSurface,
                        border = BorderStroke(1.dp, lunar.cardBorder)
                    ) {
                        Text(
                            text = "${totalFocusMinutes}m · $totalCups cups",
                            style = LunarTextStyles.StatusMono,
                            color = if (lunar.isDark) lunar.lightGold else lunar.midnight,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Celestial Pomodoro Timer LunarCard
            item {
                LunarCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(22.dp)
                ) {
                    Text(
                        text = "SESSION INTENTION",
                        style = LunarTextStyles.StatusMono,
                        color = lunar.mutedMetaText
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(lunar.canvas)
                            .border(
                                width = if (intentionFocused) 2.dp else 1.dp,
                                color = if (intentionFocused) lunar.gold else lunar.cardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (timerState.intentionLabel.isEmpty()) {
                            Text(
                                text = "Name your study chapter or essay...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = lunar.mutedMetaText
                            )
                        }
                        BasicTextField(
                            value = timerState.intentionLabel,
                            onValueChange = viewModel::updateIntentionLabel,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                color = lunar.primaryText
                            ),
                            cursorBrush = SolidColor(lunar.gold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { intentionFocused = it.isFocused }
                                .testTag("session_intention_input")
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Preset duration pills
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FocusPreset.entries.forEach { preset ->
                            LunarChip(
                                label = "${preset.minutes}m",
                                subLabel = if (preset.isBreak) "Break" else "Focus",
                                selected = timerState.selectedPreset == preset,
                                accentGold = preset.isBreak,
                                onClick = { viewModel.selectPreset(preset) },
                                modifier = Modifier.testTag("preset_${preset.minutes}")
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Celestial Circular Dial
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CelestialTimerDial(
                            formattedTime = formatSeconds(timerState.remainingSeconds),
                            progressFraction = timerState.progressFraction,
                            phaseLabel = timerState.selectedPreset.label,
                            isRunning = timerState.isRunning
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    // Primary Focus Triggers (Gold CTA with Midnight text)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LunarPrimaryButton(
                            text = if (timerState.isRunning) "Pause" else "Begin Focus",
                            onClick = viewModel::toggleFocusTimer,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("focus_toggle_button"),
                            leadingIcon = {
                                Icon(
                                    imageVector = if (timerState.isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (timerState.isRunning) "Pause timer" else "Start timer",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        LunarSecondaryButton(
                            text = "Reset",
                            onClick = viewModel::resetFocusTimer,
                            modifier = Modifier.testTag("focus_reset_button"),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = "Reset timer",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    LunarSecondaryButton(
                        text = if (timerState.isLoggingSession) "Archiving..." else "Complete & Log Session",
                        enabled = !timerState.isLoggingSession,
                        onClick = viewModel::completeAndLogNow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("complete_session_button"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = "Complete and log session",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            // Herbal Tea Brew Tracker Card
            item {
                LunarCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalCafe,
                                contentDescription = "Tea steep tracker",
                                tint = if (lunar.isDark) lunar.gold else lunar.deepLavender,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "TEA STEEP TRACKER",
                                    style = LunarTextStyles.StatusMono,
                                    color = if (lunar.isDark) lunar.gold else lunar.deepLavender
                                )
                                Text(
                                    text = timerState.selectedTea.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = lunar.primaryText
                                )
                            }
                        }

                        Text(
                            text = if (timerState.steepReady) {
                                "READY"
                            } else {
                                formatSeconds(timerState.steepRemainingSeconds)
                            },
                            style = LunarTextStyles.TimerCompact,
                            color = if (timerState.steepReady) {
                                lunar.lavender
                            } else if (lunar.isDark) {
                                lunar.lightGold
                            } else {
                                lunar.midnight
                            }
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${timerState.selectedTea.tastingNote} · ${timerState.selectedTea.steepMinutes}m @ ${timerState.selectedTea.temperatureCelsius}°C",
                        style = MaterialTheme.typography.bodySmall,
                        color = lunar.secondaryText
                    )

                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { timerState.steepProgressFraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = lunar.gold,
                        trackColor = lunar.cardBorder
                    )

                    Spacer(Modifier.height(14.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TeaCatalog.blends.forEach { blend ->
                            LunarChip(
                                label = blend.name,
                                subLabel = "${blend.steepMinutes}m",
                                selected = timerState.selectedTea.id == blend.id,
                                accentGold = true,
                                onClick = { viewModel.selectTea(blend) },
                                modifier = Modifier.testTag("tea_chip_${blend.id}")
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LunarPrimaryButton(
                            text = when {
                                timerState.isSteeping -> "Pause Steep"
                                timerState.steepReady -> "Re-Steep"
                                else -> "Start Steep"
                            },
                            onClick = viewModel::toggleSteepTimer,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("steep_toggle_button")
                        )
                        LunarSecondaryButton(
                            text = "Reset Cup",
                            onClick = viewModel::resetSteepTimer,
                            modifier = Modifier.testTag("steep_reset_button")
                        )
                    }
                }
            }

            // Ambient Soundscape Mixer
            item {
                LunarCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.GraphicEq,
                                contentDescription = "Ambient soundscapes",
                                tint = lunar.lavender,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "AMBIENT SOUNDSCAPE",
                                    style = LunarTextStyles.StatusMono,
                                    color = lunar.mutedMetaText
                                )
                                Text(
                                    text = "Moon Room Acoustics",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = lunar.primaryText
                                )
                            }
                        }

                        if (timerState.activeSoundscapes.isNotEmpty()) {
                            IconButton(
                                onClick = viewModel::muteAllSoundscapes,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("mute_soundscapes_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.VolumeOff,
                                    contentDescription = "Mute all soundscapes",
                                    tint = lunar.gold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SoundscapeChannel.entries.forEach { channel ->
                            LunarChip(
                                label = channel.title,
                                subLabel = channel.subtitle,
                                selected = channel in timerState.activeSoundscapes,
                                onClick = { viewModel.toggleSoundscape(channel) },
                                modifier = Modifier.testTag("soundscape_${channel.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            item { ConstellationDivider() }

            // Session History Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Archived Moon Room Sessions",
                        style = MaterialTheme.typography.headlineSmall,
                        color = lunar.primaryText
                    )
                    Text(
                        text = "$totalCups LOGGED",
                        style = LunarTextStyles.StatusMono,
                        color = lunar.mutedMetaText
                    )
                }
            }

            when (val state = sessionsState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = lunar.gold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        LunarCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        item {
                            LunarCard(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(20.dp)
                            ) {
                                Text(
                                    text = "No completed focus sprints yet.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = lunar.primaryText
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Start the Moon Room timer or tap Complete & Log Session to record your study sessions with tea pairings.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = lunar.secondaryText
                                )
                            }
                        }
                    } else {
                        items(state.data, key = { it.id }) { session ->
                            FocusSessionHistoryCard(
                                session = session,
                                onDelete = { viewModel.deleteSession(session.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusSessionHistoryCard(
    session: FocusSession,
    onDelete: () -> Unit
) {
    val lunar = LocalLunarColors.current
    LunarCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("session_card_${session.id}"),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.label.ifBlank { "Moonlit Study" },
                    style = MaterialTheme.typography.titleMedium,
                    color = lunar.primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = buildString {
                        append("${session.durationMinutes}m focus")
                        if (session.teaName.isNotBlank()) {
                            append(" · ${session.teaName}")
                        }
                        if (session.soundscapes.isNotEmpty()) {
                            append(" · ${session.soundscapes.joinToString(", ")}")
                        }
                    },
                    style = LunarTextStyles.StatusMono,
                    color = lunar.mutedMetaText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("delete_session_${session.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete session",
                    tint = lunar.secondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val clamped = totalSeconds.coerceAtLeast(0)
    val mins = clamped / 60
    val secs = clamped % 60
    return "%02d:%02d".format(mins, secs)
}
