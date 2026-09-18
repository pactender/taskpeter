package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.GitHubAuthState
import com.example.data.model.SubscriptionTier
import com.example.data.model.UserQuotaState
import com.example.ui.theme.DevAmber
import com.example.ui.theme.DevCyan
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.DevPurple
import com.example.ui.theme.ProtocolBg
import com.example.ui.theme.ProtocolCobaltBlue
import com.example.ui.theme.ProtocolElevated
import com.example.ui.theme.ProtocolNeonCyan
import com.example.ui.theme.ProtocolNeonOrange

/**
 * Protocol Cyber-Noir Google Sign-In Gate.
 * Shown when user is not signed in, gating AI Chatbot behind authentic Google Identity.
 */
@Composable
fun ProtocolGoogleSignInGate(
    onSignIn: (email: String, name: String, geminiApiKey: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAccountDialog by remember { mutableStateOf(false) }
    var selectedEmail by remember { mutableStateOf("") }
    var selectedName by remember { mutableStateOf("") }
    var geminiApiKey by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Card inspired by the Protocol poster
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ProtocolElevated),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(listOf(ProtocolNeonCyan, ProtocolNeonOrange))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Protocol Cyber HUD Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF070B14))
                        .border(1.dp, ProtocolNeonCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ProtocolNeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PROTOCOL // SECURE NEURAL ACCESS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProtocolNeonCyan,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Cinematic Glowing AI Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(ProtocolNeonOrange.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )
                        .border(2.dp, ProtocolNeonOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Peter AI Neural Copilot",
                        tint = ProtocolNeonOrange,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "PETER AI COPILOT",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp,
                    color = Color.White
                )

                Text(
                    text = "POWERED BY GEMINI 3.5 FLASH & PRO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = ProtocolNeonCyan,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Create a local profile to activate your daily neural query quota and unlock advanced developer reasoning. No account, no cloud - everything stays on this device.",
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Feature checklist with themed icons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF070B14).copy(alpha = 0.7f))
                        .padding(12.dp)
                ) {
                    GateFeatureRow(
                        icon = Icons.Default.ElectricBolt,
                        tint = ProtocolNeonOrange,
                        title = "Daily Quota & Model Tiers",
                        subtitle = "Free 25/day (Flash & Lite) • Pro 100/day (Pro 3.1 & 3.8)"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GateFeatureRow(
                        icon = Icons.Default.TaskAlt,
                        tint = ProtocolNeonCyan,
                        title = "Direct Sprint Task Ingestion",
                        subtitle = "1-click add AI-generated backlog items directly to your sprint board"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GateFeatureRow(
                        icon = Icons.Default.VerifiedUser,
                        tint = DevEmerald,
                        title = "100% Local-First Storage",
                        subtitle = "Everything lives in the on-device database - nothing is uploaded, ever"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Themed Google Sign-In Button with Bounce Click Animation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF4285F4), Color(0xFF2563EB))
                            )
                        )
                        .bounceClick {
                            showAccountDialog = true
                        }
                        .testTag("google_sign_in_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Create Local Profile",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.3.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Google Account Selection Dialog
    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ProtocolNeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Local Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Pick a name or email for your on-device profile. Nothing is sent anywhere - the profile lives only in this app's storage.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Spacer(modifier = Modifier.height(12.dp))

                    // Or customize email input
                    OutlinedTextField(
                        value = selectedEmail,
                        onValueChange = {
                            selectedEmail = it
                            selectedName = it.substringBefore("@").replaceFirstChar { c -> c.uppercase() }
                        },
                        label = { Text("Profile name or email (local only)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ProtocolNeonCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = geminiApiKey,
                        onValueChange = { geminiApiKey = it },
                        label = { Text("Gemini API key (optional - powers Peter AI)") },
                        placeholder = { Text("Paste your key from Google AI Studio") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ProtocolNeonOrange,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedEmail.isNotBlank(),
                    onClick = {
                        onSignIn(selectedEmail.trim(), selectedName, geminiApiKey)
                        showAccountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProtocolNeonOrange)
                ) {
                    Text("Confirm & Sign In", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GateFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

/**
 * Top Google Profile & Live Quota HUD in Peter AI Chat Screen.
 */
@Composable
fun ProtocolUserQuotaHUD(
    auth: GitHubAuthState.Authenticated,
    quotaState: UserQuotaState,
    onOpenUpgradeDialog: () -> Unit,
    onResetQuota: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = quotaState.progressPercent,
        animationSpec = tween(500),
        label = "quotaProgress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ProtocolElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, ProtocolCobaltBlue.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // User Row: Avatar + Name + Tier Badge + Sign Out
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar with laser rim
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(ProtocolNeonOrange, ProtocolNeonCyan)
                                )
                            )
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF070B14)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = auth.displayName?.firstOrNull()?.uppercase() ?: "D",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = auth.displayName ?: "Developer",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            // Google verified check
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Google Verified",
                                tint = ProtocolNeonCyan,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = auth.email ?: "google.com",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Subscription Badge (Clickable to change tier)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(quotaState.tier.badgeColor.copy(alpha = 0.18f))
                        .border(1.dp, quotaState.tier.badgeColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .bounceClick { onOpenUpgradeDialog() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("tier_upgrade_badge"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = quotaState.tier.badgeColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = quotaState.tier.title.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = quotaState.tier.badgeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Sign Out Button
                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Switch Google Account",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quota Bar & Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DAILY QUOTA: ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = if (quotaState.tier == SubscriptionTier.ENTERPRISE) "UNLIMITED"
                        else "${quotaState.remainingQueries} / ${quotaState.dailyLimit} REMAINING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (quotaState.hasQuota) ProtocolNeonCyan else ProtocolNeonOrange
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Reset Quota",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ProtocolNeonCyan,
                        modifier = Modifier.clickable { onResetQuota() }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Quota",
                        tint = ProtocolNeonCyan,
                        modifier = Modifier
                            .size(11.dp)
                            .clickable { onResetQuota() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (quotaState.hasQuota) ProtocolNeonCyan else ProtocolNeonOrange,
                trackColor = Color(0xFF1F3358)
            )
        }
    }
}

/**
 * Protocol Subscription Tier Selection & Upgrade Dialog.
 */
@Composable
fun ProtocolSubscriptionDialog(
    currentTier: SubscriptionTier,
    onSelectTier: (SubscriptionTier) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ProtocolNeonOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PROTOCOL SUBSCRIPTION",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }
                Text(
                    text = "Select your daily query quota & Gemini model tier",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SubscriptionTier.entries.forEach { tier ->
                    val isSelected = tier == currentTier
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick {
                                onSelectTier(tier)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) tier.badgeColor.copy(alpha = 0.18f)
                            else ProtocolElevated
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isSelected) tier.badgeColor else Color(0xFF1F3358)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tier.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) tier.badgeColor else Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tier.tag,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = tier.badgeColor
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Active",
                                        tint = tier.badgeColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = tier.description,
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 15.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            tier.perks.take(2).forEach { perk ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "•",
                                        color = tier.badgeColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = perk,
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = ProtocolNeonCyan)
            }
        },
        containerColor = ProtocolBg
    )
}
