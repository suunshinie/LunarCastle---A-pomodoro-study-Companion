package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.TeaCatalog
import com.example.data.preferences.LunarUserPreferences
import com.example.ui.components.LunarCard
import com.example.ui.components.LunarChip
import com.example.ui.components.LunarSecondaryButton
import com.example.ui.theme.CastleThemeMode
import com.example.ui.theme.LocalLunarColors
import com.example.ui.theme.LunarTextStyles

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    userDisplayName: String,
    preferences: LunarUserPreferences,
    onSelectThemeMode: (CastleThemeMode) -> Unit,
    onToggleReducedMotion: (Boolean) -> Unit,
    onSelectDefaultFocusMinutes: (Int) -> Unit,
    onSelectDefaultTeaId: (String) -> Unit,
    onOpenMakeItYours: () -> Unit,
    onSignOutConfirmed: () -> Unit,
    onBack: () -> Unit
) {
    val lunar = LocalLunarColors.current
    var showSignOutDialog by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = lunar.primaryText
                        )
                    }
                    Column {
                        Text(
                            text = "LUNAR CASTLE · SETTINGS",
                            style = LunarTextStyles.StatusMono,
                            color = if (lunar.isDark) lunar.gold else lunar.deepLavender
                        )
                        Text(
                            text = "Atmosphere & Preferences",
                            style = MaterialTheme.typography.headlineMedium,
                            color = lunar.primaryText
                        )
                    }
                }
            }

            // Theme selector: Moonlit Castle vs Daylight Library
            item {
                LunarCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Text(
                        text = "THEME",
                        style = LunarTextStyles.StatusMono,
                        color = lunar.mutedMetaText
                    )
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CastleThemeMode.entries.forEach { mode ->
                            val selected = preferences.themeMode == mode
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectThemeMode(mode) }
                                    .testTag("settings_theme_${mode.name.lowercase()}"),
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) lunar.lavenderWash else lunar.canvas,
                                border = BorderStroke(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) lunar.gold else lunar.cardBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (mode == CastleThemeMode.MOONLIT_CASTLE) {
                                            Icons.Outlined.DarkMode
                                        } else {
                                            Icons.Outlined.LightMode
                                        },
                                        contentDescription = mode.title,
                                        tint = if (selected) lunar.gold else lunar.secondaryText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mode.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = lunar.primaryText
                                        )
                                        Text(
                                            text = mode.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = lunar.secondaryText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Accessibility: Reduced Motion
            item {
                LunarCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Text(
                        text = "MOTION & ACCESSIBILITY",
                        style = LunarTextStyles.StatusMono,
                        color = lunar.mutedMetaText
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Reduced Motion",
                                style = MaterialTheme.typography.titleMedium,
                                color = lunar.primaryText
                            )
                            Text(
                                text = "Stop moon bounce, twinkling stars, bouncing sparkles, and drifting mist.",
                                style = MaterialTheme.typography.bodySmall,
                                color = lunar.secondaryText
                            )
                        }
                        Switch(
                            checked = preferences.reducedMotion,
                            onCheckedChange = onToggleReducedMotion,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = lunar.midnight,
                                checkedTrackColor = lunar.gold,
                                uncheckedThumbColor = lunar.secondaryText,
                                uncheckedTrackColor = lunar.canvas
                            ),
                            modifier = Modifier.testTag("settings_reduced_motion_switch")
                        )
                    }
                }
            }

            // Study defaults
            item {
                LunarCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Text(
                        text = "MOON ROOM DEFAULTS",
                        style = LunarTextStyles.StatusMono,
                        color = lunar.mutedMetaText
                    )
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(25, 45, 60).forEach { mins ->
                            LunarChip(
                                label = "${mins}m Focus",
                                selected = preferences.defaultFocusMinutes == mins,
                                onClick = { onSelectDefaultFocusMinutes(mins) },
                                modifier = Modifier.testTag("settings_focus_$mins")
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "DEFAULT TEA BLEND",
                        style = LunarTextStyles.StatusMono,
                        color = lunar.mutedMetaText
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TeaCatalog.blends.forEach { blend ->
                            LunarChip(
                                label = blend.name,
                                selected = preferences.defaultTeaId == blend.id,
                                accentGold = true,
                                onClick = { onSelectDefaultTeaId(blend.id) },
                                modifier = Modifier.testTag("settings_tea_${blend.id}")
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    LunarSecondaryButton(
                        text = "Revisit Make it yours",
                        onClick = onOpenMakeItYours,
                        modifier = Modifier.testTag("settings_make_it_yours_button"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = "Make it yours",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Account card
            item {
                LunarCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Text(
                        text = "ACCOUNT & SYNC",
                        style = LunarTextStyles.StatusMono,
                        color = lunar.mutedMetaText
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = userDisplayName.ifBlank { "Scholar" },
                        style = MaterialTheme.typography.titleLarge,
                        color = lunar.primaryText
                    )
                    Text(
                        text = "Your study sessions, tables, books, quotes, and Secret Garden notes are synced with Google Sign-In.",
                        style = MaterialTheme.typography.bodySmall,
                        color = lunar.secondaryText
                    )
                    Spacer(Modifier.height(14.dp))
                    LunarSecondaryButton(
                        text = "Sign Out",
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier.testTag("settings_sign_out_button"),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = "Sign out",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = lunar.cardSurface,
            titleContentColor = lunar.primaryText,
            textContentColor = lunar.secondaryText,
            title = { Text("Leave Lunar Castle?", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    "Your Moon Room sessions, milestones, library, and Secret Garden notes remain safely synced to your Google account.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        onSignOutConfirmed()
                    },
                    modifier = Modifier.testTag("confirm_sign_out_button")
                ) {
                    Text("Sign Out", color = lunar.gold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Stay", color = lunar.secondaryText)
                }
            }
        )
    }
}
