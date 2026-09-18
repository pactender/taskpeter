package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.theme.DevCyan
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevOrange
import com.example.ui.theme.DevPurple

enum class SpotlightTarget {
    TOP_COMMAND_PROMPT,
    FILTER_MATRIX,
    FOCUS_TIMER_CARD,
    ANALYTICS_VELOCITY,
    GITHUB_SYNC_CARD,
    THEME_PICKER_ACTION
}

data class CoachMarkStep(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val proTip: String,
    val badge: String,
    val icon: ImageVector,
    val targetTab: AppTab,
    val accentColor: Color,
    val target: SpotlightTarget
)

val DEFAULT_COACH_MARK_STEPS = listOf(
    CoachMarkStep(
        id = "step_cli_input",
        title = "CLI Command Prompt",
        subtitle = "Rapid inline syntax for power developers",
        description = "Type tasks directly into the CLI input bar. Use #tag for modules, !p1 or !p2 for urgency, ^30m for estimates, and @repo to link Git repos.",
        proTip = "Press Enter on keyboard to instantly log tasks without leaving your typing flow.",
        badge = "CLI SYNTAX",
        icon = Icons.Default.Terminal,
        targetTab = AppTab.TASKS,
        accentColor = DevCyan,
        target = SpotlightTarget.TOP_COMMAND_PROMPT
    ),
    CoachMarkStep(
        id = "step_filters",
        title = "Sprint Status Matrix",
        subtitle = "Instant status filters & telemetry search",
        description = "One-tap switching between All, In Progress, Code Review, and Done. Search matches task descriptions, tags, and commits in real time.",
        proTip = "Tap any task card to expand subtasks, edit attributes, or launch a linked focus sprint.",
        badge = "STATUS MATRIX",
        icon = Icons.Default.Checklist,
        targetTab = AppTab.TASKS,
        accentColor = DevEmerald,
        target = SpotlightTarget.FILTER_MATRIX
    ),
    CoachMarkStep(
        id = "step_focus",
        title = "Pomodoro Focus Sprints",
        subtitle = "Distraction-free deep work engine",
        description = "Execute timed 25m or 50m developer sprints directly linked to your active backlog item, complete with built-in ambient audio hums.",
        proTip = "Completed focus intervals automatically feed into your 7-day velocity and commit analytics.",
        badge = "DEEP FOCUS",
        icon = Icons.Default.Timer,
        targetTab = AppTab.FOCUS,
        accentColor = DevOrange,
        target = SpotlightTarget.FOCUS_TIMER_CARD
    ),
    CoachMarkStep(
        id = "step_analytics",
        title = "Weekly Velocity Telemetry",
        subtitle = "7-day rolling performance curve",
        description = "Inspect your real-time productivity curves showing finished tasks vs. focus duration, sprint completion rates, and tag distributions.",
        proTip = "Tap any individual weekday column on the chart to inspect precise focus minutes.",
        badge = "TELEMETRY",
        icon = Icons.Default.AutoGraph,
        targetTab = AppTab.ANALYTICS,
        accentColor = DevCyan,
        target = SpotlightTarget.ANALYTICS_VELOCITY
    ),
    CoachMarkStep(
        id = "step_git_sync",
        title = "GitHub OAuth2 & Issues Sync",
        subtitle = "RFC 8628 Device Flow & Dashboard Integration",
        description = "Authenticate with GitHub OAuth2 to seamlessly import open issues and pull requests into your TaskPeter backlog with one click, and track commit hashes in real time.",
        proTip = "Tap 'Sync to Tasks' on any GitHub issue or PR to instantly turn it into a trackable task ticket!",
        badge = "OAUTH2 & ISSUES",
        icon = Icons.Default.CloudSync,
        targetTab = AppTab.SYNC,
        accentColor = DevPurple,
        target = SpotlightTarget.GITHUB_SYNC_CARD
    ),
    CoachMarkStep(
        id = "step_themes",
        title = "IDE Color Schemes",
        subtitle = "Fleet, Darcula & Cyberpunk aesthetics",
        description = "Tap the palette icon in the top navigation bar to select tailored developer themes with high-contrast, eye-strain reducing palettes.",
        proTip = "Toggle the sun/moon icon for instant light/dark mode switching.",
        badge = "THEME STUDIO",
        icon = Icons.Default.Palette,
        targetTab = AppTab.TASKS,
        accentColor = Color(0xFFFFD54F),
        target = SpotlightTarget.THEME_PICKER_ACTION
    )
)

