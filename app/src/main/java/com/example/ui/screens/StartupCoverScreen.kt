package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.MainViewModel
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevCyan
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevPurple
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity Cinematic Animated Cover & Interactive Terms Screen.
 * Inspired by luxury editorial interfaces with a dynamic flowing geometric stream.
 */
@Composable
fun StartupCoverScreen(
    viewModel: MainViewModel,
    onEnterWorkspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    var termsAccepted by remember { mutableStateOf(false) }
    var showTermsModal by remember { mutableStateOf(false) }
    var showTermsValidationWarning by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Interactive pointer deflection for the dynamic canvas stream
    var touchX by remember { mutableFloatStateOf(-1f) }
    var touchY by remember { mutableFloatStateOf(-1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "CoverInfiniteTransition")
    val streamProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "StreamProgress"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        touchX = offset.x
                        touchY = offset.y
                    },
                    onDrag = { change, _ ->
                        touchX = change.position.x
                        touchY = change.position.y
                    },
                    onDragEnd = {
                        touchX = -1f
                        touchY = -1f
                    },
                    onDragCancel = {
                        touchX = -1f
                        touchY = -1f
                    }
                )
            }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // --- Layer 1: Ambient Atmospheric Glows ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .offset(x = 20.dp, y = 80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                DevCyan.copy(alpha = 0.22f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 60.dp, y = 60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                DevEmerald.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                DevPurple.copy(alpha = 0.14f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // --- Layer 2: Custom Real-Time Generative Vector Stream Canvas ---
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawCinematicCoverStream(
                width = size.width,
                height = size.height,
                progress = streamProgress,
                pulse = pulseScale,
                touchX = touchX,
                touchY = touchY,
                primaryColor = DevEmerald,
                secondaryColor = DevCyan,
                tertiaryColor = DevPurple
            )
        }

        // --- Layer 3: Editorial Typography, Badges & Interactive Controls ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header: System Spec & Local-First Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(DevEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CORE ARCHITECTURE v2.4",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, DevEmerald.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .background(DevEmerald.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "ZERO TELEMETRY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = DevEmerald
                    )
                }
            }

            // Center Branding Section: Editorial Title & Cockpit Mission
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                // Micro Category Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DEVELOPER WORKSPACE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Editorial Title
                Text(
                    text = "TaskPeter",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 48.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Refined Subtitle
                Text(
                    text = "The developer's focus cockpit. Precision sprints, authentic Git synchronization, and distraction-free execution.",
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Technical Pillar Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PillarChip(label = "Encrypted SQLite", icon = Icons.Default.Storage)
                    PillarChip(label = "Direct GitHub TLS", icon = Icons.Default.Security)
                    PillarChip(label = "Offline-First", icon = Icons.Default.Shield)
                }
            }

            // Bottom Section: Terms Acceptance & Initialization Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                // Terms of Service Acceptance Card
                val cardBorderColor by animateColorAsState(
                    targetValue = if (showTermsValidationWarning && !termsAccepted) DevAmber else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    label = "TermsBorder"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = termsAccepted,
                                onCheckedChange = {
                                    termsAccepted = it
                                    if (it) showTermsValidationWarning = false
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = DevEmerald,
                                    checkmarkColor = Color.Black
                                ),
                                modifier = Modifier.testTag("terms_checkbox")
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "I accept the Developer Terms & Security Policy",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Read Full Terms",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DevCyan,
                                        modifier = Modifier
                                            .clickable { showTermsModal = true }
                                            .padding(vertical = 2.dp)
                                    )
                                    Text(
                                        text = " • Local SQLite & Direct TLS",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showTermsModal = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Gavel,
                                    contentDescription = "Inspect Terms of Service",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (showTermsValidationWarning && !termsAccepted) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Please acknowledge terms to initialize your workspace.",
                                fontSize = 11.sp,
                                color = DevAmber,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary High-Contrast Pill CTA: "INITIALIZE WORKSPACE  →"
                Button(
                    onClick = {
                        if (termsAccepted) {
                            onEnterWorkspace()
                        } else {
                            showTermsValidationWarning = true
                            coroutineScope.launch {
                                shakeOffset.animateTo(12f, tween(50))
                                shakeOffset.animateTo(-12f, tween(50))
                                shakeOffset.animateTo(6f, tween(50))
                                shakeOffset.animateTo(0f, tween(50))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("initialize_workspace_button"),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (termsAccepted) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INITIALIZE WORKSPACE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp,
                            color = if (termsAccepted) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (termsAccepted) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal Terms & Conditions Dialog
    if (showTermsModal) {
        TermsAndConditionsModal(
            onDismiss = { showTermsModal = false },
            onAccept = {
                termsAccepted = true
                showTermsValidationWarning = false
                showTermsModal = false
            }
        )
    }
}

@Composable
private fun PillarChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Procedural Dynamic Vector Stream:
 * Draws undulating cubic Bézier ribbons, glowing radar nodes, crystalline polygons,
 * and micro code nodes with harmonic motion.
 */
private fun DrawScope.drawCinematicCoverStream(
    width: Float,
    height: Float,
    progress: Float,
    pulse: Float,
    touchX: Float,
    touchY: Float,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color
) {
    val streamPath = Path()
    val startX = width * 0.08f
    val startY = height * 0.95f

    // Dynamic deflection if touched
    val touchDeflectX = if (touchX > 0) (touchX - width * 0.5f) * 0.15f else 0f
    val touchDeflectY = if (touchY > 0) (touchY - height * 0.5f) * 0.15f else 0f

    val ctrl1X = width * 0.25f + sin(progress * 6.28318f) * 35f + touchDeflectX
    val ctrl1Y = height * 0.68f + cos(progress * 6.28318f) * 35f + touchDeflectY

    val ctrl2X = width * 0.72f - cos(progress * 6.28318f) * 45f + touchDeflectX
    val ctrl2Y = height * 0.38f - sin(progress * 6.28318f) * 45f + touchDeflectY

    val endX = width * 0.92f
    val endY = height * 0.08f

    streamPath.moveTo(startX, startY)
    streamPath.cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, endX, endY)

    // Draw main glowing ribbon
    val ribbonBrush = Brush.linearGradient(
        colors = listOf(
            primaryColor.copy(alpha = 0.0f),
            primaryColor.copy(alpha = 0.55f),
            secondaryColor.copy(alpha = 0.65f),
            tertiaryColor.copy(alpha = 0.55f),
            tertiaryColor.copy(alpha = 0.0f)
        ),
        start = Offset(startX, startY),
        end = Offset(endX, endY)
    )

    // Ambient diffuse stroke
    drawPath(
        path = streamPath,
        brush = ribbonBrush,
        style = Stroke(
            width = 12f * pulse,
            cap = StrokeCap.Round
        )
    )

    // Crisp inner ribbon core
    drawPath(
        path = streamPath,
        brush = Brush.linearGradient(
            colors = listOf(primaryColor, secondaryColor, tertiaryColor),
            start = Offset(startX, startY),
            end = Offset(endX, endY)
        ),
        style = Stroke(
            width = 2.5f,
            cap = StrokeCap.Round
        )
    )

    // Parallel dashed satellite trail
    val satellitePath = Path()
    satellitePath.moveTo(startX - 18f, startY + 12f)
    satellitePath.cubicTo(ctrl1X - 25f, ctrl1Y + 20f, ctrl2X - 25f, ctrl2Y + 20f, endX - 15f, endY + 10f)

    drawPath(
        path = satellitePath,
        color = secondaryColor.copy(alpha = 0.28f),
        style = Stroke(
            width = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), progress * 80f),
            cap = StrokeCap.Round
        )
    )

    // Render floating geometric nodes along the stream
    val nodeSteps = 7
    for (i in 1..nodeSteps) {
        val t = ((i.toFloat() / (nodeSteps + 1)) + progress * 0.2f) % 1.0f

        // Cubic Bézier calculation at t
        val u = 1 - t
        val nx = u * u * u * startX + 3 * u * u * t * ctrl1X + 3 * u * t * t * ctrl2X + t * t * t * endX
        val ny = u * u * u * startY + 3 * u * u * t * ctrl1Y + 3 * u * t * t * ctrl2Y + t * t * t * endY

        // Harmonic oscillation perpendicular to line
        val perpOffset = sin(t * 12.566f + progress * 6.28318f) * 16f
        val px = nx + perpOffset * 0.6f
        val py = ny - perpOffset * 0.6f

        // Node halo
        drawCircle(
            color = if (i % 2 == 0) primaryColor.copy(alpha = 0.25f) else secondaryColor.copy(alpha = 0.25f),
            radius = 12f * pulse,
            center = Offset(px, py)
        )

        // Node center
        drawCircle(
            color = if (i % 2 == 0) primaryColor else secondaryColor,
            radius = 3.5f,
            center = Offset(px, py)
        )

        // Concentric pulse wave for selected node
        if (i == 3) {
            drawCircle(
                color = tertiaryColor.copy(alpha = 0.35f * (1f - (progress % 1f))),
                radius = 28f * (0.4f + (progress % 1f) * 0.8f),
                center = Offset(px, py),
                style = Stroke(width = 1.5f)
            )
        }

        // Geometric diamond node
        if (i == 5) {
            val dPath = Path()
            val dSize = 8f * pulse
            dPath.moveTo(px, py - dSize)
            dPath.lineTo(px + dSize, py)
            dPath.lineTo(px, py + dSize)
            dPath.lineTo(px - dSize, py)
            dPath.close()

            drawPath(
                path = dPath,
                color = DevAmber,
                style = Stroke(width = 1.5f)
            )
        }
    }
}

/**
 * Detailed Terms of Service & Privacy Policy Modal.
 * Transparent, legally clear, developer-focused documentation.
 */
@Composable
fun TermsAndConditionsModal(
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DevEmerald.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = DevEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TERMS OF SERVICE & PRIVACY",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DevEmerald
                            )
                            Text(
                                text = "Developer Trust & Security Agreement",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Legal Body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(340.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    TermsPillarCard(
                        number = "01",
                        title = "100% Local-First SQLite Storage",
                        description = "All tasks, sprint durations, categories, and Git notes are stored strictly inside your device's encrypted Room database. Nothing is sent to third-party databases without your explicit interaction."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TermsPillarCard(
                        number = "02",
                        title = "Direct GitHub Communication (TLS 1.3)",
                        description = "GitHub credentials, Personal Access Tokens (PATs), and OAuth2 requests connect directly to api.github.com. TaskPeter operates zero proxy servers and never logs or hoards your private tokens."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TermsPillarCard(
                        number = "03",
                        title = "Zero Ad Trackers & Zero Telemetry",
                        description = "We incorporate no advertising SDKs, behavioral fingerprinting, or third-party telemetry. Your development habits and repository names remain private to you."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    TermsPillarCard(
                        number = "04",
                        title = "Complete Data Portability & Rights",
                        description = "You retain 100% ownership of your records. You may export your entire workspace as structured JSON or Markdown standup reports at any time, or purge all local data with a single tap."
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Dismiss", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Button(
                        onClick = onAccept,
                        colors = ButtonDefaults.buttonColors(containerColor = DevEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Accept & Continue", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TermsPillarCard(
    number: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = number,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DevCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
