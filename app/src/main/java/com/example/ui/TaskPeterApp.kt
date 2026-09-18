package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.platform.testTag
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.PrivacySecurityDialog
import com.example.ui.components.UserGuideDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.FocusSprintScreen
import com.example.ui.screens.GitHubSyncScreen
import com.example.ui.screens.PeterAIChatScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.AppThemeStyle
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.accentColor

enum class AppTab(val title: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    TASKS("Tasks", Icons.Filled.Checklist, Icons.Outlined.Checklist),
    FOCUS("Focus", Icons.Filled.Timer, Icons.Outlined.Timer),
    CALENDAR("Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    ANALYTICS("Analytics", Icons.Filled.AutoGraph, Icons.Outlined.AutoGraph),
    COPILOT("Peter AI", Icons.Filled.Psychology, Icons.Outlined.Psychology),
    SYNC("Sync", Icons.Filled.CloudSync, Icons.Outlined.CloudSync)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskPeterApp(viewModel: MainViewModel) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val currentThemeStyle by viewModel.appThemeStyle.collectAsStateWithLifecycle()
    var showThemeMenu by remember { mutableStateOf(false) }
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val showPrivacyDialog by viewModel.showPrivacyDialog.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val allSprints by viewModel.allSprints.collectAsStateWithLifecycle()
    val allCommits by viewModel.allCommits.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showMoreMenu by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
            viewModel.clearToast()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TASKPETER",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "DEV SUITE",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    },
                    actions = {
                        // Compact Modern Theme Switcher Menu
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, currentThemeStyle.accentColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .clickable { showThemeMenu = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(currentThemeStyle.accentColor)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = currentThemeStyle.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                            DropdownMenu(
                                expanded = showThemeMenu,
                                onDismissRequest = { showThemeMenu = false }
                            ) {
                                AppThemeStyle.entries.forEach { style ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(style.accentColor)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = style.displayName,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (style == currentThemeStyle) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (style == currentThemeStyle) style.accentColor else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = style.subtitle,
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            viewModel.setAppThemeStyle(style)
                                            showThemeMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Direct Peter AI Copilot Access
                        IconButton(
                            onClick = { selectedTabIndex = AppTab.COPILOT.ordinal },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("top_bar_peter_ai_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Peter AI Copilot",
                                tint = if (selectedTabIndex == AppTab.COPILOT.ordinal) DevEmerald else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // More Options (Quick Toggle, Developer Guide & Privacy)
                        Box {
                            IconButton(
                                onClick = { showMoreMenu = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = { Text(if (isDark) "Switch to Light Mode" else "Switch to Dark Mode", fontSize = 13.sp) },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.toggleTheme()
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.HelpOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = { Text("Developer Guide", fontSize = 13.sp) },
                                    onClick = {
                                        showMoreMenu = false
                                        showGuideDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = DevEmerald,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    text = { Text("Privacy & Security Audit", fontSize = 13.sp) },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.openPrivacyDialog()
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        modifier = Modifier.border(
                            width = 0.8.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                    ) {
                        AppTab.entries.forEachIndexed { index, tab ->
                            val isSelected = selectedTabIndex == index
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedTabIndex = index },
                                alwaysShowLabel = false,
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            if (isWideScreen) {
                // Desktop / Tablet layout with NavigationRail
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        AppTab.entries.forEachIndexed { index, tab ->
                            val isSelected = selectedTabIndex == index
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { selectedTabIndex = index },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = { Text(tab.title, fontSize = 11.sp) },
                                colors = NavigationRailItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        TabContent(
                            selectedIndex = selectedTabIndex,
                            viewModel = viewModel,
                            onNavigateToSprint = { task ->
                                viewModel.setSprintTask(task)
                                selectedTabIndex = AppTab.FOCUS.ordinal
                            }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    TabContent(
                        selectedIndex = selectedTabIndex,
                        viewModel = viewModel,
                        onNavigateToSprint = { task ->
                            viewModel.setSprintTask(task)
                            selectedTabIndex = AppTab.FOCUS.ordinal
                        }
                    )
                }
            }
        }

        // On-demand Developer Start Guide Dialog
        if (showGuideDialog) {
            UserGuideDialog(
                onDismiss = { showGuideDialog = false },
                onComplete = { showGuideDialog = false },
                onStartCoachTour = { showGuideDialog = false }
            )
        }

        // 100% Privacy & Zero-Leakage Security Audit Dialog
        if (showPrivacyDialog) {
            PrivacySecurityDialog(
                taskCount = allTasks.size,
                sprintCount = allSprints.size,
                commitCount = allCommits.size,
                onDismiss = { viewModel.closePrivacyDialog() }
            )
        }
    }
}

@Composable
private fun TabContent(
    selectedIndex: Int,
    viewModel: MainViewModel,
    onNavigateToSprint: (com.example.data.model.TaskEntity?) -> Unit
) {
    AnimatedContent(
        targetState = selectedIndex,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInHorizontally(
                    initialOffsetX = { it / 6 },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy)
                ) + fadeIn(animationSpec = tween(220)))
                    .togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { -it / 6 },
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy)
                        ) + fadeOut(animationSpec = tween(180))
                    )
            } else {
                (slideInHorizontally(
                    initialOffsetX = { -it / 6 },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy)
                ) + fadeIn(animationSpec = tween(220)))
                    .togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { it / 6 },
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy)
                        ) + fadeOut(animationSpec = tween(180))
                    )
            }
        },
        label = "tab_animated_content"
    ) { tabIndex ->
        when (tabIndex) {
            AppTab.TASKS.ordinal -> TasksScreen(
                viewModel = viewModel,
                onNavigateToSprint = onNavigateToSprint
            )
            AppTab.FOCUS.ordinal -> FocusSprintScreen(viewModel = viewModel)
            AppTab.CALENDAR.ordinal -> CalendarScreen(viewModel = viewModel)
            AppTab.ANALYTICS.ordinal -> AnalyticsScreen(viewModel = viewModel)
            AppTab.COPILOT.ordinal -> PeterAIChatScreen(viewModel = viewModel)
            AppTab.SYNC.ordinal -> GitHubSyncScreen(viewModel = viewModel)
        }
    }
}