@Composable
fun CoachMarksOverlay(
    active: Boolean,
    currentStepIndex: Int,
    steps: List<CoachMarkStep> = DEFAULT_COACH_MARK_STEPS,
    onSelectTab: (Int) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!active || steps.isEmpty()) return

    val stepIndex = currentStepIndex.coerceIn(0, steps.size - 1)
    val currentStep = steps[stepIndex]

    // Sync tab when step changes so the highlighted feature is visible on screen
    LaunchedEffect(currentStep.targetTab) {
        onSelectTab(currentStep.targetTab.ordinal)
    }

    // Beacon ripple animation
    val infiniteTransition = rememberInfiniteTransition(label = "coach_mark_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Subtle pointer arrow bounce
    val arrowBounce by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow_bounce"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* Tap on backdrop advances gently */ onNextStep() }
            )
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // 1. Semi-transparent non-intrusive backdrop scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
        )

        // 2. Animated Target Spotlight Beacon
        TargetSpotlightBeacon(
            target = currentStep.target,
            accentColor = currentStep.accentColor,
            pulseScale = pulseScale,
            pulseAlpha = pulseAlpha,
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )

        // 3. Floating Tooltip Card Container (Positioned safely so it never overlaps the target spotlight)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = when (currentStep.target) {
                SpotlightTarget.TOP_COMMAND_PROMPT -> Alignment.Center
                SpotlightTarget.FILTER_MATRIX -> Alignment.Center
                SpotlightTarget.FOCUS_TIMER_CARD -> Alignment.BottomCenter
                SpotlightTarget.ANALYTICS_VELOCITY -> Alignment.BottomCenter
                SpotlightTarget.GITHUB_SYNC_CARD -> Alignment.Center
                SpotlightTarget.THEME_PICKER_ACTION -> Alignment.Center
            }
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    (fadeIn(tween(220)) + scaleIn(initialScale = 0.94f, animationSpec = spring()))
                        .togetherWith(fadeOut(tween(160)) + scaleOut(targetScale = 0.96f))
                },
                label = "tooltip_animated_card",
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* Prevent clicks inside card from bubbling to scrim */ }
                    )
            ) { step ->
                CoachMarkTooltipCard(
                    step = step,
                    stepNumber = stepIndex + 1,
                    totalSteps = steps.size,
                    onNext = onNextStep,
                    onPrevious = onPreviousStep,
                    onDismiss = onDismiss,
                    hasPrevious = stepIndex > 0
                )
            }
        }
    }
}

