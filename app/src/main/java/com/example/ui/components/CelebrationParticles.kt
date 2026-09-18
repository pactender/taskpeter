package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class ParticleItem(
    val id: Long,
    val text: String,
    val color: Color,
    val initialX: Float,
    val initialY: Float,
    val targetX: Float,
    val targetY: Float
)

@Composable
fun CelebrationParticleOverlay(
    trigger: Long,
    modifier: Modifier = Modifier
) {
    if (trigger == 0L) return

    val particles = remember { mutableStateListOf<ParticleItem>() }
    val devTokens = listOf("git commit", "+100 XP", "BUILD PASS", "TASK DONE", "MERGED", "TELEMETRY OK", "CLEAN CODE", "SHIPPED")
    val accentColors = listOf(
        Color(0xFF10B981), // Emerald
        Color(0xFF38BDF8), // Cyan
        Color(0xFFA855F7), // Purple
        Color(0xFFF59E0B), // Amber
        Color(0xFFFF2A85)  // Neon Pink
    )

    LaunchedEffect(trigger) {
        particles.clear()
        val count = 12
        val newParticles = (0 until count).map { i ->
            val angle = Random.nextDouble(0.0, Math.PI * 2)
            val distance = Random.nextFloat() * 240f + 80f
            ParticleItem(
                id = trigger * 100 + i,
                text = devTokens[i % devTokens.size],
                color = accentColors[i % accentColors.size],
                initialX = 0f,
                initialY = 0f,
                targetX = (Math.cos(angle) * distance).toFloat(),
                targetY = (Math.sin(angle) * distance - 80f).toFloat() // biased upwards
            )
        }
        particles.addAll(newParticles)
        delay(1400)
        particles.clear()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        particles.forEach { particle ->
            SingleParticle(particle = particle)
        }
    }
}

@Composable
private fun SingleParticle(particle: ParticleItem) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(particle.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
    }

    val currentProgress = progress.value
    val currentX = particle.initialX + (particle.targetX - particle.initialX) * currentProgress
    val currentY = particle.initialY + (particle.targetY - particle.initialY) * currentProgress
    val alpha = (1f - currentProgress).coerceIn(0f, 1f)
    val scale = (0.5f + (1f - (currentProgress - 0.3f).coerceAtLeast(0f) * 0.7f)).coerceIn(0.4f, 1.3f)

    Text(
        text = particle.text,
        color = particle.color,
        fontSize = if (particle.text.length > 2) 13.sp else 22.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .offset { IntOffset(currentX.toInt(), currentY.toInt()) }
            .scale(scale)
            .alpha(alpha)
    )
}
