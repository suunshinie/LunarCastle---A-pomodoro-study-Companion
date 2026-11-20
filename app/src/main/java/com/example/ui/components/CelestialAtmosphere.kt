package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalLunarColors
import com.example.ui.theme.LocalReducedMotion
import kotlin.math.PI
import kotlin.math.sin

/**
 * Displays the Lunar Castle royal-beige crescent moon crest with champagne-gold moonlight glow
 * directly from the bundled vector asset, with optional gentle bounce paused under reduced motion.
 */
@Composable
fun LunarMoonCrest(
    size: Dp,
    modifier: Modifier = Modifier,
    animateBounce: Boolean = false,
    contentDescription: String? = "Lunar Castle crescent moon crest"
) {
    val reducedMotion = LocalReducedMotion.current
    val bounceDp = if (animateBounce && !reducedMotion) {
        val transition = rememberInfiniteTransition(label = "moon_bounce")
        val y by transition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "moon_bounce_y"
        )
        y.dp
    } else {
        0.dp
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_lunar_moon_crest),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .offset(y = bounceDp)
        )
    }
}

private data class StarSpec(
    val xFraction: Float,
    val yFraction: Float,
    val radiusDp: Float,
    val phase: Float,
    val isGold: Boolean,
    val isSparkle: Boolean
)

private val STARFIELD = listOf(
    StarSpec(0.10f, 0.12f, 1.8f, 0.0f, true, false),
    StarSpec(0.24f, 0.08f, 3.6f, 0.9f, false, true),
    StarSpec(0.41f, 0.14f, 1.4f, 1.8f, false, false),
    StarSpec(0.62f, 0.07f, 2.2f, 2.5f, true, false),
    StarSpec(0.78f, 0.13f, 4.0f, 3.1f, true, true),
    StarSpec(0.90f, 0.20f, 1.5f, 0.6f, false, false),
    StarSpec(0.16f, 0.28f, 3.4f, 2.1f, false, true),
    StarSpec(0.34f, 0.24f, 1.5f, 3.7f, false, false),
    StarSpec(0.70f, 0.27f, 1.7f, 1.4f, true, false),
    StarSpec(0.84f, 0.34f, 3.2f, 4.4f, true, true),
    StarSpec(0.08f, 0.44f, 1.4f, 2.9f, false, false),
    StarSpec(0.55f, 0.20f, 1.3f, 5.0f, false, false),
    StarSpec(0.92f, 0.48f, 1.6f, 1.1f, true, false),
    StarSpec(0.20f, 0.56f, 1.4f, 3.3f, false, false)
)

/**
 * Full-bleed atmospheric backdrop featuring twinkling stars, bouncing sparkles,
 * ethereal mist bands, and a faint distant castle silhouette. Freezes cleanly when Reduced Motion is active.
 */