@Composable
private fun TargetSpotlightBeacon(
    target: SpotlightTarget,
    accentColor: Color,
    pulseScale: Float,
    pulseAlpha: Float,
    screenWidth: Dp,
    screenHeight: Dp
) {
    // Determine the spotlight position & shape based on target
    when (target) {
        SpotlightTarget.TOP_COMMAND_PROMPT -> {
            // Highlighting the top CLI input area below top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 60.dp)
                    .padding(horizontal = 16.dp)
                    .height(64.dp)
            ) {
                SpotlightBox(
                    accentColor = accentColor,
                    pulseScale = pulseScale,
                    pulseAlpha = pulseAlpha,
                    cornerRadius = 10.dp
                )
            }
        }

        SpotlightTarget.FILTER_MATRIX -> {
            // Highlighting filter chips row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 132.dp)
                    .padding(horizontal = 16.dp)
                    .height(48.dp)
            ) {
                SpotlightBox(
                    accentColor = accentColor,
                    pulseScale = pulseScale,
                    pulseAlpha = pulseAlpha,
                    cornerRadius = 8.dp
                )
            }
        }

        SpotlightTarget.FOCUS_TIMER_CARD -> {
            // Highlighting center timer dial
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 120.dp)
                    .padding(horizontal = 32.dp)
                    .height(240.dp)
            ) {
                SpotlightBox(
                    accentColor = accentColor,
                    pulseScale = pulseScale,
                    pulseAlpha = pulseAlpha,
                    cornerRadius = 24.dp
                )
            }
        }

        SpotlightTarget.ANALYTICS_VELOCITY -> {
            // Highlighting top of analytics chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 80.dp)
                    .padding(horizontal = 16.dp)
                    .height(220.dp)
            ) {
                SpotlightBox(
                    accentColor = accentColor,
                    pulseScale = pulseScale,
                    pulseAlpha = pulseAlpha,
                    cornerRadius = 16.dp
                )
            }
        }

        SpotlightTarget.GITHUB_SYNC_CARD -> {
            // Highlighting sync controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 75.dp)
                    .padding(horizontal = 16.dp)
                    .height(130.dp)
            ) {
                SpotlightBox(
                    accentColor = accentColor,
                    pulseScale = pulseScale,
                    pulseAlpha = pulseAlpha,
                    cornerRadius = 14.dp
                )
            }
        }

        SpotlightTarget.THEME_PICKER_ACTION -> {
            // Highlighting top right action in top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = 12.dp)
                    .padding(end = 46.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Box(modifier = Modifier.size(46.dp)) {
                    SpotlightBox(
                        accentColor = accentColor,
                        pulseScale = pulseScale,
                        pulseAlpha = pulseAlpha,
                        cornerRadius = 23.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun SpotlightBox(
    accentColor: Color,
    pulseScale: Float,
    pulseAlpha: Float,
    cornerRadius: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Expanding radar ripple wave
                val strokeWidth = 2.dp.toPx()
                val inflatedSize = Size(
                    width = size.width * pulseScale,
                    height = size.height * pulseScale
                )
                val offsetX = (size.width - inflatedSize.width) / 2f
                val offsetY = (size.height - inflatedSize.height) / 2f

                drawRoundRect(
                    color = accentColor.copy(alpha = pulseAlpha),
                    topLeft = Offset(offsetX, offsetY),
                    size = inflatedSize,
                    cornerRadius = CornerRadius(cornerRadius.toPx() * pulseScale),
                    style = Stroke(width = strokeWidth)
                )

                // Static glowing border around element
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset.Zero,
                    size = size,
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                    )
                )
            }
    )
}

@Composable
private fun CoachMarkTooltipCard(
    step: CoachMarkStep,
    stepNumber: Int,
    totalSteps: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDismiss: () -> Unit,
    hasPrevious: Boolean
) {
    Card(
        modifier = Modifier
            .widthIn(max = 440.dp)
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.5.dp, step.accentColor.copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header Bar: Badge + Step Indicator + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(step.accentColor.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = step.badge,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = step.accentColor
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "STEP $stepNumber OF $totalSteps",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss Tour",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Feature Title with Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(step.accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = step.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = step.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = step.subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description Body
            Text(
                text = step.description,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Pro Tip Box (Terminal / Code Highlighted)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = step.accentColor,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = step.proTip,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Footer: Progress Dots + Navigation Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress Dots Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(totalSteps) { idx ->
                        val isCurrent = idx == (stepNumber - 1)
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isCurrent) 18.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isCurrent) step.accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                )
                        )
                    }
                }

                // Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (hasPrevious) {
                        OutlinedButton(
                            onClick = onPrevious,
                            modifier = Modifier.height(38.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Previous",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Button(
                        onClick = onNext,
                        modifier = Modifier.height(38.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = step.accentColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (stepNumber == totalSteps) "Finish" else "Next",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (stepNumber == totalSteps) Icons.Default.Check else Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
