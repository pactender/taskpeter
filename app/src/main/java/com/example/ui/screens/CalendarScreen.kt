package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStatus
import com.example.ui.MainViewModel
import com.example.ui.components.CategoryBadge
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.TaskEmojiBadge
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevCyan
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevRose
import com.example.util.DateUtils
import com.example.util.NotificationHelper
import java.util.Calendar

@Composable
fun CalendarScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedCalendarDate.collectAsStateWithLifecycle()

    val now = System.currentTimeMillis()
    val startOfSelected = DateUtils.getStartOfDay(selectedDate)
    val endOfSelected = DateUtils.getEndOfDay(selectedDate)

    // Filter tasks due on this date
    val tasksOnDate = allTasks.filter { task ->
        val due = task.dueDateMillis
        due != null && due in startOfSelected..endOfSelected
    }

    // Overdue tasks
    val overdueTasks = allTasks.filter { task ->
        val due = task.dueDateMillis
        due != null && due < startOfSelected && task.status != TaskStatus.DONE.name
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Calendar Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SCHEDULE & DEADLINES",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = DateUtils.formatFullDate(selectedDate),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 14-day horizontal date strip
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val dayFormat = remember { java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault()) }
                    val dayOfMonthFormat = remember { java.text.SimpleDateFormat("dd", java.util.Locale.getDefault()) }
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -3)

                    for (i in 0..13) {
                        val dayTime = cal.timeInMillis
                        val startOfDay = DateUtils.getStartOfDay(dayTime)
                        val endOfDay = DateUtils.getEndOfDay(dayTime)
                        val isSelected = startOfDay == startOfSelected
                        val isToday = DateUtils.isToday(dayTime)

                        val dayOfWeek = dayFormat.format(cal.time)
                        val dayOfMonth = dayOfMonthFormat.format(cal.time)

                        // Count tasks on this day
                        val count = allTasks.count {
                            it.dueDateMillis != null && it.dueDateMillis in startOfDay..endOfDay
                        }

                        Box(
                            modifier = Modifier
                                .width(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else if (isToday) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else if (isToday) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                    else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setCalendarDate(dayTime) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayOfWeek.uppercase(),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = dayOfMonth,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (count > 0) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(MaterialTheme.colorScheme.tertiary)
                                    )
                                }
                            }
                        }

                        cal.add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
            }
        }

        // Overdue Alert Section (if any)
        if (overdueTasks.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OVERDUE DEADLINES (${overdueTasks.size})",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        overdueTasks.take(3).forEach { t ->
                            Text(
                                text = "• ${t.title} (${t.repositoryName})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Section header for selected date
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCHEDULED ON ${DateUtils.formatShortDate(selectedDate).uppercase()}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${tasksOnDate.size} Tasks",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Tasks scheduled on selected date
        if (tasksOnDate.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No deadlines scheduled for this date. Enjoy deep coding!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(tasksOnDate, key = { it.id }) { task ->
                CalendarTaskCard(
                    task = task,
                    onSendReminder = {
                        NotificationHelper.showTaskReminderNotification(
                            viewModel.getApplication(),
                            task.title,
                            DateUtils.formatShortDate(task.dueDateMillis ?: selectedDate)
                        )
                        viewModel.showToast("Reminder notification set for '${task.title}'")
                    },
                    onMarkDone = {
                        viewModel.updateTaskStatus(task, TaskStatus.DONE)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun CalendarTaskCard(
    task: TaskEntity,
    onSendReminder: () -> Unit,
    onMarkDone: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TaskEmojiBadge(emoji = task.emoji)
                    PriorityBadge(priorityName = task.priority)
                    CategoryBadge(categoryName = task.category)
                    StatusBadge(statusName = task.status)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = task.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Repo: ${task.repositoryName} • Est: ${task.estimatedMinutes}m",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Test Notification Reminder icon
                IconButton(onClick = onSendReminder) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Send Reminder",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Complete action
                IconButton(onClick = onMarkDone) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Done",
                        tint = if (task.status == "DONE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
