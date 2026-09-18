package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ui.components.TaskPeterThemedGlyph
import com.example.ui.components.bounceClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.github.GitHubService
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStatus
import com.example.ui.MainViewModel
import com.example.ui.components.CategoryBadge
import com.example.ui.components.GitCommitBadge
import com.example.ui.components.PriorityBadge
import com.example.ui.components.QuickCommandPalette
import com.example.ui.components.RepoBranchBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.TaskDetailDialog
import com.example.ui.components.TaskEmojiBadge
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevCyan
import com.example.ui.theme.DevEmerald

@Composable
fun TasksScreen(
    viewModel: MainViewModel,
    onNavigateToSprint: (TaskEntity?) -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalTasksCount.collectAsStateWithLifecycle()
    val completedCount by viewModel.completedTasksCount.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val connectedRepos by viewModel.connectedRepos.collectAsStateWithLifecycle()
    val fetchedRepoCategories by viewModel.fetchedRepoCategories.collectAsStateWithLifecycle()
    val allSprints by viewModel.allSprints.collectAsStateWithLifecycle()

    val totalSprintMinutes = remember(allSprints) { allSprints.sumOf { it.durationMinutes } }

    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var taskForCommitModal by remember { mutableStateOf<TaskEntity?>(null) }
    var showShortcutsInfo by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Clean Developer Metrics
                DevMetricsCard(
                    totalTasks = totalCount,
                    completedTasks = completedCount,
                    totalSprintMinutes = totalSprintMinutes,
                    showShortcuts = showShortcutsInfo,
                    onToggleShortcuts = { showShortcutsInfo = !showShortcutsInfo },
                    onStartTour = { viewModel.startCoachMarksTour() }
                )
            }

            // Keyboard Shortcuts collapsible
            item {
                AnimatedVisibility(
                    visible = showShortcutsInfo,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    KeyboardShortcutsBanner()
                }
            }

            // Command Palette Bar
            item {
                QuickCommandPalette(
                    onExecuteCommand = { cmd -> viewModel.executeQuickCommand(cmd) }
                )
            }

            // Search Bar & Filter Controls
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Filter by title, repo, or branch...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "ALL" to "All Status",
                            TaskStatus.TODO.name to "Todo",
                            TaskStatus.IN_PROGRESS.name to "In Progress",
                            TaskStatus.IN_REVIEW.name to "Review",
                            TaskStatus.DONE.name to "Done"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = selectedStatus == key,
                                onClick = { viewModel.setStatusFilter(key) },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    // Category Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val allCategories = listOf(
                            "ALL" to "All Tags",
                            TaskCategory.FEATURE.name to "#feat",
                            TaskCategory.BUGFIX.name to "#bug",
                            TaskCategory.REFACTOR.name to "#refactor",
                            TaskCategory.DEVOPS.name to "#infra",
                            TaskCategory.CODE_REVIEW.name to "#review",
                            TaskCategory.DOCS.name to "#docs",
                            TaskCategory.TESTING.name to "#test"
                        ) + fetchedRepoCategories

                        allCategories.forEach { (key, label) ->
                            FilterChip(
                                selected = selectedCategory == key,
                                onClick = { viewModel.setCategoryFilter(key) },
                                label = { Text(label, fontFamily = FontFamily.Monospace, fontSize = 11.sp) }
                            )
                        }
                        
                        if (fetchedRepoCategories.isEmpty()) {
                            IconButton(onClick = { viewModel.fetchUserRepositories() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Fetch Repositories", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Task Items or Empty Workspace State
            if (tasks.isEmpty()) {
                item {
                    EmptyWorkspaceCard(
                        searchQuery = searchQuery,
                        onCreateTask = { showAddDialog = true }
                    )
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onStatusChange = { newStatus -> viewModel.updateTaskStatus(task, newStatus) },
                        onStartSprint = { onNavigateToSprint(task) },
                        onEdit = { taskToEdit = task },
                        onDelete = { viewModel.deleteTask(task) },
                        onShowCommitModal = { taskForCommitModal = task }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Task",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    // Add Task Dialog
    if (showAddDialog) {
        val defaultRepo = connectedRepos.firstOrNull()?.fullName ?: ""
        TaskDetailDialog(
            initialTask = null,
            defaultRepo = defaultRepo,
            fetchedRepoCategories = fetchedRepoCategories,
            onDismiss = { showAddDialog = false },
            onSaveTask = { title, desc, cat, pri, repo, branch, est, due, emoji ->
                viewModel.addNewTask(title, desc, cat, pri, repo, branch, est, due, emoji)
            }
        )
    }

    // Edit Task Dialog
    taskToEdit?.let { task ->
        TaskDetailDialog(
            initialTask = task,
            fetchedRepoCategories = fetchedRepoCategories,
            onDismiss = { taskToEdit = null },
            onSaveTask = { title, desc, cat, pri, repo, branch, est, due, emoji ->
                viewModel.updateTask(
                    task.copy(
                        title = title,
                        description = desc,
                        category = cat,
                        priority = pri,
                        repositoryName = repo,
                        branchName = branch,
                        estimatedMinutes = est,
                        dueDateMillis = due,
                        emoji = emoji
                    )
                )
            }
        )
    }

    // Git Commit Generator Dialog
    taskForCommitModal?.let { task ->
        val commitMsg = task.commitMessage ?: "feat(${task.category.lowercase()}): ${task.title.lowercase()}"
        val gitCmd = GitHubService.generateGitCommitCommand(commitMsg, task.branchName)

        AlertDialog(
            onDismissRequest = { taskForCommitModal = null },
            shape = RoundedCornerShape(12.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Commit, contentDescription = null, tint = DevCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Git Commit Command", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Conventional Commit Message:",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = commitMsg,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Terminal CLI Command:",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = gitCmd,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = DevCyan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(gitCmd))
                        viewModel.showToast("Copied command to clipboard!")
                        taskForCommitModal = null
                    }
                ) {
                    Text("Copy Command", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.logManualGitCommit(task, commitMsg, task.repositoryName, task.branchName)
                        taskForCommitModal = null
                    }
                ) {
                    Text("Log to Git History", color = DevCyan)
                }
            }
        )
    }
}

@Composable
private fun DevMetricsCard(
    totalTasks: Int,
    completedTasks: Int,
    totalSprintMinutes: Int,
    showShortcuts: Boolean,
    onToggleShortcuts: () -> Unit,
    onStartTour: () -> Unit = {}
) {
    val completionRate = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0
    val progressAnimated by animateFloatAsState(
        targetValue = if (totalTasks > 0) completedTasks.toFloat() / totalTasks.toFloat() else 0f,
        animationSpec = spring(),
        label = "progress"
    )

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "TELEMETRY",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SPRINT METRICS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = onStartTour)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Feature Tour",
                            tint = DevCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Tour",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = DevCyan
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = onToggleShortcuts)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Shortcuts",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = if (showShortcuts) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle shortcuts",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(label = "Total Tasks", value = "$totalTasks", modifier = Modifier.weight(1f))
                MetricColumn(label = "Completed", value = "$completedTasks", modifier = Modifier.weight(1f))
                MetricColumn(label = "Completion", value = "$completionRate%", modifier = Modifier.weight(1f))
                MetricColumn(label = "Focus Log", value = "${totalSprintMinutes}m", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progressAnimated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun KeyboardShortcutsBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "COMMAND PALETTE SYNTAX",
                color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text("• :add <Title> → Quick task creation", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
            Text("• #bug, #feat, #refactor, #infra, #test → Assign tags", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
            Text("• !p0, !p1, !p2, !p3 → Set priority level", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
            Text("• ^today, ^tomorrow, ^week → Target schedule", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
            Text("• @repo ~30m → Set repository & sprint duration", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun EmptyWorkspaceCard(
    searchQuery: String,
    onCreateTask: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TaskPeterThemedGlyph(
                symbol = if (searchQuery.isNotBlank()) "search" else "clean",
                size = 32.dp,
                animated = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (searchQuery.isNotBlank()) "No Matching Tasks Found" else "Clean Workspace",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (searchQuery.isNotBlank())
                    "No items match '$searchQuery'. Try adjusting your query or tag filter."
                else
                    "Your developer workspace is ready. Create a task or use the command palette below to start your sprint.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCreateTask,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create Task", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun TaskCard(
    task: TaskEntity,
    onStatusChange: (TaskStatus) -> Unit,
    onStartSprint: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShowCommitModal: () -> Unit
) {
    val isDone = task.status == TaskStatus.DONE.name

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (task.priority == "P0") MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Badges Flow Row with Custom Task Emoji & Repo Branch
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TaskEmojiBadge(emoji = task.emoji)
                PriorityBadge(priorityName = task.priority)
                CategoryBadge(categoryName = task.category)
                StatusBadge(statusName = task.status)
                RepoBranchBadge(repo = task.repositoryName, branch = task.branchName)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Task Title
            Text(
                text = task.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
            )

            // Description
            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            // Commit Hash if present
            if (!task.commitHash.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GitCommitBadge(commitHash = task.commitHash)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = task.commitMessage ?: "",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time & Actions Bottom Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Focus time spent/est
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Sprint time",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${task.spentMinutes}/${task.estimatedMinutes}m",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Interactive Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start Focus Sprint button
                    IconButton(
                        onClick = onStartSprint,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Sprint on Task",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Git Commit Generator
                    IconButton(
                        onClick = onShowCommitModal,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Commit,
                            contentDescription = "Git Commit",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Status Cycle Quick Action
                    IconButton(
                        onClick = {
                            val nextStatus = when (task.status) {
                                TaskStatus.TODO.name -> TaskStatus.IN_PROGRESS
                                TaskStatus.IN_PROGRESS.name -> TaskStatus.IN_REVIEW
                                TaskStatus.IN_REVIEW.name -> TaskStatus.DONE
                                else -> TaskStatus.TODO
                            }
                            onStatusChange(nextStatus)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Advance Status",
                            tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Edit
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
