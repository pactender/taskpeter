package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevCyan
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevPurple
import com.example.ui.theme.DevRose
import kotlin.math.cos
import kotlin.math.sin

/**
 * TaskPeter Proprietary Themed Glyphs.
 * High-fidelity animated vector emblems tailored specifically for developer workflows,
 * replacing generic system emojis with custom visual identity.
 */
enum class TaskPeterGlyphType(
    val key: String,
    val displayName: String,
    val defaultColor: Color,
    val secondaryColor: Color
) {
    CORE_ENERGY("energy", "Core Energy", DevEmerald, DevCyan),
    PROPULSION_SHIP("ship", "Feature Release", DevCyan, DevPurple),
    CYBER_BUG("bug", "Defect Terminator", DevRose, DevAmber),
    GIT_BRANCH("branch", "Git Confluence", DevPurple, DevCyan),
    ARCH_TOOL("tool", "Refactor Caliper", DevAmber, DevEmerald),
    SYS_PIPELINE("pipeline", "DevOps Gear", DevCyan, DevEmerald),
    TECH_SPEC("spec", "Markdown Spec", DevEmerald, DevCyan),
    CODE_RADAR("radar", "Code Review Lens", DevCyan, DevPurple),
    TEST_REACTOR("qa", "Test Reactor", DevEmerald, DevAmber),
    THERMAL_CORE("thermal", "Priority P0 Core", DevRose, DevAmber),
    CRYPTO_VAULT("vault", "Security Vault", DevEmerald, DevCyan),
    FOCUS_RETICLE("reticle", "Sprint Reticle", DevCyan, DevEmerald),
    NEURAL_CHIP("chip", "Logic Circuit", DevPurple, DevCyan),
    TERMINAL_CLI("cli", "Terminal Shell", DevEmerald, DevCyan);

    companion object {
        fun fromKey(raw: String): TaskPeterGlyphType {
            val clean = raw.trim()
            return when {
                clean.equals("energy", true) || clean == "⚡" || clean.equals("core", true) -> CORE_ENERGY
                clean.equals("ship", true) || clean == "🚀" || clean.equals("feat", true) || clean.equals("feature", true) -> PROPULSION_SHIP
                clean.equals("bug", true) || clean == "🐛" || clean.equals("defect", true) -> CYBER_BUG
                clean.equals("branch", true) || clean == "🔀" || clean.equals("pr", true) || clean.equals("review", true) -> GIT_BRANCH
                clean.equals("tool", true) || clean == "🛠️" || clean == "🛠" || clean.equals("refactor", true) -> ARCH_TOOL
                clean.equals("pipeline", true) || clean == "⚙️" || clean == "⚙" || clean.equals("infra", true) -> SYS_PIPELINE
                clean.equals("spec", true) || clean == "📝" || clean.equals("doc", true) || clean.equals("docs", true) -> TECH_SPEC
                clean.equals("radar", true) || clean == "👀" -> CODE_RADAR
                clean.equals("qa", true) || clean == "🧪" || clean.equals("test", true) -> TEST_REACTOR
                clean.equals("thermal", true) || clean == "🔥" || clean.equals("p0", true) || clean.equals("critical", true) -> THERMAL_CORE
                clean.equals("vault", true) || clean == "🔒" || clean.equals("security", true) || clean == "🛡️" || clean == "🛡" -> CRYPTO_VAULT
                clean.equals("reticle", true) || clean == "🎯" || clean.equals("focus", true) -> FOCUS_RETICLE
                clean.equals("chip", true) || clean == "🧠" || clean == "💡" || clean.equals("logic", true) -> NEURAL_CHIP
                clean.equals("cli", true) || clean == "💻" || clean == "☕" || clean.equals("terminal", true) -> TERMINAL_CLI
                else -> CORE_ENERGY
            }
        }
    }
}

/**
 * Animated High-Fidelity Vector Glyph Composable.
 */
