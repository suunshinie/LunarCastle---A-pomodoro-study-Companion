package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalLunarColors
import com.example.ui.theme.LocalReducedMotion
import com.example.ui.theme.LunarTextStyles
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Rounded-2xl (16.dp) archival card with soft borders, shadow depth, and subtle gold rim light.
 */
@Composable
fun LunarCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val lunar = LocalLunarColors.current
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor = if (isFocused) lunar.gold else lunar.cardBorder
    val borderWidth = if (isFocused) 2.dp else 1.dp

    Surface(
        modifier = modifier
            .shadow(
                elevation = if (lunar.isDark) 6.dp else 3.dp,
                shape = shape,
                ambientColor = lunar.midnight.copy(alpha = 0.25f),
                spotColor = lunar.midnight.copy(alpha = 0.35f)
            )
            .drawBehind {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(lunar.rimHighlight, Color.Transparent),
                        start = Offset.Zero,
                        end = Offset(size.width * 0.55f, size.height * 0.4f)
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(shape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(color = lunar.gold),
                            onClick = onClick
                        )
                } else Modifier
            ),
        shape = shape,
        color = lunar.cardSurface,
        border = BorderStroke(borderWidth, borderColor),
        tonalElevation = if (lunar.isDark) 2.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * Primary gold button (#E5C17B) with Midnight (#12172B) text, >= 48.dp touch target, and visible gold focus ring.
 */
@Composable
fun LunarPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val lunar = LocalLunarColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .then(
                if (isFocused) {
                    Modifier.border(2.dp, lunar.lightGold, RoundedCornerShape(16.dp))
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = lunar.gold,
            contentColor = lunar.goldButtonText,
            disabledContainerColor = lunar.cardBorder,
            disabledContentColor = lunar.secondaryText.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun LunarSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val lunar = LocalLunarColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isFocused) 2.dp else 1.dp,
            color = if (isFocused) lunar.gold else lunar.cardBorder
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = lunar.primaryText
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun LunarChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subLabel: String? = null,
    accentGold: Boolean = false
) {
    val lunar = LocalLunarColors.current
    val reducedMotion = LocalReducedMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val targetBg = when {
        selected && accentGold -> lunar.goldWash
        selected -> lunar.lavenderWash
        else -> lunar.canvas
    }
    val targetBorder = when {
        isFocused -> lunar.gold
        selected && accentGold -> lunar.gold
        selected -> lunar.lavender
        else -> lunar.cardBorder
    }

    val bgColor by animateColorAsState(
        targetValue = targetBg,
        animationSpec = if (reducedMotion) snap() else tween(180),
        label = "chip_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = targetBorder,
        animationSpec = if (reducedMotion) snap() else tween(180),
        label = "chip_border"
    )

    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(999.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = lunar.gold),
                onClick = onClick
            ),
        shape = RoundedCornerShape(999.dp),
        color = bgColor,
        border = BorderStroke(if (isFocused) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = LunarTextStyles.StatusMono,
                color = when {
                    selected && accentGold -> if (lunar.isDark) lunar.lightGold else lunar.midnight
                    selected -> lunar.lavender
                    else -> lunar.secondaryText
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subLabel.isNullOrBlank()) {
                Text(
                    text = "· $subLabel",
                    style = LunarTextStyles.StatusMono,
                    color = lunar.mutedMetaText,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun SearchScrollField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    testTag: String = "search_scroll_input"
) {
    val lunar = LocalLunarColors.current
    val reducedMotion = LocalReducedMotion.current
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) lunar.gold else lunar.cardBorder,
        animationSpec = if (reducedMotion) snap() else tween(160),
        label = "scroll_border"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(lunar.canvas)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = "Search",
            tint = if (isFocused) lunar.gold else lunar.mutedMetaText,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = lunar.mutedMetaText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = lunar.primaryText
                ),
                cursorBrush = SolidColor(lunar.gold),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag)
                    .onFocusChanged { isFocused = it.isFocused }
            )
        }
        if (value.isNotEmpty()) {
            IconButton(
                onClick = { onValueChange("") },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("${testTag}_clear")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Clear search",
                    tint = lunar.secondaryText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun DiamondCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val lunar = LocalLunarColors.current
    val reducedMotion = LocalReducedMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val fillProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = if (reducedMotion) snap() else spring(dampingRatio = 0.7f, stiffness = 420f),
        label = "diamond_fill"
    )
    val scale by animateFloatAsState(
        targetValue = if (checked && !reducedMotion) 1.06f else 1f,
        animationSpec = if (reducedMotion) snap() else spring(dampingRatio = 0.6f, stiffness = 350f),
        label = "diamond_scale"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .then(if (isFocused) Modifier.border(2.dp, lunar.gold, CircleShape) else Modifier)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = lunar.gold),
                role = Role.Checkbox,
                onClickLabel = contentDescription,
                onClick = onCheckedChange
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp * scale)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val r = (w.coerceAtMost(h) / 2f) - 2.dp.toPx()

            val diamond = Path().apply {
                moveTo(cx, cy - r)
                lineTo(cx + r, cy)
                lineTo(cx, cy + r)
                lineTo(cx - r, cy)
                close()
            }

            if (fillProgress > 0.05f) {
                drawPath(
                    path = diamond,
                    color = lunar.gold.copy(alpha = fillProgress)
                )
            }
            drawPath(
                path = diamond,
                color = if (checked) lunar.gold else lunar.cardBorder,
                style = Stroke(width = 1.6.dp.toPx())
            )

            if (fillProgress > 0.45f) {
                val checkPath = Path().apply {
                    moveTo(cx - r * 0.38f, cy + r * 0.02f)
                    lineTo(cx - r * 0.06f, cy + r * 0.34f)
                    lineTo(cx + r * 0.42f, cy - r * 0.28f)
                }
                drawPath(
                    path = checkPath,
                    color = lunar.midnight.copy(alpha = fillProgress),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }
    }
}

