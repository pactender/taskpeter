package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.github.GitHubDeviceCodeResponse
import com.example.data.github.GitHubIssueItem
import com.example.data.github.GitHubLabelItem
import com.example.data.github.GitHubPullRequestItem
import com.example.data.github.GitHubRepo
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevCyan
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevOrange
import com.example.ui.theme.DevPurple

val GitHubIssueItem.author: String get() = user?.login ?: "contributor"
val GitHubIssueItem.authorAvatarUrl: String? get() = user?.avatarUrl
val GitHubIssueItem.commentsCount: Int get() = comments
val GitHubIssueItem.repository: String get() = repositoryUrl?.substringAfterLast("repos/") ?: htmlUrl.substringAfter("github.com/").substringBefore("/issues")

val GitHubPullRequestItem.author: String get() = user?.login ?: "contributor"
val GitHubPullRequestItem.authorAvatarUrl: String? get() = user?.avatarUrl
val GitHubPullRequestItem.branch: String get() = head?.ref ?: "feature-branch"
val GitHubPullRequestItem.baseBranch: String get() = base?.ref ?: "main"
val GitHubPullRequestItem.repository: String get() = htmlUrl.substringAfter("github.com/").substringBefore("/pull")

/**
 * Main Card for GitHub Issues & Pull Requests synchronization.
 */