@Composable
fun TaskPeterThemedGlyph(
    glyph: TaskPeterGlyphType,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    animated: Boolean = true,
    tint: Color? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GlyphAnim_${glyph.key}")

    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GlyphRotation"
    )

    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlyphPulse"
    )

    val activePrimary = tint ?: glyph.defaultColor
    val activeSecondary = tint ?: glyph.secondaryColor

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val pulse = if (animated) pulseAnim else 1.0f
        val rotation = if (animated) rotationAnim else 0f

        when (glyph) {
            TaskPeterGlyphType.CORE_ENERGY -> drawCoreEnergy(w, h, pulse, activePrimary, activeSecondary)
            TaskPeterGlyphType.PROPULSION_SHIP -> drawPropulsionShip(w, h, pulse, activePrimary, activeSecondary)
            TaskPeterGlyphType.CYBER_BUG -> drawCyberBug(w, h, pulse, activePrimary, activeSecondary)
            TaskPeterGlyphType.GIT_BRANCH -> drawGitBranch(w, h, pulse, activePrimary, activeSecondary)
            TaskPeterGlyphType.ARCH_TOOL -> drawArchTool(w, h, pulse, activePrimary, activeSecondary)
            TaskPeterGlyphType.SYS_PIPELINE -> drawSysPipeline(w, h, rotation, activePrimary, activeSecondary)
            TaskPeterGlyphType.TECH_SPEC -> drawTechSpec(w, h, pulse, activePrimary, activeSecondary)
            TaskPeterGlyphType.CODE_RADAR -> drawCodeRadar(w, h, pulse, rotation, activePrimary, activeSecondary)
            TaskPeterGlyphType.TEST_REACTOR -> drawTestReactor(w, h, pulse, activePrimary, activeSecondary)
            TaskPeterGlyphType.THERMAL_CORE -> drawThermalCore(w, h, pulse, activePrimary, activeSecondary)
            TaskPeterGlyphType.CRYPTO_VAULT -> drawCryptoVault(w, h, pulse, activePrimary, activeSecondary)
            TaskPeterGlyphType.FOCUS_RETICLE -> drawFocusReticle(w, h, pulse, activePrimary, activeSecondary)
            TaskPeterGlyphType.NEURAL_CHIP -> drawNeuralChip(w, h, pulse, activePrimary, activeSecondary)
            TaskPeterGlyphType.TERMINAL_CLI -> drawTerminalCli(w, h, pulse, activePrimary, activeSecondary)
        }
    }
}

@Composable
fun TaskPeterThemedGlyph(
    symbol: String,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    animated: Boolean = true,
    tint: Color? = null
) {
    val glyphType = TaskPeterGlyphType.fromKey(symbol)
    TaskPeterThemedGlyph(
        glyph = glyphType,
        modifier = modifier,
        size = size,
        animated = animated,
        tint = tint
    )
}