@Composable
fun CelestialBackdrop(
    modifier: Modifier = Modifier,
    showCastleSilhouette: Boolean = true
) {
    val lunar = LocalLunarColors.current
    val reducedMotion = LocalReducedMotion.current

    val twinkleClock: Float
    val mistClock: Float
    if (!reducedMotion) {
        val transition = rememberInfiniteTransition(label = "celestial_backdrop")
        twinkleClock = transition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 5200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "twinkle_clock"
        ).value
        mistClock = transition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 7600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "mist_clock"
        ).value
    } else {
        twinkleClock = 0.8f
        mistClock = 0f
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Base canvas sky gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(lunar.canvasDeep, lunar.canvas, lunar.canvas)
            )
        )

        // Ethereal mist plumes
        val mistShift = mistClock * w * 0.06f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(lunar.mistColor, Color.Transparent),
                center = Offset(w * 0.25f + mistShift, h * 0.68f),
                radius = w * 0.65f
            ),
            center = Offset(w * 0.25f + mistShift, h * 0.68f),
            radius = w * 0.65f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(lunar.goldWash.copy(alpha = if (lunar.isDark) 0.08f else 0.10f), Color.Transparent),
                center = Offset(w * 0.78f - mistShift, h * 0.52f),
                radius = w * 0.55f
            ),
            center = Offset(w * 0.78f - mistShift, h * 0.52f),
            radius = w * 0.55f
        )

        // Twinkling stars & bouncing sparkles
        val starVisibilityMultiplier = if (lunar.isDark) 1f else 0.42f
        STARFIELD.forEach { star ->
            val wave = 0.45f + 0.55f * ((sin(twinkleClock + star.phase) + 1f) / 2f)
            val baseColor = if (star.isGold) lunar.lightGold else lunar.cream
            val cx = w * star.xFraction
            val bounceY = if (star.isSparkle && !reducedMotion) {
                sin(twinkleClock * 1.4f + star.phase) * 3.dp.toPx()
            } else 0f
            val cy = h * star.yFraction + bounceY

            if (star.isSparkle) {
                drawFourPointSparkle(
                    center = Offset(cx, cy),
                    radius = star.radiusDp.dp.toPx() * (0.85f + 0.25f * wave),
                    color = baseColor.copy(alpha = wave * 0.85f * starVisibilityMultiplier)
                )
            } else {
                drawCircle(
                    color = baseColor.copy(alpha = wave * 0.78f * starVisibilityMultiplier),
                    radius = star.radiusDp.dp.toPx(),
                    center = Offset(cx, cy)
                )
            }
        }

        // Faint distant castle turret silhouette along lower horizon
        if (showCastleSilhouette) {
            val baseY = h * 0.94f
            val castlePath = Path().apply {
                moveTo(0f, h)
                lineTo(0f, baseY - 26.dp.toPx())
                lineTo(w * 0.08f, baseY - 26.dp.toPx())
                lineTo(w * 0.08f, baseY - 62.dp.toPx())
                lineTo(w * 0.12f, baseY - 88.dp.toPx()) // Left spire
                lineTo(w * 0.16f, baseY - 62.dp.toPx())
                lineTo(w * 0.16f, baseY - 38.dp.toPx())
                // Battlements
                lineTo(w * 0.28f, baseY - 38.dp.toPx())
                lineTo(w * 0.28f, baseY - 48.dp.toPx())
                lineTo(w * 0.34f, baseY - 48.dp.toPx())
                lineTo(w * 0.34f, baseY - 38.dp.toPx())
                // Tall central citadel keep
                lineTo(w * 0.43f, baseY - 38.dp.toPx())
                lineTo(w * 0.43f, baseY - 94.dp.toPx())
                lineTo(w * 0.50f, baseY - 128.dp.toPx())
                lineTo(w * 0.57f, baseY - 94.dp.toPx())
                lineTo(w * 0.57f, baseY - 42.dp.toPx())
                // East library wing & turret
                lineTo(w * 0.74f, baseY - 42.dp.toPx())
                lineTo(w * 0.74f, baseY - 76.dp.toPx())
                lineTo(w * 0.79f, baseY - 102.dp.toPx())
                lineTo(w * 0.84f, baseY - 76.dp.toPx())
                lineTo(w * 0.84f, baseY - 30.dp.toPx())
                lineTo(w, baseY - 30.dp.toPx())
                lineTo(w, h)
                close()
            }
            drawPath(
                path = castlePath,
                color = lunar.silhouetteColor
            )
        }
    }
}

private fun DrawScope.drawFourPointSparkle(
    center: Offset,
    radius: Float,
    color: Color
) {
    val inner = radius * 0.30f
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + inner, center.y - inner)
        lineTo(center.x + radius, center.y)
        lineTo(center.x + inner, center.y + inner)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - inner, center.y + inner)
        lineTo(center.x - radius, center.y)
        lineTo(center.x - inner, center.y - inner)
        close()
    }
    drawPath(path = path, color = color)
}
