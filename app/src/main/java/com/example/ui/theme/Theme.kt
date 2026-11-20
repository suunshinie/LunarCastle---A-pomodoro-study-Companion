package com.example.ui.theme

import android.provider.Settings
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

enum class CastleThemeMode(val title: String, val subtitle: String) {
    MOONLIT_CASTLE("Moonlit Castle", "Midnight indigo, lavender & candlelight gold"),
    DAYLIGHT_LIBRARY("Daylight Library", "Warm archival cream & sunlit stone")
}

@Immutable
data class LunarExtraColors(
    val isDark: Boolean,
    val canvas: Color,
    val canvasDeep: Color,
    val cardSurface: Color,
    val cardElevated: Color,
    val cardBorder: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedMetaText: Color,
    val lavender: Color,
    val deepLavender: Color,
    val gold: Color,
    val lightGold: Color,
    val goldButtonText: Color,
    val cream: Color,
    val creamSurface: Color,
    val midnight: Color,
    val rimHighlight: Color,
    val lavenderWash: Color,
    val goldWash: Color,
    val goldHalo: Color,
    val mistColor: Color,
    val silhouetteColor: Color
)

private val MoonlitCastleExtraColors = LunarExtraColors(
    isDark = true,
    canvas = Midnight,
    canvasDeep = MidnightDeep,
    cardSurface = MidnightSurface,
    cardElevated = MidnightElevated,
    cardBorder = MidnightBorder,
    primaryText = Cream,
    secondaryText = CreamSurface,
    mutedMetaText = Lavender,
    lavender = Lavender,
    deepLavender = DeepLavender,
    gold = ChampagneGold,
    lightGold = LightGold,
    goldButtonText = Midnight,
    cream = Cream,
    creamSurface = CreamSurface,
    midnight = Midnight,
    rimHighlight = RimHighlightGold,
    lavenderWash = LavenderWash,
    goldWash = GoldWash,
    goldHalo = GoldHalo,
    mistColor = Lavender.copy(alpha = 0.09f),
    silhouetteColor = MidnightSurface.copy(alpha = 0.65f)
)

private val DaylightLibraryExtraColors = LunarExtraColors(
    isDark = false,
    canvas = Cream,
    canvasDeep = CreamSurface,
    cardSurface = CreamSurface,
    cardElevated = Color(0xFFFAF3E8),
    cardBorder = DaylightBorder,
    primaryText = Midnight,
    secondaryText = DaylightSecondaryInk,
    mutedMetaText = DeepLavender,
    lavender = DeepLavender,
    deepLavender = DeepLavender,
    gold = ChampagneGold,
    lightGold = LightGold,
    goldButtonText = Midnight,
    cream = Cream,
    creamSurface = CreamSurface,
    midnight = Midnight,
    rimHighlight = RimHighlightDaylight,
    lavenderWash = DeepLavender.copy(alpha = 0.14f),
    goldWash = ChampagneGold.copy(alpha = 0.25f),
    goldHalo = GoldHalo,
    mistColor = ChampagneGold.copy(alpha = 0.12f),
    silhouetteColor = DaylightBorder.copy(alpha = 0.45f)
)

val LocalLunarColors = staticCompositionLocalOf { MoonlitCastleExtraColors }
val LocalReducedMotion = staticCompositionLocalOf { false }

private val MoonlitCastleColorScheme = darkColorScheme(
    primary = ChampagneGold,
    onPrimary = Midnight,
    primaryContainer = Lavender,
    onPrimaryContainer = Midnight,
    secondary = Lavender,
    onSecondary = Midnight,
    secondaryContainer = DeepLavender,
    onSecondaryContainer = Cream,
    tertiary = LightGold,
    onTertiary = Midnight,
    background = Midnight,
    onBackground = Cream,
    surface = Midnight,
    onSurface = Cream,
    surfaceVariant = MidnightSurface,
    onSurfaceVariant = CreamSurface,
    surfaceContainerLowest = MidnightDeep,
    surfaceContainerLow = Midnight,
    surfaceContainer = MidnightSurface,
    surfaceContainerHigh = MidnightElevated,
    surfaceContainerHighest = MidnightBorder,
    outline = DeepLavender,
    outlineVariant = MidnightBorder,
    error = ErrorCoralDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

private val DaylightLibraryColorScheme = lightColorScheme(
    primary = ChampagneGold,
    onPrimary = Midnight,
    primaryContainer = DeepLavender,
    onPrimaryContainer = Cream,
    secondary = DeepLavender,
    onSecondary = Cream,
    secondaryContainer = CreamSurface,
    onSecondaryContainer = Midnight,
    tertiary = DaylightGoldInk,
    onTertiary = Cream,
    background = Cream,
    onBackground = Midnight,
    surface = Cream,
    onSurface = Midnight,
    surfaceVariant = CreamSurface,
    onSurfaceVariant = DaylightSecondaryInk,
    surfaceContainerLowest = Cream,
    surfaceContainerLow = Cream,
    surfaceContainer = CreamSurface,
    surfaceContainerHigh = Color(0xFFEAE1D2),
    surfaceContainerHighest = DaylightBorder,
    outline = DeepLavender,
    outlineVariant = DaylightBorder,
    error = ErrorBrickLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

val LunarShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun LunarCastleTheme(
    themeMode: CastleThemeMode = CastleThemeMode.MOONLIT_CASTLE,
    reducedMotionPreference: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemReducedMotion = remember(context) {
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        } catch (_: Exception) {
            false
        }
    }
    val effectiveReducedMotion = reducedMotionPreference || systemReducedMotion

    val isDark = themeMode == CastleThemeMode.MOONLIT_CASTLE
    val extraColors = if (isDark) MoonlitCastleExtraColors else DaylightLibraryExtraColors
    val colorScheme = if (isDark) MoonlitCastleColorScheme else DaylightLibraryColorScheme

    CompositionLocalProvider(
        LocalLunarColors provides extraColors,
        LocalReducedMotion provides effectiveReducedMotion
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LunarTypography,
            shapes = LunarShapes,
            content = content
        )
    }
}