/**
 * Grid Picker for selecting custom TaskPeter Glyphs.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskPeterGlyphPicker(
    selectedGlyphKey: String,
    onGlyphSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeGlyph = TaskPeterGlyphType.fromKey(selectedGlyphKey)

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskPeterGlyphType.entries.forEach { glyph ->
            val isSelected = glyph == activeGlyph
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) glyph.defaultColor.copy(alpha = 0.22f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 0.8.dp,
                        color = if (isSelected) glyph.defaultColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onGlyphSelected(glyph.key) }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                TaskPeterThemedGlyph(
                    glyph = glyph,
                    size = 22.dp,
                    animated = isSelected
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// Custom Precision Vector Drawing Functions
// -----------------------------------------------------------------------------------------

private fun DrawScope.drawCoreEnergy(w: Float, h: Float, pulse: Float, pri: Color, sec: Color) {
    // Background squircle shield
    drawRoundRect(
        brush = Brush.linearGradient(listOf(pri.copy(alpha = 0.20f), sec.copy(alpha = 0.10f))),
        size = Size(w, h),
        cornerRadius = CornerRadius(w * 0.25f, h * 0.25f)
    )
    drawRoundRect(
        color = pri.copy(alpha = 0.45f * pulse),
        size = Size(w, h),
        cornerRadius = CornerRadius(w * 0.25f, h * 0.25f),
        style = Stroke(width = 1.2f)
    )

    // Lightning core path
    val path = Path().apply {
        moveTo(w * 0.52f, h * 0.15f)
        lineTo(w * 0.26f, h * 0.54f)
        lineTo(w * 0.48f, h * 0.54f)
        lineTo(w * 0.44f, h * 0.88f)
        lineTo(w * 0.74f, h * 0.46f)
        lineTo(w * 0.52f, h * 0.46f)
        close()
    }
    drawPath(path, pri, style = Fill)
    drawPath(path, sec, style = Stroke(width = 1f, join = StrokeJoin.Round))
}

private fun DrawScope.drawPropulsionShip(w: Float, h: Float, pulse: Float, pri: Color, sec: Color) {
    // Rocket fuselage
    val body = Path().apply {
        moveTo(w * 0.50f, h * 0.12f)
        cubicTo(w * 0.65f, h * 0.35f, w * 0.68f, h * 0.62f, w * 0.62f, h * 0.75f)
        lineTo(w * 0.38f, h * 0.75f)
        cubicTo(w * 0.32f, h * 0.62f, w * 0.35f, h * 0.35f, w * 0.50f, h * 0.12f)
        close()
    }
    drawPath(body, pri)

    // Side fins
    val fins = Path().apply {
        moveTo(w * 0.38f, h * 0.65f)
        lineTo(w * 0.20f, h * 0.78f)
        lineTo(w * 0.35f, h * 0.78f)
        close()

        moveTo(w * 0.62f, h * 0.65f)
        lineTo(w * 0.80f, h * 0.78f)
        lineTo(w * 0.65f, h * 0.78f)
        close()
    }
    drawPath(fins, sec)

    // Animated exhaust thrust
    val flame = Path().apply {
        moveTo(w * 0.42f, h * 0.77f)
        lineTo(w * 0.50f, h * (0.88f + 0.10f * pulse))
        lineTo(w * 0.58f, h * 0.77f)
        close()
    }
    drawPath(flame, DevAmber)

    // Cockpit porthole
    drawCircle(color = Color.Black, radius = w * 0.08f, center = Offset(w * 0.50f, h * 0.42f))
    drawCircle(color = sec, radius = w * 0.05f, center = Offset(w * 0.50f, h * 0.42f))
}

private fun DrawScope.drawCyberBug(w: Float, h: Float, pulse: Float, pri: Color, sec: Color) {
    // Bug exoskeleton
    drawOval(
        color = pri,
        topLeft = Offset(w * 0.32f, h * 0.32f),
        size = Size(w * 0.36f, h * 0.46f)
    )
    // Head
    drawCircle(color = sec, radius = w * 0.14f, center = Offset(w * 0.50f, h * 0.24f))

    // Antennae
    drawLine(sec, Offset(w * 0.44f, h * 0.18f), Offset(w * 0.30f, h * 0.08f), strokeWidth = 1.5f, cap = StrokeCap.Round)
    drawLine(sec, Offset(w * 0.56f, h * 0.18f), Offset(w * 0.70f, h * 0.08f), strokeWidth = 1.5f, cap = StrokeCap.Round)

    // Circuit legs
    val legY = listOf(0.40f, 0.54f, 0.68f)
    legY.forEach { y ->
        drawLine(pri.copy(alpha = 0.8f), Offset(w * 0.32f, h * y), Offset(w * 0.14f, h * (y - 0.04f * pulse)), strokeWidth = 1.4f, cap = StrokeCap.Round)
        drawLine(pri.copy(alpha = 0.8f), Offset(w * 0.68f, h * y), Offset(w * 0.86f, h * (y - 0.04f * pulse)), strokeWidth = 1.4f, cap = StrokeCap.Round)
    }

    // Reticle crosshair over bug
    drawCircle(sec.copy(alpha = 0.4f * pulse), radius = w * 0.42f, center = Offset(w * 0.50f, h * 0.50f), style = Stroke(width = 1f))
}

private fun DrawScope.drawGitBranch(w: Float, h: Float, pulse: Float, pri: Color, sec: Color) {
    // Main vertical branch
    val lineX = w * 0.34f
    drawLine(pri, Offset(lineX, h * 0.18f), Offset(lineX, h * 0.82f), strokeWidth = 2.2f, cap = StrokeCap.Round)

    // Curved fork
    val fork = Path().apply {
        moveTo(lineX, h * 0.56f)
        cubicTo(lineX, h * 0.42f, w * 0.66f, h * 0.50f, w * 0.66f, h * 0.36f)
    }
    drawPath(fork, sec, style = Stroke(width = 2.2f, cap = StrokeCap.Round))

    // Commit nodes
    drawCircle(pri, radius = w * 0.12f, center = Offset(lineX, h * 0.22f))
    drawCircle(Color.Black, radius = w * 0.06f, center = Offset(lineX, h * 0.22f))

    drawCircle(pri, radius = w * 0.12f, center = Offset(lineX, h * 0.78f))
    drawCircle(Color.Black, radius = w * 0.06f, center = Offset(lineX, h * 0.78f))

    drawCircle(sec, radius = w * 0.12f * pulse, center = Offset(w * 0.66f, h * 0.34f))
    drawCircle(Color.Black, radius = w * 0.06f, center = Offset(w * 0.66f, h * 0.34f))
}

private fun DrawScope.drawArchTool(w: Float, h: Float, pulse: Float, pri: Color, sec: Color) {
    // Wrench / Caliper diagonal
    val wrench = Path().apply {
        moveTo(w * 0.30f, h * 0.25f)
        lineTo(w * 0.75f, h * 0.70f)
        lineTo(w * 0.68f, h * 0.77f)
        lineTo(w * 0.23f, h * 0.32f)
        close()
    }
    drawPath(wrench, pri)

    // Caliper head
    drawCircle(pri, radius = w * 0.16f, center = Offset(w * 0.26f, h * 0.28f), style = Stroke(width = 2f))
    drawCircle(sec, radius = w * 0.12f, center = Offset(w * 0.72f, h * 0.74f))
    drawCircle(Color.Black, radius = w * 0.06f, center = Offset(w * 0.72f, h * 0.74f))
}

private fun DrawScope.drawSysPipeline(w: Float, h: Float, rotation: Float, pri: Color, sec: Color) {
    rotate(rotation) {
        val teeth = 6
        val rOuter = w * 0.44f
        val rInner = w * 0.32f
        val gearPath = Path()

        for (i in 0 until teeth) {
            val angle = i * (360f / teeth)
            val rad1 = Math.toRadians((angle - 15).toDouble())
            val rad2 = Math.toRadians((angle + 15).toDouble())
            val radMid1 = Math.toRadians((angle - 8).toDouble())
            val radMid2 = Math.toRadians((angle + 8).toDouble())

            val cx = w * 0.5f
            val cy = h * 0.5f

            if (i == 0) gearPath.moveTo((cx + rInner * cos(rad1)).toFloat(), (cy + rInner * sin(rad1)).toFloat())
            gearPath.lineTo((cx + rOuter * cos(radMid1)).toFloat(), (cy + rOuter * sin(radMid1)).toFloat())
            gearPath.lineTo((cx + rOuter * cos(radMid2)).toFloat(), (cy + rOuter * sin(radMid2)).toFloat())
            gearPath.lineTo((cx + rInner * cos(rad2)).toFloat(), (cy + rInner * sin(rad2)).toFloat())
        }
        gearPath.close()

        drawPath(gearPath, pri, style = Stroke(width = 2.4f, join = StrokeJoin.Round))
        drawCircle(sec, radius = w * 0.15f, center = Offset(w * 0.5f, h * 0.5f))
        drawCircle(Color.Black, radius = w * 0.07f, center = Offset(w * 0.5f, h * 0.5f))
    }
}

private fun DrawScope.drawTechSpec(w: Float, h: Float, pulse: Float, pri: Color, sec: Color) {
    // Document outline
    drawRoundRect(
        color = pri.copy(alpha = 0.25f),
        topLeft = Offset(w * 0.22f, h * 0.14f),
        size = Size(w * 0.56f, h * 0.72f),
        cornerRadius = CornerRadius(w * 0.1f, h * 0.1f)
    )
    drawRoundRect(
        color = pri,
        topLeft = Offset(w * 0.22f, h * 0.14f),
        size = Size(w * 0.56f, h * 0.72f),
        cornerRadius = CornerRadius(w * 0.1f, h * 0.1f),
        style = Stroke(width = 1.5f)
    )

    // Code lines
    drawLine(sec, Offset(w * 0.32f, h * 0.32f), Offset(w * 0.66f, h * 0.32f), strokeWidth = 1.8f, cap = StrokeCap.Round)
    drawLine(sec, Offset(w * 0.32f, h * 0.46f), Offset(w * 0.58f, h * 0.46f), strokeWidth = 1.8f, cap = StrokeCap.Round)
    drawLine(sec.copy(alpha = 0.7f), Offset(w * 0.32f, h * 0.60f), Offset(w * 0.62f, h * 0.60f), strokeWidth = 1.8f, cap = StrokeCap.Round)

    // Active cursor indicator
    drawCircle(DevEmerald, radius = 2f * pulse, center = Offset(w * 0.66f, h * 0.60f))
}

private fun DrawScope.drawCodeRadar(w: Float, h: Float, pulse: Float, rotation: Float, pri: Color, sec: Color) {
    // Eye / Reticle outer border
    val eye = Path().apply {
        moveTo(w * 0.15f, h * 0.50f)
        cubicTo(w * 0.32f, h * 0.25f, w * 0.68f, h * 0.25f, w * 0.85f, h * 0.50f)
        cubicTo(w * 0.68f, h * 0.75f, w * 0.32f, h * 0.75f, w * 0.15f, h * 0.50f)
        close()
    }
    drawPath(eye, pri, style = Stroke(width = 1.8f, cap = StrokeCap.Round))

    // Pupil
    drawCircle(sec, radius = w * 0.16f * pulse, center = Offset(w * 0.50f, h * 0.50f))
    drawCircle(Color.Black, radius = w * 0.08f, center = Offset(w * 0.50f, h * 0.50f))

    // Scanning sweep line
    rotate(rotation, pivot = Offset(w * 0.50f, h * 0.50f)) {
        drawLine(DevEmerald.copy(alpha = 0.6f), Offset(w * 0.50f, h * 0.50f), Offset(w * 0.75f, h * 0.50f), strokeWidth = 1.2f)
    }
}

private fun DrawScope.drawTestReactor(w: Float, h: Float, pulse: Float, pri: Color, sec: Color) {
    // Lab beaker flask
    val flask = Path().apply {
        moveTo(w * 0.40f, h * 0.15f)
        lineTo(w * 0.60f, h * 0.15f)
        lineTo(w * 0.60f, h * 0.35f)
        lineTo(w * 0.80f, h * 0.78f)
        cubicTo(w * 0.82f, h * 0.84f, w * 0.76f, h * 0.88f, w * 0.70f, h * 0.88f)
        lineTo(w * 0.30f, h * 0.88f)
        cubicTo(w * 0.24f, h * 0.88f, w * 0.18f, h * 0.84f, w * 0.20f, h * 0.78f)
        lineTo(w * 0.40f, h * 0.35f)
        close()
    }
    drawPath(flask, pri, style = Stroke(width = 1.6f, join = StrokeJoin.Round))

    // Reagent fluid
    val fluid = Path().apply {
        moveTo(w * 0.26f, h * 0.65f)
        lineTo(w * 0.74f, h * 0.65f)
        lineTo(w * 0.78f, h * 0.84f)
        lineTo(w * 0.22f, h * 0.84f)
        close()
    }
    drawPath(fluid, sec.copy(alpha = 0.45f))

    // Effervescent micro bubbles
    drawCircle(sec, radius = 2.0f * pulse, center = Offset(w * 0.45f, h * 0.58f))
    drawCircle(pri, radius = 1.5f, center = Offset(w * 0.55f, h * 0.48f))
    drawCircle(DevAmber, radius = 2.2f * pulse, center = Offset(w * 0.50f, h * 0.72f))
}

private fun DrawScope.drawThermalCore(w: Float, h: Float, pulse: Float, pri: Color, sec: Color) {
    // Multi-layer plasma flame
    val outerFlame = Path().apply {
        moveTo(w * 0.50f, h * 0.10f)
        cubicTo(w * 0.68f, h * 0.32f, w * 0.82f, h * 0.60f, w * 0.72f, h * 0.85f)
        cubicTo(w * 0.62f, h * 0.95f, w * 0.38f, h * 0.95f, w * 0.28f, h * 0.85f)
        cubicTo(w * 0.18f, h * 0.60f, w * 0.32f, h * 0.32f, w * 0.50f, h * 0.10f)
        close()
    }
    drawPath(outerFlame, pri)

    // Inner hotter plasma
    val innerFlame = Path().apply {
        moveTo(w * 0.50f, h * 0.38f)
        cubicTo(w * 0.62f, h * 0.52f, w * 0.66f, h * 0.72f, w * 0.58f, h * 0.82f)
        cubicTo(w * 0.52f, h * 0.88f, w * 0.48f, h * 0.88f, w * 0.42f, h * 0.82f)
        cubicTo(w * 0.34f, h * 0.72f, w * 0.38f, h * 0.52f, w * 0.50f, h * 0.38f)
        close()
    }
    drawPath(innerFlame, sec.copy(alpha = pulse.coerceIn(0.7f, 1f)))
}

private fun DrawScope.drawCryptoVault(w: Float, h: Float, pulse: Float, pri: Color, sec: Color) {
    // Padlock shackle
    val shackle = Path().apply {
        moveTo(w * 0.32f, h * 0.45f)
        lineTo(w * 0.32f, h * 0.30f)
        cubicTo(w * 0.32f, h * 0.15f, w * 0.68f, h * 0.15f, w * 0.68f, h * 0.30f)
        lineTo(w * 0.68f, h * 0.45f)
    }
    drawPath(shackle, sec, style = Stroke(width = 2.2f, cap = StrokeCap.Round))

    // Body
    drawRoundRect(
        color = pri,
        topLeft = Offset(w * 0.22f, h * 0.42f),
        size = Size(w * 0.56f, h * 0.48f),
        cornerRadius = CornerRadius(w * 0.12f, h * 0.12f)
    )

    // Keyhole
    drawCircle(Color.Black, radius = w * 0.08f, center = Offset(w * 0.50f, h * 0.62f))
    drawRect(Color.Black, topLeft = Offset(w * 0.46f, h * 0.62f), size = Size(w * 0.08f, h * 0.15f))
    drawCircle(sec, radius = 2f * pulse, center = Offset(w * 0.50f, h * 0.62f))
}

private fun DrawScope.drawFocusReticle(w: Float, h: Float, pulse: Float, pri: Color, sec: Color) {
    val cx = w * 0.50f
    val cy = h * 0.50f

    // Outer target ring
    drawCircle(pri, radius = w * 0.40f * pulse, center = Offset(cx, cy), style = Stroke(width = 1.6f))
    // Inner target ring
    drawCircle(sec, radius = w * 0.22f, center = Offset(cx, cy), style = Stroke(width = 1.4f))
    // Center bullseye
    drawCircle(pri, radius = w * 0.08f, center = Offset(cx, cy))

    // Crosshairs
    drawLine(pri.copy(alpha = 0.7f), Offset(cx, h * 0.06f), Offset(cx, h * 0.22f), strokeWidth = 1.4f)
    drawLine(pri.copy(alpha = 0.7f), Offset(cx, h * 0.78f), Offset(cx, h * 0.94f), strokeWidth = 1.4f)
    drawLine(pri.copy(alpha = 0.7f), Offset(w * 0.06f, cy), Offset(w * 0.22f, cy), strokeWidth = 1.4f)
    drawLine(pri.copy(alpha = 0.7f), Offset(w * 0.78f, cy), Offset(w * 0.94f, cy), strokeWidth = 1.4f)
}

private fun DrawScope.drawNeuralChip(w: Float, h: Float, pulse: Float, pri: Color, sec: Color) {
    // Chip body
    drawRoundRect(
        color = pri,
        topLeft = Offset(w * 0.28f, h * 0.28f),
        size = Size(w * 0.44f, h * 0.44f),
        cornerRadius = CornerRadius(w * 0.08f, h * 0.08f)
    )

    // Inner core
    drawCircle(sec, radius = w * 0.12f * pulse, center = Offset(w * 0.50f, h * 0.50f))
    drawCircle(Color.Black, radius = w * 0.06f, center = Offset(w * 0.50f, h * 0.50f))

    // Bus pins
    val pins = listOf(0.38f, 0.50f, 0.62f)
    pins.forEach { p ->
        // Top & Bottom
        drawLine(sec, Offset(w * p, h * 0.12f), Offset(w * p, h * 0.28f), strokeWidth = 1.5f)
        drawLine(sec, Offset(w * p, h * 0.72f), Offset(w * p, h * 0.88f), strokeWidth = 1.5f)
        // Left & Right
        drawLine(sec, Offset(w * 0.12f, h * p), Offset(w * 0.28f, h * p), strokeWidth = 1.5f)
        drawLine(sec, Offset(w * 0.72f, h * p), Offset(w * 0.88f, h * p), strokeWidth = 1.5f)
    }
}

private fun DrawScope.drawTerminalCli(w: Float, h: Float, pulse: Float, pri: Color, sec: Color) {
    // Terminal window frame
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(w * 0.15f, h * 0.18f),
        size = Size(w * 0.70f, h * 0.64f),
        cornerRadius = CornerRadius(w * 0.1f, h * 0.1f)
    )
    drawRoundRect(
        color = pri,
        topLeft = Offset(w * 0.15f, h * 0.18f),
        size = Size(w * 0.70f, h * 0.64f),
        cornerRadius = CornerRadius(w * 0.1f, h * 0.1f),
        style = Stroke(width = 1.5f)
    )

    // Prompt ">"
    val prompt = Path().apply {
        moveTo(w * 0.28f, h * 0.38f)
        lineTo(w * 0.44f, h * 0.50f)
        lineTo(w * 0.28f, h * 0.62f)
    }
    drawPath(prompt, pri, style = Stroke(width = 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Blinking cursor "_"
    drawLine(
        color = sec.copy(alpha = pulse.coerceIn(0.2f, 1f)),
        start = Offset(w * 0.50f, h * 0.62f),
        end = Offset(w * 0.66f, h * 0.62f),
        strokeWidth = 2.2f,
        cap = StrokeCap.Square
    )
}