@Composable
fun GitHubIssuesAndPrsCard(
    issues: List<GitHubIssueItem>,
    pullRequests: List<GitHubPullRequestItem>,
    isLoading: Boolean,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    selectedRepo: String?,
    onRepoFilterChange: (String?) -> Unit,
    connectedRepos: List<GitHubRepo>,
    onRefresh: () -> Unit,
    onSyncAll: () -> Unit,
    onSyncIssue: (GitHubIssueItem, String) -> Unit,
    onSyncPr: (GitHubPullRequestItem, String) -> Unit,
    isItemSynced: (Int, String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val totalCount = issues.size + pullRequests.size

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DevCyan.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = DevCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GITHUB ISSUES & PULL REQUESTS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = DevCyan
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = DevCyan,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Issues",
                                tint = DevCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = onSyncAll,
                        colors = ButtonDefaults.buttonColors(containerColor = DevEmerald),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(30.dp),
                        enabled = totalCount > 0 && !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sync All ($totalCount)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "OAuth2 synchronized tickets from your connected repositories. One click transforms any open issue or pull request into a TaskPeter dashboard backlog item.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips (All, Issues, PRs)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "all",
                    onClick = { onFilterChange("all") },
                    label = { Text("All ($totalCount)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DevCyan.copy(alpha = 0.2f),
                        selectedLabelColor = DevCyan
                    )
                )

                FilterChip(
                    selected = selectedFilter == "issues",
                    onClick = { onFilterChange("issues") },
                    label = { Text("Issues (${issues.size})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DevEmerald.copy(alpha = 0.2f),
                        selectedLabelColor = DevEmerald
                    )
                )

                FilterChip(
                    selected = selectedFilter == "prs",
                    onClick = { onFilterChange("prs") },
                    label = { Text("PRs (${pullRequests.size})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DevPurple.copy(alpha = 0.2f),
                        selectedLabelColor = DevPurple
                    )
                )
            }

            // Repository Filter Row (if repositories exist)
            if (connectedRepos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedRepo == null,
                            onClick = { onRepoFilterChange(null) },
                            label = { Text("All Repos", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                    items(connectedRepos) { repo ->
                        FilterChip(
                            selected = selectedRepo == repo.name,
                            onClick = { onRepoFilterChange(if (selectedRepo == repo.name) null else repo.name) },
                            label = { Text(repo.name, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content List
            if (isLoading && totalCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = DevCyan, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Fetching GitHub Issues & Pull Requests...",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (totalCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No open GitHub issues or pull requests found.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onRefresh,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fetch / Reload", fontSize = 11.sp)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Render Issues
                    if (selectedFilter != "prs") {
                        val filteredIssues = if (selectedRepo != null) {
                            issues.filter { it.repository.contains(selectedRepo, ignoreCase = true) }
                        } else issues

                        filteredIssues.forEach { issue ->
                            val isSynced = isItemSynced(issue.number, issue.repository)
                            IssueCardItem(
                                issue = issue,
                                isSynced = isSynced,
                                onSync = { onSyncIssue(issue, issue.repository) }
                            )
                        }
                    }

                    // Render Pull Requests
                    if (selectedFilter != "issues") {
                        val filteredPrs = if (selectedRepo != null) {
                            pullRequests.filter { it.repository.contains(selectedRepo, ignoreCase = true) }
                        } else pullRequests

                        filteredPrs.forEach { pr ->
                            val isSynced = isItemSynced(pr.number, pr.repository)
                            PullRequestCardItem(
                                pr = pr,
                                isSynced = isSynced,
                                onSync = { onSyncPr(pr, pr.repository) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Issue Card with State badge, labels, author, and 1-tap Dashboard Sync.
 */
@Composable
fun IssueCardItem(
    issue: GitHubIssueItem,
    isSynced: Boolean,
    onSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .border(
                width = 1.dp,
                color = if (isSynced) DevEmerald.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            // Header: Type + Number + Repo + State Badge
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
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = DevEmerald,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "#${issue.number}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = DevEmerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = issue.repository,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DevCyan
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // State Pill
                    StateBadge(state = issue.state)

                    Spacer(modifier = Modifier.width(4.dp))

                    // Open in GitHub Link
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(issue.htmlUrl)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open in GitHub",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Issue Title
            Text(
                text = issue.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Labels Row
            if (issue.labels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(issue.labels) { label ->
                        GitHubLabelBadge(label = label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Author & Sync Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!issue.authorAvatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(issue.authorAvatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = "@${issue.author}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (issue.commentsCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${issue.commentsCount} comments",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Sync to Dashboard Action
                if (isSynced) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DevEmerald.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = DevEmerald,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "In Backlog",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DevEmerald
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onSync,
                        colors = ButtonDefaults.buttonColors(containerColor = DevEmerald),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "Sync to Tasks",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Pull Request Card with branch info, review status, and 1-tap Dashboard Sync.
 */
@Composable
fun PullRequestCardItem(
    pr: GitHubPullRequestItem,
    isSynced: Boolean,
    onSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .border(
                width = 1.dp,
                color = if (isSynced) DevPurple.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            // Header: PR # + Repo + State Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DevPurple.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Commit,
                                contentDescription = null,
                                tint = DevPurple,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "PR #${pr.number}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = DevPurple
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = pr.repository,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DevCyan
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StateBadge(state = pr.state)

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pr.htmlUrl)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open in GitHub",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // PR Title
            Text(
                text = pr.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Branch row: head -> base
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(DevCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = pr.branch,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DevCyan
                    )
                }

                Text(" → ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = pr.baseBranch,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (pr.draft) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DevAmber.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "DRAFT",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = DevAmber
                        )
                    }
                }
            }

            // Labels Row
            if (pr.labels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(pr.labels) { label ->
                        GitHubLabelBadge(label = label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Author & Sync Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!pr.authorAvatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(pr.authorAvatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = "@${pr.author}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sync to Dashboard Action
                if (isSynced) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DevPurple.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = DevPurple,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "In Backlog",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DevPurple
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onSync,
                        colors = ButtonDefaults.buttonColors(containerColor = DevPurple),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "Sync to Tasks",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * GitHub Label Badge with hex color support.
 */
@Composable
fun GitHubLabelBadge(label: GitHubLabelItem) {
    val parsedColor = try {
        val hex = if (label.color.startsWith("#")) label.color else "#${label.color}"
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        DevCyan
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(parsedColor.copy(alpha = 0.15f))
            .border(1.dp, parsedColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = label.name,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = parsedColor
        )
    }
}

/**
 * State badge (open/closed/merged)
 */
@Composable
fun StateBadge(state: String) {
    val (color, text) = when (state.lowercase()) {
        "open" -> DevEmerald to "OPEN"
        "closed" -> Color(0xFFDA3633) to "CLOSED"
        "merged" -> DevPurple to "MERGED"
        else -> DevCyan to state.uppercase()
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * GitHub OAuth2 Device Authorization Flow Dialog (RFC 8628).
 */
@Composable
fun GitHubDeviceFlowDialog(
    response: GitHubDeviceCodeResponse,
    isPolling: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_device")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = DevPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GitHub OAuth2 Device Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "RFC 8628 OAuth2 Grant. Authorize TaskPeter to sync your repositories, issues, and pull requests without manual API keys:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Step 1: User Code Box
                Text(
                    text = "STEP 1: YOUR ONE-TIME DEVICE CODE",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = DevCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.5.dp, DevCyan.copy(alpha = pulseAlpha), RoundedCornerShape(10.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = response.userCode,
                            fontSize = 26.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            color = DevAmber,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(response.userCode))
                            },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Code", fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Step 2: Verification URL Button
                Text(
                    text = "STEP 2: AUTHORIZE ON GITHUB",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = DevEmerald
                )
                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(response.userCode))
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(response.verificationUri)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            // Browser not available or activity resolution failed; code is already in clipboard
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevPurple),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Code & Open GitHub", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Polling Status Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isPolling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = DevEmerald,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Awaiting GitHub OAuth2 confirmation...",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = DevEmerald
                            )
                        } else {
                            Text(
                                text = "Code valid for ${response.expiresIn / 60} minutes",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel Flow")
            }
        }
    )
}
