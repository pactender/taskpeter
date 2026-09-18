package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Priority
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import com.example.ui.theme.DevEmerald

val CoderEmojiList = listOf(
    "⚡", "🚀", "💻", "🧠", "🎯", "🛠️", "☕", "📦", "🧪", "🌿",
    "🔥", "💡", "🛡️", "💎", "🎨", "📝", "⚙️", "🚢", "🐛", "🔒"
)

@Composable
fun TaskDetailDialog(
    initialTask: TaskEntity? = null,
    defaultRepo: String = "",
    fetchedRepoCategories: List<Pair<String, String>> = emptyList(),
    onDismiss: () -> Unit,
    onSaveTask: (
        title: String,
        description: String,
        category: String,
        priority: String,
        repo: String,
        branch: String,
        estimatedMins: Int,
        dueDate: Long?,
        emoji: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var selectedEmoji by remember { mutableStateOf(initialTask?.emoji ?: "energy") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(initialTask?.category ?: TaskCategory.FEATURE.name) }
    var selectedPriority by remember { mutableStateOf(initialTask?.priority ?: Priority.P1.name) }
    var repo by remember { mutableStateOf(initialTask?.repositoryName ?: defaultRepo) }
    var branch by remember { mutableStateOf(initialTask?.branchName ?: "main") }
    var estimatedMins by remember { mutableIntStateOf(initialTask?.estimatedMinutes ?: 30) }
    var hasDueDate by remember { mutableStateOf(initialTask?.dueDateMillis != null) }
    var showError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Interactive Glyph Badge
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .clickable { showEmojiPicker = !showEmojiPicker },
                            contentAlignment = Alignment.Center
                        ) {
                            TaskPeterThemedGlyph(symbol = selectedEmoji, size = 24.dp, animated = true)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (initialTask == null) "Create Task" else "Edit Task",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "TaskPeter Developer Cockpit",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Collapsible Glyph Picker
                AnimatedVisibility(visible = showEmojiPicker) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "TASKPETER THEMED GLYPH",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TaskPeterGlyphPicker(
                            selectedGlyphKey = selectedEmoji,
                            onGlyphSelected = { key ->
                                selectedEmoji = key
                                showEmojiPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) showError = false
                    },
                    label = { Text("Task Title / Objective") },
                    placeholder = { Text("What needs to be built or fixed?") },
                    singleLine = true,
                    isError = showError && title.isBlank(),
                    supportingText = if (showError && title.isBlank()) {
                        { Text("Task title is required", color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details / Acceptance Criteria (Optional)") },
                    placeholder = { Text("Commit scope, edge cases, implementation notes...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Priority Selection
                Text(
                    text = "PRIORITY",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Priority.entries.forEach { pri ->
                        FilterChip(
                            selected = selectedPriority == pri.name,
                            onClick = { selectedPriority = pri.name },
                            label = { Text(pri.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(pri.colorHex).copy(alpha = 0.2f),
                                selectedLabelColor = Color(pri.colorHex)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection
                Text(
                    text = "CATEGORY",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val defaultCategories = TaskCategory.entries.map { it.name to it.label }
                    val allCategories = defaultCategories + fetchedRepoCategories

                    allCategories.forEach { (catName, catLabel) ->
                        FilterChip(
                            selected = selectedCategory == catName,
                            onClick = {
                                selectedCategory = catName
                                if (initialTask == null && branch == "main") {
                                    val catEnum = try { TaskCategory.valueOf(catName) } catch (e: Exception) { null }
                                    val prefix = when (catEnum) {
                                        TaskCategory.BUGFIX -> "fix"
                                        TaskCategory.REFACTOR -> "refactor"
                                        TaskCategory.DEVOPS -> "infra"
                                        TaskCategory.DOCS -> "docs"
                                        null -> "feat"
                                        else -> "feat"
                                    }
                                    branch = "$prefix/new-task"
                                }
                            },
                            label = { Text(catLabel, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Repository & Branch Inputs
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = repo,
                        onValueChange = { repo = it },
                        label = { Text("Repo (Optional)") },
                        placeholder = { Text("owner/repo") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = branch,
                        onValueChange = { branch = it },
                        label = { Text("Branch") },
                        placeholder = { Text("main") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sprint Time
                Text(
                    text = "ESTIMATED FOCUS SPRINT: ${estimatedMins}m",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 25, 45, 60, 90).forEach { mins ->
                        FilterChip(
                            selected = estimatedMins == mins,
                            onClick = { estimatedMins = mins },
                            label = { Text("${mins}m") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Schedule Due Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Schedule Due Date (Today)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FilterChip(
                        selected = hasDueDate,
                        onClick = { hasDueDate = !hasDueDate },
                        label = { Text(if (hasDueDate) "Active" else "None") }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val dueDateMillis = if (hasDueDate) (initialTask?.dueDateMillis ?: (System.currentTimeMillis() + 86400000L)) else null
                                onSaveTask(
                                    title.trim(),
                                    description.trim(),
                                    selectedCategory,
                                    selectedPriority,
                                    repo.trim(),
                                    branch.trim().ifBlank { "main" },
                                    estimatedMins,
                                    dueDateMillis,
                                    selectedEmoji
                                )
                                onDismiss()
                            } else {
                                showError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (initialTask == null) "Create Task" else "Save Changes",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
