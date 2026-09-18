package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.auth.GitHubAuthState
import com.example.data.gemini.ChatMessage
import com.example.data.gemini.GeminiModelOption
import com.example.data.model.SubscriptionTier
import com.example.data.model.UserQuotaState
import com.example.ui.MainViewModel
import com.example.ui.components.ProtocolGoogleSignInGate
import com.example.ui.components.ProtocolSubscriptionDialog
import com.example.ui.components.ProtocolUserQuotaHUD
import com.example.ui.components.bounceClick
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevCyan
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevPurple
import com.example.ui.theme.ProtocolCobaltBlue
import com.example.ui.theme.ProtocolElevated
import com.example.ui.theme.ProtocolNeonCyan
import com.example.ui.theme.ProtocolNeonOrange

@Composable
fun PeterAIChatScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val userQuotaState by viewModel.userQuotaState.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedGeminiModel.collectAsStateWithLifecycle()

    var inputPrompt by remember { mutableStateOf("") }
    var showModelMenu by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Automatically scroll down when new messages arrive
    LaunchedEffect(chatMessages.size, isChatLoading) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size)
        }
    }

    // 1. Gated Access: Require Google Sign-In with Firebase Auth
    if (authState !is GitHubAuthState.Authenticated) {
        ProtocolGoogleSignInGate(
            onSignIn = { email, name, geminiKey ->
                viewModel.signInWithGoogle(email = email, displayName = name)
                viewModel.setGeminiApiKey(geminiKey)
            },
            modifier = modifier
        )
        return
    }

    val authenticatedUser = authState as GitHubAuthState.Authenticated

    val quickPrompts = listOf(
        "Break down this feature into sprint subtasks",
        "Review PR code checklist & security flaws",
        "Explain Git rebase vs merge with example commands",
        "Prioritize my P0/P1 developer backlog today",
        "Generate automated tests for Room Dao"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // --- 1. Top Protocol HUD: Google Profile, Quota Bar & Tier Badge ---
        ProtocolUserQuotaHUD(
            auth = authenticatedUser,
            quotaState = userQuotaState,
            onOpenUpgradeDialog = { showSubscriptionDialog = true },
            onResetQuota = { viewModel.resetDailyQuota() },
            onSignOut = { viewModel.signOutAuth() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- 2. Model Selector Bar ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = ProtocolElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, ProtocolCobaltBlue.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Branding & Model Trigger
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ProtocolNeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Peter AI",
                            tint = ProtocolNeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Peter AI",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DevPurple.copy(alpha = 0.25f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "GEMINI",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC084FC)
                                )
                            }
                        }

                        // Model Selector Dropdown Trigger
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF070B14))
                                    .bounceClick { showModelMenu = true }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = selectedModel.displayName,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ProtocolNeonCyan
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = "Select Gemini Model",
                                    tint = ProtocolNeonCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showModelMenu,
                                onDismissRequest = { showModelMenu = false }
                            ) {
                                GeminiModelOption.entries.forEach { modelOption ->
                                    val isAllowed = userQuotaState.tier.isModelAllowed(modelOption.modelId)
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = modelOption.displayName,
                                                        fontWeight = if (modelOption == selectedModel) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 13.sp
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    if (!isAllowed) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(3.dp))
                                                                .background(ProtocolNeonOrange.copy(alpha = 0.2f))
                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = "PRO",
                                                                fontSize = 9.sp,
                                                                fontFamily = FontFamily.Monospace,
                                                                fontWeight = FontWeight.Bold,
                                                                color = ProtocolNeonOrange
                                                            )
                                                        }
                                                    } else {
                                                        Text(
                                                            text = "(${modelOption.tag})",
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = modelOption.description,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            showModelMenu = false
                                            if (isAllowed) {
                                                viewModel.setGeminiModel(modelOption)
                                            } else {
                                                showSubscriptionDialog = true
                                            }
                                        },
                                        leadingIcon = {
                                            if (!isAllowed) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Requires Pro",
                                                    tint = ProtocolNeonOrange,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else if (modelOption == selectedModel) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = ProtocolNeonCyan,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Actions: Clear History
                if (chatMessages.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearChatHistory() },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Chat History",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- 3. Chat Messages History ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Welcome card if conversation is empty
            if (chatMessages.isEmpty()) {
                item {
                    ProtocolWelcomeCard(
                        selectedModel = selectedModel,
                        quotaState = userQuotaState,
                        onUpgradeClick = { showSubscriptionDialog = true }
                    )
                }
            }

            items(chatMessages, key = { it.id }) { message ->
                ChatMessageItem(
                    message = message,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(message.text))
                    },
                    onAddTask = { title, cat, priority ->
                        viewModel.addSuggestedTaskToBacklog(title, cat, priority)
                    }
                )
            }

            // Loading Indicator bubble
            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                                .background(ProtocolElevated)
                                .border(1.dp, ProtocolNeonCyan.copy(alpha = 0.4f), RoundedCornerShape(topStart = 4.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = ProtocolNeonCyan
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Peter AI is reasoning...",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ProtocolNeonCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- 4. Quick Prompts Horizontal Bar ---
        if (chatMessages.isEmpty() || chatMessages.size < 4) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(quickPrompts) { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF070B14))
                            .border(1.dp, ProtocolCobaltBlue.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .bounceClick {
                                if (userQuotaState.hasQuota) {
                                    viewModel.sendChatMessage(prompt)
                                } else {
                                    showSubscriptionDialog = true
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // --- 5. Polished Chat Bar & Send Button (Never Blank, Fully Animated) ---
        val canSend = inputPrompt.isNotBlank() && !isChatLoading && userQuotaState.hasQuota

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = ProtocolElevated),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (canSend) ProtocolNeonOrange else ProtocolCobaltBlue.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Quota Warning if exhausted
                if (!userQuotaState.hasQuota) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(ProtocolNeonOrange.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily quota reached (${userQuotaState.dailyLimit}/${userQuotaState.dailyLimit}). Upgrade plan for more.",
                            fontSize = 10.sp,
                            color = ProtocolNeonOrange,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Upgrade ➔",
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showSubscriptionDialog = true }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Input TextField
                    OutlinedTextField(
                        value = inputPrompt,
                        onValueChange = { inputPrompt = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("peter_ai_prompt_input"),
                        placeholder = {
                            Text(
                                text = if (userQuotaState.hasQuota) "Ask Peter AI to review code, break down sprint tasks..." else "Daily quota exhausted...",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        },
                        singleLine = false,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (canSend) {
                                val promptToSend = inputPrompt
                                inputPrompt = ""
                                keyboardController?.hide()
                                viewModel.sendChatMessage(promptToSend)
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Polished Send Button Column with Tactile Arrow
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (canSend) Brush.horizontalGradient(
                                        listOf(ProtocolNeonOrange, Color(0xFFFF6D00))
                                    ) else Brush.horizontalGradient(
                                        listOf(Color(0xFF1F293D), Color(0xFF161E30))
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (canSend) ProtocolNeonOrange else Color(0xFF334155),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .bounceClick(enabled = canSend) {
                                    if (canSend) {
                                        val promptToSend = inputPrompt
                                        inputPrompt = ""
                                        keyboardController?.hide()
                                        viewModel.sendChatMessage(promptToSend)
                                    }
                                }
                                .testTag("peter_ai_send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isChatLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                // Guaranteed High-Contrast Arrow Icon: Never Blank!
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Send Prompt",
                                    tint = if (canSend) Color.White else Color(0xFF94A3B8),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // Label underneath arrow to ensure absolute clarity
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "SEND ➔",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (canSend) ProtocolNeonOrange else Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }

    // Subscription & Upgrade Dialog
    if (showSubscriptionDialog) {
        ProtocolSubscriptionDialog(
            currentTier = userQuotaState.tier,
            onSelectTier = { tier ->
                viewModel.setSubscriptionTier(tier)
            },
            onDismiss = { showSubscriptionDialog = false }
        )
    }
}

@Composable
private fun ProtocolWelcomeCard(
    selectedModel: GeminiModelOption,
    quotaState: UserQuotaState,
    onUpgradeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ProtocolElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, ProtocolNeonCyan.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = ProtocolNeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Peter AI Developer Copilot",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(quotaState.tier.badgeColor.copy(alpha = 0.2f))
                        .clickable { onUpgradeClick() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = quotaState.tier.tag,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = quotaState.tier.badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Welcome to your intelligent developer sprint copilot powered by ${selectedModel.displayName}. Ask Peter AI to decompose user stories into sprint tasks, review PR security checklists, explain complex Git commands, or generate clean Kotlin code.",
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Quota: ${quotaState.remainingQueries}/${quotaState.dailyLimit} queries remaining",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ProtocolNeonOrange
                )

                Text(
                    text = "Upgrade Plan ➔",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProtocolNeonCyan,
                    modifier = Modifier.clickable { onUpgradeClick() }
                )
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    onCopy: () -> Unit,
    onAddTask: (String, String, String) -> Unit
) {
    if (message.isUser) {
        // User Message (Right-aligned)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomStart = 14.dp, bottomEnd = 14.dp))
                    .background(Color(0xFF0F1A30))
                    .border(1.dp, ProtocolNeonCyan.copy(alpha = 0.5f), RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomStart = 14.dp, bottomEnd = 14.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "You",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProtocolNeonCyan
                        )
                        Text(
                            text = com.example.util.DateUtils.formatTime(message.timestamp),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = message.text,
                        fontSize = 13.sp,
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    } else {
        // Peter AI Message (Left-aligned)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp))
                    .background(if (message.isError) Color(0xFF280B10) else ProtocolElevated)
                    .border(
                        1.dp,
                        if (message.isError) Color(0xFFEF4444) else ProtocolCobaltBlue.copy(alpha = 0.5f),
                        RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
                    )
                    .padding(12.dp)
            ) {
                Column {
                    // Header: Peter AI + Model Tag + Timestamp + Copy
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Peter AI",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (message.isError) Color(0xFFF87171) else ProtocolNeonCyan
                            )

                            if (message.modelUsed != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(DevPurple.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = message.modelUsed,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFFC084FC)
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = com.example.util.DateUtils.formatTime(message.timestamp),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = onCopy,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Message",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Message Body
                    FormattedMessageText(text = message.text)

                    // Suggested Task 1-Tap Action
                    if (!message.suggestedTaskTitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = ProtocolNeonOrange.copy(alpha = 0.12f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ProtocolNeonOrange.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "SUGGESTED TASK",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = ProtocolNeonOrange
                                    )
                                    Text(
                                        text = message.suggestedTaskTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Priority: ${message.suggestedTaskPriority ?: "P1_HIGH"} • Category: ${message.suggestedTaskCategory ?: "FEATURE"}",
                                        fontSize = 10.sp,
                                        color = Color(0xFFCBD5E1)
                                    )
                                }

                                Button(
                                    onClick = {
                                        onAddTask(
                                            message.suggestedTaskTitle,
                                            message.suggestedTaskCategory ?: "FEATURE",
                                            message.suggestedTaskPriority ?: "P1_HIGH"
                                        )
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ProtocolNeonOrange),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Add to Tasks",
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
        }
    }
}

@Composable
private fun FormattedMessageText(text: String) {
    val lines = text.split("\n")
    Column {
        lines.forEach { line ->
            when {
                line.startsWith("```") -> {
                    // Code fence indicator
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    Row(modifier = Modifier.padding(vertical = 1.dp)) {
                        Text(
                            text = "•",
                            color = ProtocolNeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = line.removePrefix("- ").removePrefix("* "),
                            fontSize = 13.sp,
                            color = Color(0xFFF1F5F9),
                            lineHeight = 17.sp
                        )
                    }
                }
                line.startsWith("git ") || line.startsWith("cd ") || line.startsWith("npm ") || line.startsWith("./gradlew") || line.startsWith("adb ") -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF070B14))
                            .border(1.dp, ProtocolCobaltBlue.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = line,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = ProtocolNeonCyan
                        )
                    }
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                else -> {
                    Text(
                        text = line,
                        fontSize = 13.sp,
                        color = Color(0xFFF1F5F9),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
