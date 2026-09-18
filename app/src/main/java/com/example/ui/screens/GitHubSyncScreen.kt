package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Commit
import com.example.ui.components.TaskPeterGlyphPicker
import com.example.ui.components.TaskPeterThemedGlyph
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.auth.GitHubAuthState
import com.example.data.github.GitHubDeviceCodeResponse
import com.example.data.github.GitHubIssueItem
import com.example.data.github.GitHubLabelItem
import com.example.data.github.GitHubPullRequestItem
import com.example.data.github.GitHubRepo
import com.example.data.model.GitCommitEntity
import com.example.ui.MainViewModel
import com.example.ui.components.GitCommitBadge
import com.example.ui.theme.AppThemeStyle
import com.example.ui.theme.accentColor
import com.example.ui.theme.secondaryAccentColor
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevCyan
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevPurple

@Composable
fun GitHubSyncScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val clipboardManager = LocalClipboardManager.current

    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val githubUser by viewModel.githubUsername.collectAsStateWithLifecycle()
    val repos by viewModel.connectedRepos.collectAsStateWithLifecycle()
    val commits by viewModel.allCommits.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()
    val lastSync by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val devices by viewModel.syncDevices.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val currentThemeStyle by viewModel.appThemeStyle.collectAsStateWithLifecycle()
    val workspaceName by viewModel.workspaceName.collectAsStateWithLifecycle()
    val userStatusEmoji by viewModel.userStatusEmoji.collectAsStateWithLifecycle()
    val userStatusText by viewModel.userStatusText.collectAsStateWithLifecycle()
    val githubIssues by viewModel.githubIssues.collectAsStateWithLifecycle()
    val githubPullRequests by viewModel.githubPullRequests.collectAsStateWithLifecycle()
    val isLoadingIssues by viewModel.isLoadingIssues.collectAsStateWithLifecycle()
    val selectedIssueFilter by viewModel.selectedIssueFilter.collectAsStateWithLifecycle()
    val selectedRepoFilter by viewModel.selectedRepoFilter.collectAsStateWithLifecycle()
    val deviceCodeState by viewModel.deviceCodeState.collectAsStateWithLifecycle()
    val isDevicePolling by viewModel.isDeviceFlowPolling.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showAddRepoDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showSetupGuide by remember { mutableStateOf(false) }
    var showWorkspaceDialog by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Section Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = DevEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FIREBASE AUTH & GITHUB SYNC",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DevEmerald
                    )
                }

                // Dark mode toggle in header
                IconButton(onClick = { viewModel.toggleTheme() }) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Theme",
                        tint = if (isDark) DevAmber else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // --- IDE & AI Theme Selection Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DevPurple.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = DevPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "IDE & DEVELOPER THEMES",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = DevPurple
                            )
                        }

                        Text(
                            text = currentThemeStyle.displayName,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = DevCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppThemeStyle.entries.forEach { theme ->
                            val isSelected = currentThemeStyle == theme
                            val themeAccent = theme.accentColor
                            val borderColor = if (isSelected) themeAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            val bgColor = if (isSelected) themeAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setAppThemeStyle(theme) }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Theme preview swatches
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(end = 10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.accentColor)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.secondaryAccentColor)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = theme.displayName,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) themeAccent else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = theme.subtitle,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = themeAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Developer Workspace & Custom Status ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DevAmber.copy(alpha = 0.35f))
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
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                TaskPeterThemedGlyph(symbol = userStatusEmoji, size = 18.dp, animated = true)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = workspaceName,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Status: $userStatusText",
                                    fontSize = 11.sp,
                                    color = DevAmber
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { showWorkspaceDialog = true },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // --- Firebase Auth & GitHub Authentication Status ---
        item {
            when (val state = authState) {
                is GitHubAuthState.Authenticated -> {
                    AuthenticatedGitHubCard(
                        user = state,
                        isSyncing = isSyncing,
                        lastSync = lastSync,
                        onSync = { viewModel.syncWithCloud() },
                        onFetchRepos = { viewModel.fetchUserRepositories() },
                        onFetchIssues = { viewModel.refreshIssuesAndPrs() },
                        onSignOut = { showSignOutDialog = true }
                    )
                }
                is GitHubAuthState.Authenticating -> {
                    AuthenticatingCard(step = state.step)
                }
                is GitHubAuthState.AuthError -> {
                    AuthErrorCard(
                        error = state.error,
                        isConfig = state.isConfigurationError,
                        onRetry = {
                            activity?.let { viewModel.signInWithGitHub(it) }
                        },
                        onDismiss = { viewModel.dismissAuthError() },
                        onToggleGuide = { showSetupGuide = !showSetupGuide },
                        showGuide = showSetupGuide
                    )
                }
                is GitHubAuthState.Unauthenticated, is GitHubAuthState.Initializing -> {
                    UnauthenticatedGitHubCard(
                        onConnectUser = { username, token ->
                            viewModel.connectGitHubAccount(username, token)
                        },
                        onSignIn = {
                            activity?.let { viewModel.signInWithGitHub(it) }
                        },
                        onStartDeviceFlow = {
                            viewModel.startGitHubDeviceFlow()
                        },
                        onManualConfig = { showAccountDialog = true },
                        onToggleGuide = { showSetupGuide = !showSetupGuide },
                        showGuide = showSetupGuide
                    )
                }
            }
        }

        // --- Multi-Device Continuity & Cloud Sync State ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DevCyan.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Devices,
                                contentDescription = null,
                                tint = DevCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MULTI-DEVICE CONTINUITY",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = DevCyan
                            )
                        }

                        Button(
                            onClick = { viewModel.syncWithCloud() },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = DevCyan),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Syncing...", color = Color.Black, fontSize = 11.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync Now", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Last Synced: $lastSync",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    devices.forEach { device ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(DevEmerald)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = device,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // --- Connected Repositories Section ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = DevEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CONNECTED REPOSITORIES (${repos.size})",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = DevEmerald
                            )
                        }

                        OutlinedButton(
                            onClick = { showAddRepoDialog = true },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Link Repo", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (repos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "No project repositories linked yet. Tap 'Link Repo' above to connect your GitHub projects.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        repos.forEach { repo ->
                            RepoRow(repo = repo, onRemove = { viewModel.removeConnectedRepo(repo) })
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // --- GitHub Issues & Pull Requests Dashboard Sync ---
        item {
            GitHubIssuesAndPrsCard(
                issues = githubIssues,
                pullRequests = githubPullRequests,
                isLoading = isLoadingIssues,
                selectedFilter = selectedIssueFilter,
                onFilterChange = { viewModel.setIssueFilter(it) },
                selectedRepo = selectedRepoFilter,
                onRepoFilterChange = { viewModel.setRepoFilter(it) },
                connectedRepos = repos,
                onRefresh = { viewModel.refreshIssuesAndPrs() },
                onSyncAll = { viewModel.syncAllOpenIssuesAndPrs() },
                onSyncIssue = { issue, repo -> viewModel.syncIssueToDashboard(issue, repo) },
                onSyncPr = { pr, repo -> viewModel.syncPullRequestToDashboard(pr, repo) },
                isItemSynced = { number, repo ->
                    allTasks.any { it.githubIssueNumber == number && (repo.isEmpty() || it.repositoryName == repo) }
                }
            )
        }

        // --- Automated Git Commit Stream ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Commit,
                                contentDescription = null,
                                tint = DevEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AUTOMATED COMMIT LOGS",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = DevEmerald
                            )
                        }

                        Text(
                            text = "${commits.size} Commits",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (commits.isEmpty()) {
                        Text(
                            text = "No commits logged yet. Complete tasks or log manual commits to sync git history.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        commits.take(4).forEach { commit ->
                            CommitItemRow(
                                commit = commit,
                                onCopy = {
                                    val cmd = "git commit -m \"${commit.message}\""
                                    clipboardManager.setText(AnnotatedString(cmd))
                                    viewModel.showToast("Copied CLI commit command!")
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // --- Cloud Backups & Export Features ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = DevPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SECURE DATA BACKUPS & EXPORTS",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = DevPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Export Standup Markdown
                        OutlinedButton(
                            onClick = {
                                val md = viewModel.exportMarkdownReport()
                                clipboardManager.setText(AnnotatedString(md))
                                viewModel.showToast("Copied Markdown Standup to clipboard!")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export MD", fontSize = 11.sp)
                        }

                        // Export JSON Backup
                        OutlinedButton(
                            onClick = {
                                val json = viewModel.exportJsonBackup()
                                clipboardManager.setText(AnnotatedString(json))
                                viewModel.showToast("Copied JSON Backup payload to clipboard!")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export JSON", fontSize = 11.sp)
                        }

                        // Restore JSON Backup
                        OutlinedButton(
                            onClick = { showRestoreDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // --- Privacy & Zero Data Leakage Audit Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DevEmerald.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = DevEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PRIVACY & SECURITY AUDIT",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = DevEmerald
                            )
                        }

                        Button(
                            onClick = { viewModel.openPrivacyDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = DevEmerald),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Inspect Audit", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "100% On-Device Local Sandbox. Zero tracking, zero analytics SDKs, zero telemetry. Your code, task tickets, notes, and personal access tokens never leak or get shared with anyone.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // --- Developer Start Guide Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DevCyan.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = DevCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DEVELOPER START GUIDE",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = DevCyan
                            )
                        }

                        Button(
                            onClick = { viewModel.openUserGuide() },
                            colors = ButtonDefaults.buttonColors(containerColor = DevCyan),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Open Guide", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Master agile task parsing (#feat, #bug), deep work sprint timer with binaural audio synthesis, and automated GitHub repository category mapping.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // --- GitHub OAuth2 Device Authorization Flow Dialog ---
    val currentDeviceCode = deviceCodeState
    if (currentDeviceCode != null) {
        GitHubDeviceFlowDialog(
            response = currentDeviceCode,
            isPolling = isDevicePolling,
            onDismiss = { viewModel.cancelDeviceFlow() }
        )
    }

    // --- Sign Out Confirmation Dialog ---
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out of Firebase Auth?", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "You will be disconnected from Firebase Auth and live cloud synchronization. Your local tasks and sprint logs will remain safe on this device.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.signOutGitHub()
                        showSignOutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sign Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Add Connected Repo Dialog ---
    if (showAddRepoDialog) {
        var repoNameInput by remember { mutableStateOf("") }
        var repoLangInput by remember { mutableStateOf("Kotlin") }
        var repoDescInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddRepoDialog = false },
            title = { Text("Link Repository", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Add a project repository to synchronize branch tasks and automated commit logs:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Quick 1-tap repo suggestions
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            androidx.compose.material3.SuggestionChip(
                                onClick = {
                                    repoNameInput = "google-gemini/cookbook"
                                    repoLangInput = "Kotlin"
                                    repoDescInput = "Google Gemini AI Cookbook & Samples"
                                },
                                label = { Text("google-gemini/cookbook", fontSize = 10.sp, fontFamily = FontFamily.Monospace) }
                            )
                        }
                        item {
                            androidx.compose.material3.SuggestionChip(
                                onClick = {
                                    repoNameInput = "android/architecture-templates"
                                    repoLangInput = "Kotlin"
                                    repoDescInput = "Modern Android clean architecture blueprints"
                                },
                                label = { Text("android/architecture-templates", fontSize = 10.sp, fontFamily = FontFamily.Monospace) }
                            )
                        }
                        item {
                            androidx.compose.material3.SuggestionChip(
                                onClick = {
                                    repoNameInput = "square/retrofit"
                                    repoLangInput = "Kotlin"
                                    repoDescInput = "Type-safe HTTP client for Android and Java"
                                },
                                label = { Text("square/retrofit", fontSize = 10.sp, fontFamily = FontFamily.Monospace) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = repoNameInput,
                        onValueChange = { repoNameInput = it },
                        label = { Text("Repo Name or owner/repo") },
                        placeholder = { Text("org/awesome-backend") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = repoLangInput,
                        onValueChange = { repoLangInput = it },
                        label = { Text("Primary Language") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = repoDescInput,
                        onValueChange = { repoDescInput = it },
                        label = { Text("Description") },
                        placeholder = { Text("Microservice or client application") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (repoNameInput.isNotBlank()) {
                            viewModel.addConnectedRepo(repoNameInput, repoLangInput, repoDescInput)
                            showAddRepoDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevEmerald)
                ) {
                    Text("Link Repo", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRepoDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Developer Workspace & Status Dialog ---
    if (showWorkspaceDialog) {
        var tempWorkspace by remember { mutableStateOf(workspaceName) }
        var tempEmoji by remember { mutableStateOf(userStatusEmoji) }
        var tempStatus by remember { mutableStateOf(userStatusText) }

        AlertDialog(
            onDismissRequest = { showWorkspaceDialog = false },
            title = { Text("Workspace & Status", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Customize your developer profile and environment:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempWorkspace,
                        onValueChange = { tempWorkspace = it },
                        label = { Text("Workspace Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Status Glyph",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TaskPeterGlyphPicker(
                        selectedGlyphKey = tempEmoji,
                        onGlyphSelected = { tempEmoji = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempStatus,
                        onValueChange = { tempStatus = it },
                        label = { Text("Status Description") },
                        placeholder = { Text("Deep in sprint flow") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempWorkspace.isNotBlank()) viewModel.setWorkspaceName(tempWorkspace.trim())
                        viewModel.setUserStatus(tempEmoji, tempStatus.trim())
                        showWorkspaceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevEmerald)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWorkspaceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Configure GitHub Account Dialog ---
    if (showAccountDialog) {
        var usernameInput by remember { mutableStateOf(githubUser) }
        var tokenInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = { Text("Manual GitHub Config", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Configure a custom GitHub handle or Personal Access Token (PAT) for developer identity:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("GitHub Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("Personal Access Token (optional)") },
                        placeholder = { Text("ghp_...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.connectGitHubAccount(usernameInput, tokenInput)
                        showAccountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevEmerald)
                ) {
                    Text("Save & Connect", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Restore JSON Backup Dialog ---
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore From Cloud / JSON Backup", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Paste TaskPeter JSON backup data to import:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = { restoreJsonText = it },
                        placeholder = { Text("{\"app\": \"TaskPeter\", ...}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        maxLines = 8
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonText.isNotBlank()) {
                            viewModel.restoreBackup(restoreJsonText)
                            showRestoreDialog = false
                            restoreJsonText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevEmerald)
                ) {
                    Text("Restore Backup", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- Authenticated Developer Identity Card ---
@Composable
private fun AuthenticatedGitHubCard(
    user: GitHubAuthState.Authenticated,
    isSyncing: Boolean,
    lastSync: String,
    onSync: () -> Unit,
    onFetchRepos: () -> Unit,
    onFetchIssues: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DevEmerald.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with provider badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DevEmerald.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DevEmerald,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GITHUB OAUTH2 • ${user.authMethod.uppercase()}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = DevEmerald
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sign Out", fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Photo
                if (!user.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(user.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "GitHub Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.githubUsername.take(1).uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.displayName ?: user.githubUsername,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DevEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "PRO",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = DevEmerald
                            )
                        }
                    }
                    Text(
                        text = "github.com/@${user.githubUsername}",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DevCyan
                    )
                    user.email?.let { email ->
                        Text(
                            text = email,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Optional Bio Row
            if (!user.bio.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = user.bio,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Developer Metric Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(vertical = 6.dp, horizontal = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = DevCyan, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${user.publicReposCount} Repos",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(vertical = 6.dp, horizontal = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = DevEmerald, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${user.followersCount} Devs",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DevEmerald.copy(alpha = 0.12f))
                        .padding(vertical = 6.dp, horizontal = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = DevEmerald, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Zero Leakage",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = DevEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions: Sync Repositories & Fetch Issues
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onFetchRepos,
                    colors = ButtonDefaults.buttonColors(containerColor = DevCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sync Repos",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = onFetchIssues,
                    colors = ButtonDefaults.buttonColors(containerColor = DevEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.BugReport, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Fetch Issues/PRs",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Firebase UID & Sync Status Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Active Local Session",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "UID: ${user.uid.take(12)}...",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// --- Authenticating Spinner Card ---
@Composable
private fun AuthenticatingCard(step: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DevEmerald.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = DevEmerald,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "FIREBASE GITHUB AUTHENTICATION",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DevEmerald
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = step,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --- Auth Error Card with Troubleshooting & Firebase Guide ---
@Composable
private fun AuthErrorCard(
    error: String,
    isConfig: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onToggleGuide: () -> Unit,
    showGuide: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DevAmber)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = DevAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isConfig) "FIREBASE GITHUB CONFIGURATION REQUIRED" else "AUTHENTICATION ERROR",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DevAmber
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = error,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = DevEmerald),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry Sign-In", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onToggleGuide,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showGuide) "Hide Guide" else "Setup Guide", fontSize = 11.sp)
                }
            }

            AnimatedVisibility(visible = showGuide) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    FirebaseSetupGuideBlock()
                }
            }
        }
    }
}

// --- Unauthenticated Sign-In Card ---
@Composable
private fun UnauthenticatedGitHubCard(
    onConnectUser: (username: String, token: String) -> Unit,
    onSignIn: () -> Unit,
    onStartDeviceFlow: () -> Unit,
    onManualConfig: () -> Unit,
    onToggleGuide: () -> Unit,
    showGuide: Boolean
) {
    var quickUsername by remember { mutableStateOf("") }
    var quickToken by remember { mutableStateOf("") }
    var showTokenField by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DevEmerald.copy(alpha = 0.4f))
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF21262D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = DevEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "GITHUB DEVELOPER IDENTITY",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = DevEmerald
                        )
                        Text(
                            text = "Connect & Sync Repositories",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                OutlinedButton(
                    onClick = onManualConfig,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Config", fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Connect your GitHub developer handle to turn your public/private repositories into task categories and sync your developer identity.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Connect Input Field
            OutlinedTextField(
                value = quickUsername,
                onValueChange = { quickUsername = it },
                label = { Text("GitHub Handle (@username)") },
                placeholder = { Text("e.g. your-github-username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            if (showTokenField) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quickToken,
                    onValueChange = { quickToken = it },
                    label = { Text("Personal Access Token (PAT) - Optional") },
                    placeholder = { Text("ghp_...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Direct Connect Button
            Button(
                onClick = {
                    if (quickUsername.isNotBlank()) {
                        onConnectUser(quickUsername.trim(), quickToken.trim())
                    }
                },
                enabled = quickUsername.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DevEmerald),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Connect GitHub & Pull Repos", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // GitHub OAuth2 Device Authorization Flow (RFC 8628)
            Button(
                onClick = onStartDeviceFlow,
                colors = ButtonDefaults.buttonColors(containerColor = DevPurple),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(40.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("GitHub OAuth2 Device Login (RFC 8628)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Firebase OAuth Button
            OutlinedButton(
                onClick = onSignIn,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(38.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Login, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sign In with Firebase OAuth", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Privacy Guarantee Notice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Zero Data Leakage: Tokens & repos stay 100% on-device.",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showTokenField = !showTokenField }) {
                    Text(
                        text = if (showTokenField) "Hide Token Field" else "+ Add Private Repo Token",
                        fontSize = 10.sp,
                        color = DevCyan
                    )
                }

                TextButton(onClick = onToggleGuide) {
                    Text(
                        text = if (showGuide) "Hide OAuth Guide" else "OAuth Guide",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = showGuide) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    FirebaseSetupGuideBlock()
                }
            }
        }
    }
}

// --- Firebase Console Setup Step-by-Step Guide ---
@Composable
private fun FirebaseSetupGuideBlock() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = "FIREBASE GITHUB PROVIDER SETUP GUIDE",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DevCyan
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "1. Firebase Console: Go to Authentication → Sign-in method → Add GitHub.",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "2. Copy the Firebase Authorization callback URL.",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "3. GitHub: Settings → Developer Settings → OAuth Apps → New OAuth App. Paste the callback URL.",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "4. Copy GitHub Client ID and Client Secret into Firebase Console.",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "5. Download google-services.json to /app module.",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RepoRow(repo: GitHubRepo, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = repo.fullName,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = DevCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DevEmerald.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = repo.language ?: "Unknown",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = DevEmerald
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = repo.description ?: "",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove Repo",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CommitItemRow(commit: GitCommitEntity, onCopy: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GitCommitBadge(commitHash = commit.commitHash)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${commit.repo}:${commit.branch}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = commit.message,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(onClick = onCopy) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy CLI",
                tint = DevCyan,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
