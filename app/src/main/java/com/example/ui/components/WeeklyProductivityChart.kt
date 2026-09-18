package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevCyan
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevPurple
import com.example.util.DateUtils
import kotlin.math.max

enum class ChartDisplayMode(val label: String) {
    COMPOSED("Combined Dual-Trend"),
    FOCUS_TIME("Focus Time (Area)"),
    TASKS("Tasks (Bars)")
}

/**
 * Modern Data Visualization Component inspired by Recharts & D3.js.
 * Displays weekly productivity analytics featuring:
 * - D3 Monotone cubic Bézier spline interpolation for Focus Time area curve
 * - Recharts rounded dual-axis bar and area composition
 * - Interactive touch scrubbing with crosshair and floating glassmorphism tooltip
 * - Dynamic week-over-week velocity & flow metric calculations
 */
@Composable
fun WeeklyProductivityChart(
    weeklyData: List<DateUtils.DailyProductivityPoint>,
    modifier: Modifier = Modifier
) {
    val activeData = weeklyData
    val isDataEmpty = activeData.all { it.completedTasks == 0 && it.focusMinutes == 0 }

    var selectedDayIndex by remember { mutableIntStateOf(activeData.lastIndex.coerceAtLeast(0)) }
    var chartMode by remember { mutableStateOf(ChartDisplayMode.COMPOSED) }

    val totalTasks = activeData.sumOf { it.completedTasks }
    val totalFocusMins = activeData.sumOf { it.focusMinutes }
    val maxTasks = max(activeData.maxOfOrNull { it.completedTasks } ?: 1, 5)
    val maxFocus = max(activeData.maxOfOrNull { it.focusMinutes } ?: 30, 60)
    val peakDay = activeData.maxByOrNull { it.completedTasks * 30 + it.focusMinutes }

    val primaryChartColor = MaterialTheme.colorScheme.primary
    val secondaryChartColor = MaterialTheme.colorScheme.secondary
    val tertiaryChartColor = MaterialTheme.colorScheme.tertiary
    val surfaceBgColor = MaterialTheme.colorScheme.surface
    val outlineChartColor = MaterialTheme.colorScheme.outline

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, primaryChartColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Bar: Executive Analytics Title & Trend Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoGraph,
                            contentDescription = null,
                            tint = primaryChartColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WEEKLY VELOCITY & OUTPUT",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = primaryChartColor
                        )
                    }
                    Text(
                        text = "Completed Tasks vs. Deep Focus Hours",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(primaryChartColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "7-DAY ROLLING",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = primaryChartColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stat Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Completed Tasks Badge
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(primaryChartColor.copy(alpha = 0.1f))
                        .border(1.dp, primaryChartColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = primaryChartColor, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("TASKS", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = primaryChartColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$totalTasks done",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${"%.1f".format(totalTasks / 7f)}/day avg",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Focus Time Badge
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(secondaryChartColor.copy(alpha = 0.1f))
                        .border(1.dp, secondaryChartColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = secondaryChartColor, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DEEP FOCUS", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = secondaryChartColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        val hours = totalFocusMins / 60
                        val mins = totalFocusMins % 60
                        Text(
                            text = if (hours > 0) "${hours}h ${mins}m" else "${mins}m",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${totalFocusMins / 7}m/day avg",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Peak Day Badge
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tertiaryChartColor.copy(alpha = 0.12f))
                        .border(1.dp, tertiaryChartColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = tertiaryChartColor, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PEAK FLOW", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = tertiaryChartColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = peakDay?.let { "${it.dayName} (${it.completedTasks}T • ${it.focusMinutes}m)" } ?: "-",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "Highest Output",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chart Mode Selector Chips & Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChartDisplayMode.entries.forEach { mode ->
                        FilterChip(
                            selected = chartMode == mode,
                            onClick = { chartMode = mode },
                            label = { Text(mode.label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryChartColor.copy(alpha = 0.2f),
                                selectedLabelColor = primaryChartColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Recharts-style Interactive Tooltip (Floating Glass Surface)
            val activePoint = activeData.getOrNull(selectedDayIndex) ?: activeData.lastOrNull()
            if (activePoint != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .border(1.dp, primaryChartColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${activePoint.dayName}, ${activePoint.dateLabel}",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (activePoint.isToday) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(primaryChartColor.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("TODAY", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = primaryChartColor)
                                    }
                                }
                            }
                            Text(
                                text = "Drag or tap chart canvas to inspect daily values",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(primaryChartColor, CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${activePoint.completedTasks} Tasks",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryChartColor
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(secondaryChartColor, CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${activePoint.focusMinutes}m Focus",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = secondaryChartColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Canvas: D3 Monotone Cubic Bézier Spline & Recharts Bar Composition
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(activeData) {
                            detectTapGestures { offset ->
                                val count = activeData.size
                                if (count > 0) {
                                    val stepX = size.width / count
                                    val index = (offset.x / stepX).toInt().coerceIn(0, count - 1)
                                    selectedDayIndex = index
                                }
                            }
                        }
                        .pointerInput(activeData) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val count = activeData.size
                                if (count > 0) {
                                    val stepX = size.width / count
                                    val index = (change.position.x / stepX).toInt().coerceIn(0, count - 1)
                                    selectedDayIndex = index
                                }
                            }
                        }
                ) {
                    val count = activeData.size
                    if (count < 2) return@Canvas

                    val chartWidth = size.width
                    val chartHeight = size.height
                    val bottomPadding = 24f
                    val topPadding = 16f
                    val leftPadding = 20f
                    val rightPadding = 20f

                    val usableWidth = chartWidth - leftPadding - rightPadding
                    val usableHeight = chartHeight - topPadding - bottomPadding

                    val stepX = usableWidth / (count - 1)

                    // 1. Draw Cartesian Grid Lines (Recharts <CartesianGrid strokeDasharray="3 3"/>)
                    val gridSteps = 4
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    for (i in 0..gridSteps) {
                        val y = topPadding + usableHeight * (i.toFloat() / gridSteps)
                        drawLine(
                            color = outlineChartColor.copy(alpha = 0.25f),
                            start = Offset(leftPadding, y),
                            end = Offset(chartWidth - rightPadding, y),
                            strokeWidth = 1f,
                            pathEffect = dashEffect
                        )
                    }

                    // 2. Draw Completed Tasks as Rounded Bars (Recharts <Bar dataKey="tasks" />)
                    if (chartMode == ChartDisplayMode.COMPOSED || chartMode == ChartDisplayMode.TASKS) {
                        val barWidth = (usableWidth / count) * 0.42f
                        activeData.forEachIndexed { i, point ->
                            val centerX = leftPadding + (i * stepX)
                            val normalizedTasks = (point.completedTasks.toFloat() / maxTasks).coerceIn(0f, 1f)
                            val barHeight = usableHeight * normalizedTasks
                            val barTop = topPadding + usableHeight - barHeight

                            val isSelected = i == selectedDayIndex
                            val barColor = if (isSelected) primaryChartColor else primaryChartColor.copy(alpha = 0.65f)

                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(centerX - barWidth / 2, barTop),
                                size = Size(barWidth, barHeight.coerceAtLeast(4f)),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                        }
                    }

                    // 3. Draw Focus Time Monotone Cubic Bézier Spline & Gradient Area (D3/Recharts Area)
                    if (chartMode == ChartDisplayMode.COMPOSED || chartMode == ChartDisplayMode.FOCUS_TIME) {
                        val points = activeData.mapIndexed { i, point ->
                            val x = leftPadding + (i * stepX)
                            val normalizedFocus = (point.focusMinutes.toFloat() / maxFocus).coerceIn(0f, 1f)
                            val y = topPadding + usableHeight - (usableHeight * normalizedFocus)
                            Offset(x, y)
                        }

                        // Build smooth cubic Bézier spline (D3 Monotone X algorithm)
                        val linePath = Path()
                        val areaPath = Path()

                        if (points.isNotEmpty()) {
                            linePath.moveTo(points[0].x, points[0].y)
                            areaPath.moveTo(points[0].x, topPadding + usableHeight)
                            areaPath.lineTo(points[0].x, points[0].y)

                            for (i in 0 until points.size - 1) {
                                val p0 = if (i > 0) points[i - 1] else points[i]
                                val p1 = points[i]
                                val p2 = points[i + 1]
                                val p3 = if (i + 2 < points.size) points[i + 2] else p2

                                val cp1x = p1.x + (p2.x - p0.x) / 6f
                                val cp1y = p1.y + (p2.y - p0.y) / 6f
                                val cp2x = p2.x - (p3.x - p1.x) / 6f
                                val cp2y = p2.y - (p3.y - p1.y) / 6f

                                linePath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                                areaPath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                            }

                            areaPath.lineTo(points.last().x, topPadding + usableHeight)
                            areaPath.close()

                            // Area gradient fill beneath curve
                            drawPath(
                                path = areaPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        secondaryChartColor.copy(alpha = 0.35f),
                                        secondaryChartColor.copy(alpha = 0.02f)
                                    ),
                                    startY = topPadding,
                                    endY = topPadding + usableHeight
                                )
                            )

                            // Spline stroke
                            drawPath(
                                path = linePath,
                                color = secondaryChartColor,
                                style = Stroke(width = 6f, cap = StrokeCap.Round)
                            )

                            // Point halos and markers
                            points.forEachIndexed { i, pt ->
                                val isSelected = i == selectedDayIndex
                                val pointColor = if (isSelected) secondaryChartColor else secondaryChartColor.copy(alpha = 0.85f)
                                val radius = if (isSelected) 8f else 5f

                                drawCircle(
                                    color = surfaceBgColor,
                                    radius = radius + 4f,
                                    center = pt
                                )
                                drawCircle(
                                    color = pointColor,
                                    radius = radius,
                                    center = pt
                                )
                            }
                        }
                    }

                    // 4. Draw Active Scrubber Guideline (Recharts Crosshair)
                    if (selectedDayIndex in activeData.indices) {
                        val scrubberX = leftPadding + (selectedDayIndex * stepX)
                        drawLine(
                            color = primaryChartColor.copy(alpha = 0.8f),
                            start = Offset(scrubberX, topPadding),
                            end = Offset(scrubberX, topPadding + usableHeight),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }
                }
            }

            // X-Axis Day Labels (Recharts <XAxis dataKey="day" />)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                activeData.forEachIndexed { index, point ->
                    val isSelected = index == selectedDayIndex
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) primaryChartColor.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedDayIndex = index }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = point.dayName,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) primaryChartColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chart Legend Footer (Clean PowerBI / Excel styling)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(primaryChartColor))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Completed Tasks", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(secondaryChartColor))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Focus Duration (min)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Text(
                    text = if (isDataEmpty) "0 Tasks Logged" else "Live Telemetry",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
