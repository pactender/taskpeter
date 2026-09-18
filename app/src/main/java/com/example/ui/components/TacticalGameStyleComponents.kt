package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HorizonBlue
import com.example.ui.theme.HorizonOrange
import com.example.ui.theme.NeonPunkCyan
import com.example.ui.theme.NeonPunkPink
import com.example.ui.theme.NeonPunkYellow
import com.example.ui.theme.RadiantBg
import com.example.ui.theme.RadiantChalk
import com.example.ui.theme.RadiantCyan
import com.example.ui.theme.RadiantElevated
import com.example.ui.theme.RadiantRed

/**
 * Geometric Chamfer Shape for tactical and cyber containers.
 * Cuts diagonal corners at specified angles.
 */
class ChamferShape(
    val cutTopLeft: Dp = 0.dp,
    val cutTopRight: Dp = 0.dp,
    val cutBottomRight: Dp = 0.dp,
    val cutBottomLeft: Dp = 0.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val cTL = with(density) { cutTopLeft.toPx() }
            val cTR = with(density) { cutTopRight.toPx() }
            val cBR = with(density) { cutBottomRight.toPx() }
            val cBL = with(density) { cutBottomLeft.toPx() }

            moveTo(cTL, 0f)
            lineTo(size.width - cTR, 0f)
            if (cTR > 0f) lineTo(size.width, cTR)
            lineTo(size.width, size.height - cBR)
            if (cBR > 0f) lineTo(size.width - cBR, size.height)
            lineTo(cBL, size.height)
            if (cBL > 0f) lineTo(0f, size.height - cBL)
            lineTo(0f, cTL)
            close()
        }
        return Outline.Generic(path)
    }
}

/** Pre-defined standard tactical chamfer presets */
val TacticalCardShape = ChamferShape(cutTopLeft = 12.dp, cutBottomRight = 12.dp)
val TacticalButtonShape = ChamferShape(cutTopRight = 10.dp, cutBottomLeft = 10.dp)
val TacticalBadgeShape = ChamferShape(cutTopLeft = 6.dp, cutBottomRight = 6.dp)
val GlitchBadgeShape = ChamferShape(cutTopRight = 8.dp, cutBottomLeft = 8.dp)

// Backward-compatibility aliases
val ValorantCardShape = TacticalCardShape
val ValorantButtonShape = TacticalButtonShape
val ValorantBadgeShape = TacticalBadgeShape
val SpiderVerseBadgeShape = GlitchBadgeShape

/**
 * Tactical HUD Card Container with Corner Brackets & Crosshair ticks.
 */
@Composable
fun TacticalHUDCard(
    modifier: Modifier = Modifier,
    borderColor: Color = RadiantRed,
    bracketColor: Color = RadiantCyan,
    containerColor: Color = RadiantElevated,
    headerTag: String? = null,
    showBrackets: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(TacticalCardShape)
            .background(containerColor)
            .border(1.dp, borderColor.copy(alpha = 0.6f), TacticalCardShape)
    ) {
        // Decorative tactical HUD corner ticks
        if (showBrackets) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val tickLen = 14.dp.toPx()
                val strokeW = 2.dp.toPx()

                // Top-left bracket
                drawLine(
                    color = bracketColor,
                    start = Offset(14.dp.toPx(), 4.dp.toPx()),
                    end = Offset(14.dp.toPx() + tickLen, 4.dp.toPx()),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Square
                )
                // Bottom-right bracket
                drawLine(
                    color = bracketColor,
                    start = Offset(size.width - 14.dp.toPx() - tickLen, size.height - 4.dp.toPx()),
                    end = Offset(size.width - 14.dp.toPx(), size.height - 4.dp.toPx()),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Square
                )
            }
        }

        Column(modifier = Modifier.padding(14.dp)) {
            if (headerTag != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(TacticalBadgeShape)
                                .background(RadiantRed)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SYSTEM // PROTOCOL",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = headerTag,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = RadiantCyan,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Tactical Crosshair Icon
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = RadiantCyan.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Box(content = content)
        }
    }
}

@Composable
fun ValorantTacticalCard(
    modifier: Modifier = Modifier,
    borderColor: Color = RadiantRed,
    bracketColor: Color = RadiantCyan,
    containerColor: Color = RadiantElevated,
    headerTag: String? = null,
    showBrackets: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) = TacticalHUDCard(modifier, borderColor, bracketColor, containerColor, headerTag, showBrackets, content)

/**
 * Neon Glitch Comic Panel Container.
 * Features vibrant chromatic offset, Gwen Stacy neon magenta & Miles cyan accents.
 */
@Composable
fun NeonGlitchPanel(
    modifier: Modifier = Modifier,
    comicTitle: String? = null,
    badgeTag: String = "CYBER // PUNK",
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        // Chromatic aberration glow shadow behind
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 3.dp, start = 3.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NeonPunkPink.copy(alpha = 0.35f))
        )

        // Main Comic Panel
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF16102A))
                .border(2.dp, NeonPunkCyan, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column {
                if (comicTitle != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(GlitchBadgeShape)
                                    .background(NeonPunkPink)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeTag,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = comicTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonPunkYellow,
                                letterSpacing = 0.3.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = NeonPunkYellow,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Box(content = content)
            }
        }
    }
}

@Composable
fun SpiderVersePanel(
    modifier: Modifier = Modifier,
    comicTitle: String? = null,
    badgeTag: String = "CYBER // PUNK",
    content: @Composable BoxScope.() -> Unit
) = NeonGlitchPanel(modifier, comicTitle, badgeTag, content)

/**
 * Sci-Fi HUD Action Button with Chamfered Corners and Pulse Ring.
 */
@Composable
fun ActionHUDButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = HorizonOrange,
    secondaryColor: Color = HorizonBlue,
    enabled: Boolean = true,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.AutoMirrored.Filled.ArrowForward
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(TacticalButtonShape)
            .background(
                if (enabled) Brush.horizontalGradient(listOf(primaryColor, primaryColor.copy(alpha = 0.85f)))
                else Brush.horizontalGradient(listOf(Color(0xFF263238), Color(0xFF1E272C)))
            )
            .border(
                1.5.dp,
                if (enabled) secondaryColor.copy(alpha = glowAlpha) else Color(0xFF37474F),
                TacticalButtonShape
            )
            .bounceClick(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp,
                color = if (enabled) Color.White else Color(0xFF90A4AE)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) Color.White else Color(0xFF90A4AE),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun OverwatchTacticalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = HorizonOrange,
    secondaryColor: Color = HorizonBlue,
    enabled: Boolean = true,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.AutoMirrored.Filled.ArrowForward
) = ActionHUDButton(text, onClick, modifier, primaryColor, secondaryColor, enabled, icon)