@Composable
fun HangingBookmarkTag(
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val lunar = LocalLunarColors.current
    Box(
        modifier = modifier
            .offset(y = (-6).dp)
            .defaultMinSize(minHeight = 48.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .drawBehind {
                val tagHeight = size.height - 8.dp.toPx()
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, tagHeight)
                    lineTo(size.width / 2f, tagHeight - 6.dp.toPx())
                    lineTo(0f, tagHeight)
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(lunar.gold, lunar.lightGold)
                    )
                )
            }
            .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = label.uppercase(),
            style = LunarTextStyles.StatusMono,
            color = lunar.midnight,
            maxLines = 1
        )
    }
}

@Composable
fun CelestialTimerDial(
    formattedTime: String,
    progressFraction: Float,
    phaseLabel: String,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val lunar = LocalLunarColors.current
    val reducedMotion = LocalReducedMotion.current
    val activeLavender = lunar.lavender
    val deepLavender = lunar.deepLavender

    val pulseAlpha: Float = if (isRunning && !reducedMotion) {
        val infiniteTransition = rememberInfiniteTransition(label = "celestial_glow")
        infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "moon_pulse"
        ).value
    } else {
        if (isRunning) 0.65f else 0.45f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = if (reducedMotion) snap() else tween(450, easing = FastOutSlowInEasing),
        label = "dial_sweep"
    )

    Box(
        modifier = modifier.size(244.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 10.dp.toPx()
            val outerRingPadding = 12.dp.toPx()
            val arcInset = outerRingPadding + 14.dp.toPx()
            val arcDiameter = size.minDimension - arcInset * 2
            val topLeft = Offset(arcInset, arcInset)
            val arcSize = Size(arcDiameter, arcDiameter)

            // Outer astrolabe constellation ring
            drawCircle(
                color = lunar.cardBorder.copy(alpha = 0.85f),
                radius = (size.minDimension / 2f) - outerRingPadding,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f), 0f)
                )
            )

            // Ambient glow when active
            if (isRunning) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            activeLavender.copy(alpha = 0.16f * pulseAlpha),
                            Color.Transparent
                        )
                    ),
                    radius = size.minDimension * 0.5f
                )
            }

            // Recessed track
            drawArc(
                color = lunar.cardBorder,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active lavender progress stroke
            val sweep = animatedProgress * 360f
            if (sweep > 0.5f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(deepLavender, activeLavender, lunar.gold)
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            // Glowing crescent terminal point
            val angleRad = ((-90f + sweep) * PI / 180.0)
            val radius = arcDiameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val tip = Offset(
                center.x + (radius * cos(angleRad)).toFloat(),
                center.y + (radius * sin(angleRad)).toFloat()
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        lunar.gold.copy(alpha = pulseAlpha),
                        Color.Transparent
                    ),
                    center = tip,
                    radius = 16.dp.toPx()
                ),
                center = tip,
                radius = 16.dp.toPx()
            )
            drawCircle(
                color = lunar.creamSurface,
                radius = 6.dp.toPx(),
                center = tip
            )
            drawCircle(
                color = lunar.canvas,
                radius = 4.2.dp.toPx(),
                center = Offset(tip.x + 2.2.dp.toPx(), tip.y - 1.5.dp.toPx())
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = phaseLabel.uppercase(),
                style = LunarTextStyles.StatusMono,
                color = if (isRunning) lunar.lavender else lunar.mutedMetaText
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = formattedTime,
                style = LunarTextStyles.TimerLarge,
                color = if (lunar.isDark) lunar.lightGold else lunar.midnight
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isRunning) "MOON ROOM ACTIVE" else "MOON ROOM READY",
                style = LunarTextStyles.StatusMono,
                color = lunar.secondaryText.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun ConstellationDivider(modifier: Modifier = Modifier) {
    val lunar = LocalLunarColors.current
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
    ) {
        val cy = size.height / 2f
        val cx = size.width / 2f
        drawLine(
            color = lunar.cardBorder,
            start = Offset(0f, cy),
            end = Offset(cx - 24.dp.toPx(), cy),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = lunar.cardBorder,
            start = Offset(cx + 24.dp.toPx(), cy),
            end = Offset(size.width, cy),
            strokeWidth = 1.dp.toPx()
        )
        val r = 5.dp.toPx()
        val star = Path().apply {
            moveTo(cx, cy - r)
            lineTo(cx + r * 0.35f, cy - r * 0.35f)
            lineTo(cx + r, cy)
            lineTo(cx + r * 0.35f, cy + r * 0.35f)
            lineTo(cx, cy + r)
            lineTo(cx - r * 0.35f, cy + r * 0.35f)
            lineTo(cx - r, cy)
            lineTo(cx - r * 0.35f, cy - r * 0.35f)
            close()
        }
        drawPath(star, color = lunar.gold.copy(alpha = 0.85f))
        drawCircle(lunar.lavender, radius = 1.8.dp.toPx(), center = Offset(cx - 14.dp.toPx(), cy))
        drawCircle(lunar.lavender, radius = 1.8.dp.toPx(), center = Offset(cx + 14.dp.toPx(), cy))
    }
}

@Composable
fun lunarFieldColors(): TextFieldColors {
    val lunar = LocalLunarColors.current
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = lunar.gold,
        unfocusedBorderColor = lunar.cardBorder,
        focusedTextColor = lunar.primaryText,
        unfocusedTextColor = lunar.primaryText,
        focusedLabelColor = if (lunar.isDark) lunar.gold else lunar.deepLavender,
        unfocusedLabelColor = lunar.mutedMetaText,
        focusedContainerColor = lunar.canvas,
        unfocusedContainerColor = lunar.canvas,
        cursorColor = lunar.gold
    )
}
