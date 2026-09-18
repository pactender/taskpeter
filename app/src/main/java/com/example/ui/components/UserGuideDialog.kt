package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevCyan
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevPurple
import com.example.ui.theme.DevRose

private data class GuideSlide(
    val badge: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val colorProvider: @Composable () -> Color,
    val bullets: List<String>,
    val codeSnippet: String? = null
)

@Composable
fun UserGuideDialog(
    onDismiss: () -> Unit,
    onComplete: () -> Unit,
    onStartCoachTour: () -> Unit = {}
) {
    val slides = remember {
        listOf(
            GuideSlide(
                badge = "STEP 1 • AGILE TASK MANAGEMENT",
                title = "Developer Task Flow",
                subtitle = "Manage issues with smart tags, keyboard commands, and conventional commits.",
                icon = Icons.Default.Code,
                colorProvider = { MaterialTheme.colorScheme.primary },
                bullets = listOf(
                    "Include #feat, #bug, #refactor in titles for instant categorizing.",
                    "Auto-generate conventional commits: feat(auth): add token caching.",
                    "Quick command palette to create, search, and jump to tasks."
                ),
                codeSnippet = "git checkout -b feat/taskpeter-core\ngit commit -m \"feat: implement sprint audio\""
            ),
            GuideSlide(
                badge = "STEP 2 • DEEP WORK FOCUS",
                title = "Sprint Timer & Audio Engine",
                subtitle = "Eliminate context switching with Pomodoro sprints and real-time noise synthesis.",
                icon = Icons.Default.Timer,
                colorProvider = { MaterialTheme.colorScheme.secondary },
                bullets = listOf(
                    "Select 25m, 50m, or 90m deep focus intervals.",
                    "Synthesize binaural beats, soft rain, or white noise natively with zero streaming lag.",
                    "Earn streak days and log sprint velocity automatically."
                ),
                codeSnippet = "AudioEngine.startSynthesis(ToneType.BINAURAL_ALPHA)\nFocusSession: 25:00 [STREAK: 7 DAYS]"
            ),
            GuideSlide(
                badge = "STEP 3 • GITHUB INTEGRATION",
                title = "Repositories As Categories",
                subtitle = "Retrofit REST API integration with personal tokens and verified developer badges.",
                icon = Icons.Default.CloudSync,
                colorProvider = { MaterialTheme.colorScheme.tertiary },
                bullets = listOf(
                    "Connect your GitHub username or token with verified badge and avatar.",
                    "Real repositories turn into clickable category filter chips (#repo-name).",
                    "Track commit history logs with copyable terminal shell snippets."
                ),
                codeSnippet = "GET https://api.github.com/users/{user}/repos\n-> 18 Repositories Loaded as Task Categories"
            ),
            GuideSlide(
                badge = "STEP 4 • METRICS & CALENDAR",
                title = "Git Heatmap & Watchdog",
                subtitle = "Visual workload velocity, 28-day contribution map, and deadline alerts.",
                icon = Icons.Default.AutoGraph,
                colorProvider = { MaterialTheme.colorScheme.secondary },
                bullets = listOf(
                    "14-Day agenda timeline keeps track of upcoming deadlines.",
                    "Interactive 28-day GitHub-styled green contribution heatmap.",
                    "Automatic category distribution breakdown and velocity metrics."
                ),
                codeSnippet = "Weekly Velocity: +24 Tasks Completed\nOverdue Watchdog: 0 Pending Blockers"
            ),
            GuideSlide(
                badge = "STEP 5 • PRIVACY GUARANTEED",
                title = "Zero Data Collection Guarantee",
                subtitle = "100% offline-first. Your code and thoughts never leak to any third party.",
                icon = Icons.Default.Security,
                colorProvider = { MaterialTheme.colorScheme.primary },
                bullets = listOf(
                    "Local Room SQLite database: All data is stored in your private device sandbox.",
                    "Zero telemetry, zero analytical beaconing, zero ad networks.",
                    "No external cloud servers ever see your tasks, notes, or credentials.",
                    "You retain absolute sovereignty over your developer productivity."
                ),
                codeSnippet = "Privacy Audit: 0 Trackers Detected\nLocal SQLite: Encrypted Device Storage"
            )
        )
    }

    var currentSlideIndex by remember { mutableIntStateOf(0) }
    val currentSlide = slides[currentSlideIndex]
    val activeAccentColor = currentSlide.colorProvider()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Brush.linearGradient(listOf(activeAccentColor.copy(alpha = 0.8f), Color.Transparent))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(activeAccentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentSlide.badge,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeAccentColor
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Guide",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animated Slide Content
                AnimatedContent(
                    targetState = currentSlide,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "GuideSlideAnimation"
                ) { slide ->
                    val slideAccent = slide.colorProvider()
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(slideAccent.copy(alpha = 0.12f))
                                    .border(1.5.dp, slideAccent.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = slide.icon,
                                    contentDescription = null,
                                    tint = slideAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = slide.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = slide.subtitle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Bullet Points
                        slide.bullets.forEach { bullet ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 5.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(slideAccent)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = bullet,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 17.sp
                                )
                            }
                        }

                        // Code / Terminal Box
                        if (slide.codeSnippet != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Terminal,
                                                contentDescription = null,
                                                tint = slideAccent,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "DEV TERMINAL",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = slideAccent
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error))
                                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
                                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = slide.codeSnippet,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Launch Live Animated Coach Marks Tour
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = {
                            onDismiss()
                            onStartCoachTour()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = activeAccentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Explore via live animated Coach Marks Tour →",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = activeAccentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Navigation & Progress Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Indicator Dots
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        slides.indices.forEach { index ->
                            val isSelected = index == currentSlideIndex
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 18.dp else 6.dp, 6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (isSelected) activeAccentColor
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }

                    // Navigation Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (currentSlideIndex > 0) {
                            OutlinedButton(
                                onClick = { currentSlideIndex-- },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Prev", fontSize = 11.sp)
                            }
                        }

                        if (currentSlideIndex < slides.size - 1) {
                            Button(
                                onClick = { currentSlideIndex++ },
                                colors = ButtonDefaults.buttonColors(containerColor = activeAccentColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Next", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    onComplete()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Get Started", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
