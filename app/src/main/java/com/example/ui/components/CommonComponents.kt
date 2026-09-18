package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.TaskCategory
import com.example.data.model.TaskStatus
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevCyan
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevPurple
import com.example.ui.theme.DevRose

@Composable
fun PriorityBadge(priorityName: String, modifier: Modifier = Modifier) {
    val priority = try { Priority.valueOf(priorityName) } catch (_: Exception) { Priority.P1 }
    val color = Color(priority.colorHex)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = priority.label,
            color = color,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CategoryBadge(categoryName: String, modifier: Modifier = Modifier) {
    val category = try { TaskCategory.valueOf(categoryName) } catch (_: Exception) { null }
    val (color, icon) = if (category != null) {
        when (category) {
            TaskCategory.BUGFIX -> DevRose to Icons.Default.BugReport
            TaskCategory.FEATURE -> DevEmerald to Icons.Default.Code
            TaskCategory.REFACTOR -> DevCyan to Icons.Default.Speed
            TaskCategory.DEVOPS -> DevAmber to Icons.Default.Handyman
            TaskCategory.CODE_REVIEW -> DevPurple to Icons.Default.RateReview
            TaskCategory.DOCS -> Color(0xFF94A3B8) to Icons.Default.Description
            TaskCategory.TESTING -> Color(0xFF38BDF8) to Icons.Default.Speed
        }
    } else {
        DevPurple to Icons.Default.Storage
    }
    
    val displayText = category?.iconTag ?: "#${categoryName.take(10).lowercase()}"

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = " $displayText",
            color = color,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusBadge(statusName: String, modifier: Modifier = Modifier) {
    val status = try { TaskStatus.valueOf(statusName) } catch (_: Exception) { TaskStatus.TODO }
    val color = Color(status.badgeColorHex)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = " ${status.label}",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun GitCommitBadge(commitHash: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = "#$commitHash",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RepoBranchBadge(repo: String, branch: String, modifier: Modifier = Modifier) {
    if (repo.isBlank() && branch.isBlank()) return
    val cleanRepo = if (repo.isNotBlank()) repo.substringAfterLast("/") else "repo"
    val cleanBranch = if (branch.isNotBlank()) branch else "main"
    Text(
        text = "$cleanRepo:$cleanBranch",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    )
}

@Composable
fun TaskEmojiBadge(emoji: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .border(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        TaskPeterThemedGlyph(symbol = emoji, size = 18.dp, animated = true)
    }
}
