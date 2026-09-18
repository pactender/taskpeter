package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevCyan
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevPurple
import com.example.ui.theme.DevRose
import com.example.ui.components.WeeklyProductivityChart
import com.example.ui.components.bounceClick
import com.example.util.DateUtils
import com.example.util.ExportHelper

@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val allSprints by viewModel.allSprints.collectAsStateWithLifecycle()
    val allCommits by viewModel.allCommits.collectAsStateWithLifecycle()

    val completedTasks = allTasks.filter { it.status == "DONE" }
    val totalFocusMins = allSprints.sumOf { it.durationMinutes }

    // Combine task completion & commit timestamps for 28-day contribution matrix
    val activityTimestamps = completedTasks.mapNotNull { it.completedAtMillis } + allCommits.map { it.timestamp }
    val contributionDays = remember(activityTimestamps) {
        DateUtils.calculatePast28DaysMatrix(activityTimestamps)
    }

    val currentStreak = remember(activityTimestamps) {
        DateUtils.calculateConsecutiveStreak(activityTimestamps)
    }

    val weeklyProductivity = remember(completedTasks, allSprints, allCommits) {
        DateUtils.calculateWeeklyProductivity(
            completedTaskTimestamps = completedTasks.mapNotNull { it.completedAtMillis },
            sessionTimestampsAndMinutes = allSprints.map { it.completedTimestamp to it.durationMinutes },
            commitTimestamps = allCommits.map { it.timestamp }
        )
    }

    var selectedHeatmapDay by remember { mutableStateOf<DateUtils.DayContribution?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DEV PRODUCTIVITY & HABITS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .bounceClick {
                            val report = viewModel.exportMarkdownReport()
                            ExportHelper.shareText(context, report, "TaskPeter Standup Summary")
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SHARE REPORT", color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp)
                    }
                }
            }
        }

        // Long-term Habit Formation & Streak Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (currentStreak > 0) "$currentStreak-DAY CODE STREAK" else "0-DAY STREAK",
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (currentStreak > 0) "Daily consistency active • Momentum maintained" else "Complete tasks or sprints to build your streak",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val compRate = if (allTasks.isNotEmpty()) (completedTasks.size * 100 / allTasks.size) else 0
                        Text(
                            text = if (allTasks.isNotEmpty()) "$compRate% DONE" else "ACTIVE",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // GitHub Contribution Heatmap Card (28 Days)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONTRIBUTION HEATMAP (28 DAYS)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "${activityTimestamps.size} contributions",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val heatPrimary = MaterialTheme.colorScheme.primary
                    val heatEmpty = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    val heatLegendColors = listOf(
                        heatEmpty,
                        heatPrimary.copy(alpha = 0.35f),
                        heatPrimary.copy(alpha = 0.6f),
                        heatPrimary.copy(alpha = 0.85f),
                        heatPrimary
                    )

                    // 7 Columns x 4 Rows or 4 Weeks x 7 Days Grid
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (row in 0 until 4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (col in 0 until 7) {
                                    val index = row * 7 + col
                                    if (index < contributionDays.size) {
                                        val day = contributionDays[index]
                                        val cellColor = when (day.level) {
                                            0 -> heatEmpty
                                            1 -> heatPrimary.copy(alpha = 0.35f)
                                            2 -> heatPrimary.copy(alpha = 0.6f)
                                            3 -> heatPrimary.copy(alpha = 0.85f)
                                            else -> heatPrimary
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(cellColor)
                                                .border(
                                                    1.dp,
                                                    if (selectedHeatmapDay?.dateIso == day.dateIso) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                                    RoundedCornerShape(3.dp)
                                                )
                                                .clickable { selectedHeatmapDay = day }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Heatmap Legend & Tooltip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedHeatmapDay?.let { "${it.shortLabel}: ${it.count} commits & tasks" } ?: "Tap square for date info",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Less", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            heatLegendColors.forEach { c ->
                                Box(modifier = Modifier.size(10.dp).background(c, RoundedCornerShape(2.dp)))
                            }
                            Text("More", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Weekly Productivity Analytics (D3 / Recharts Data Visualization Component)
        item {
            WeeklyProductivityChart(weeklyData = weeklyProductivity)
        }

        // Category Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TASK DISTRIBUTION BY DISCIPLINE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val featCount = allTasks.count { it.category == "FEATURE" }
                    val bugCount = allTasks.count { it.category == "BUGFIX" }
                    val refactorCount = allTasks.count { it.category == "REFACTOR" }
                    val infraCount = allTasks.count { it.category == "DEVOPS" }
                    val total = (featCount + bugCount + refactorCount + infraCount).coerceAtLeast(1)

                    CategoryBar("Features", featCount, featCount.toFloat() / total, DevEmerald)
                    CategoryBar("Bugfixes", bugCount, bugCount.toFloat() / total, DevRose)
                    CategoryBar("Refactoring", refactorCount, refactorCount.toFloat() / total, DevCyan)
                    CategoryBar("DevOps & Infra", infraCount, infraCount.toFloat() / total, DevAmber)
                }
            }
        }

        // Actionable Developer Insights
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ACTIONABLE PRODUCTIVITY INSIGHTS",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    val avgSprint = if (allSprints.isNotEmpty()) allSprints.map { it.durationMinutes }.average().toInt() else 0
                    val compRate = if (allTasks.isNotEmpty()) (completedTasks.size * 100 / allTasks.size) else 0

                    Text(
                        text = "• Sprint Discipline: $avgSprint min average sprint length across ${allSprints.size} recorded sessions ($totalFocusMins mins total).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Execution Velocity: $compRate% task completion rate (${completedTasks.size} completed of ${allTasks.size} tasks in backlog).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Git History: ${allCommits.size} automated commits generated and logged across active repositories.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun CategoryBar(label: String, count: Int, ratio: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("$count tasks (${(ratio * 100).toInt()}%)", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
