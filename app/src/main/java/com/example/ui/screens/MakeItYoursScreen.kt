package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.components.CelestialBackdrop
import com.example.ui.components.LunarCard
import com.example.ui.components.LunarChip
import com.example.ui.components.LunarMoonCrest
import com.example.ui.components.LunarPrimaryButton
import com.example.ui.theme.CastleThemeMode
import com.example.ui.theme.Cream
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.LocalLunarColors
import com.example.ui.theme.LunarTextStyles
import com.example.ui.theme.Midnight
import com.example.ui.theme.MidnightSurface

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MakeItYoursScreen(
    currentPreferences: LunarUserPreferences,
    onPreviewThemeChange: (CastleThemeMode) -> Unit,
    onPreviewReducedMotionChange: (Boolean) -> Unit,
    onCompleteSetup: (
        themeMode: CastleThemeMode,
        focusMinutes: Int,
        teaId: String,
        reducedMotion: Boolean
    ) -> Unit
) {
    val lunar = LocalLunarColors.current
    var selectedTheme by remember(currentPreferences.themeMode) {
        mutableStateOf(currentPreferences.themeMode)
    }
    var selectedFocusMinutes by remember(currentPreferences.defaultFocusMinutes) {
        mutableIntStateOf(currentPreferences.defaultFocusMinutes)
    }
    var selectedTeaId by remember(currentPreferences.defaultTeaId) {
        mutableStateOf(currentPreferences.defaultTeaId)
    }
    var reducedMotion by remember(currentPreferences.reducedMotion) {
        mutableStateOf(currentPreferences.reducedMotion)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        CelestialBackdrop(showCastleSilhouette = true)

        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LunarMoonCrest(size = 48.dp, animateBounce = false)
                Column {
                    Text(
                        text = "MAKE IT YOURS",
                        style = LunarTextStyles.StatusMono,
                        color = if (lunar.isDark) lunar.gold else lunar.deepLavender
                    )
                    Text(
                        text = "Shape Your Castle",
                        style = MaterialTheme.typography.headlineMedium,
                        color = lunar.primaryText
                    )
                }
            }

            Text(
                text = "Choose how Lunar Castle feels before you settle at your desk. You can change any of these anytime in Settings.",
                style = LunarTextStyles.TaglineItalic,
                color = lunar.secondaryText
            )

            // Theme picker
            LunarCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(18.dp)
            ) {
                Text(
                    text = "CASTLE ATMOSPHERE",
                    style = LunarTextStyles.StatusMono,
                    color = lunar.mutedMetaText
                )
                Spacer(Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CastleThemeMode.entries.forEach { mode ->
                        val isSelected = selectedTheme == mode
                        val swatchBg = if (mode == CastleThemeMode.MOONLIT_CASTLE) MidnightSurface else CreamSurface
                        val swatchDot = if (mode == CastleThemeMode.MOONLIT_CASTLE) Midnight else Cream
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedTheme = mode
                                    onPreviewThemeChange(mode)
                                }
                                .testTag("theme_option_${mode.name.lowercase()}"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) lunar.lavenderWash else lunar.canvas,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) lunar.gold else lunar.cardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(swatchBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (mode == CastleThemeMode.MOONLIT_CASTLE) {
                                            Icons.Outlined.DarkMode
                                        } else {
                                            Icons.Outlined.LightMode
                                        },
                                        contentDescription = mode.title,
                                        tint = if (mode == CastleThemeMode.MOONLIT_CASTLE) lunar.lightGold else Midnight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
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

            // Default Focus Sprint
            LunarCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(18.dp)
            ) {
                Text(
                    text = "MOON ROOM DEFAULT TIMER",
                    style = LunarTextStyles.StatusMono,
                    color = lunar.mutedMetaText
                )
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(25 to "Deep Study", 45 to "Long Chapter", 60 to "Manuscript").forEach { (mins, desc) ->
                        LunarChip(
                            label = "${mins}m",
                            subLabel = desc,
                            selected = selectedFocusMinutes == mins,
                            onClick = { selectedFocusMinutes = mins },
                            modifier = Modifier.testTag("onboarding_focus_$mins")
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "FAVORITE TEA STEEP",
                    style = LunarTextStyles.StatusMono,
                    color = lunar.mutedMetaText
                )
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TeaCatalog.blends.forEach { blend ->
                        LunarChip(
                            label = blend.name,
                            subLabel = "${blend.steepMinutes}m",
                            selected = selectedTeaId == blend.id,
                            accentGold = true,
                            onClick = { selectedTeaId = blend.id },
                            modifier = Modifier.testTag("onboarding_tea_${blend.id}")
                        )
                    }
                }
            }

            // Reduced Motion accessibility toggle
            LunarCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(18.dp)
            ) {
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
                            text = "Pause moon bounce, twinkling stars, sparkles, and mist animations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = lunar.secondaryText
                        )
                    }
                    Switch(
                        checked = reducedMotion,
                        onCheckedChange = { enabled ->
                            reducedMotion = enabled
                            onPreviewReducedMotionChange(enabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = lunar.midnight,
                            checkedTrackColor = lunar.gold,
                            uncheckedThumbColor = lunar.secondaryText,
                            uncheckedTrackColor = lunar.canvas
                        ),
                        modifier = Modifier.testTag("onboarding_reduced_motion_switch")
                    )
                }
            }

            LunarPrimaryButton(
                text = "Enter Lunar Castle",
                onClick = {
                    onCompleteSetup(
                        selectedTheme,
                        selectedFocusMinutes,
                        selectedTeaId,
                        reducedMotion
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("complete_onboarding_button")
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}
