package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DevCyan
import com.example.ui.theme.DevEmerald
import com.example.ui.theme.ProtocolNeonCyan
import com.example.ui.theme.ProtocolNeonOrange

enum class SubscriptionTier(
    val id: String,
    val title: String,
    val tag: String,
    val dailyQuota: Int,
    val allowedModels: Set<String>,
    val badgeColor: Color,
    val description: String,
    val perks: List<String>
) {
    FREE(
        id = "free",
        title = "Protocol Free",
        tag = "TIER 01 // FREE",
        dailyQuota = 25,
        allowedModels = setOf("gemini-3.5-flash", "gemini-3.1-flash-lite-preview"),
        badgeColor = ProtocolNeonCyan,
        description = "Essential developer assistant with 25 queries/day",
        perks = listOf(
            "25 queries / day quota",
            "Gemini 3.5 Flash & 3.1 Flash Lite access",
            "Automatic sprint task generation",
            "Git command & rebase syntax assistant"
        )
    ),
    PRO(
        id = "pro",
        title = "Protocol Pro Dev",
        tag = "TIER 02 // PRO",
        dailyQuota = 100,
        allowedModels = setOf("gemini-3.5-flash", "gemini-3.1-flash-lite-preview", "gemini-3.1-pro-preview", "gemini-3.8-flash"),
        badgeColor = ProtocolNeonOrange,
        description = "High-velocity engineer suite with 100 queries/day & Pro models",
        perks = listOf(
            "100 queries / day quota",
            "Gemini 3.1 Pro Preview (Deep Code & Architecture)",
            "Gemini 3.8 Flash Preview",
            "Full PR code reviews & security audits",
            "Priority neural inference"
        )
    ),
    ENTERPRISE(
        id = "enterprise",
        title = "Protocol Syndicate",
        tag = "TIER 03 // UNLIMITED",
        dailyQuota = 9999,
        allowedModels = setOf("gemini-3.5-flash", "gemini-3.1-flash-lite-preview", "gemini-3.1-pro-preview", "gemini-3.8-flash"),
        badgeColor = DevEmerald,
        description = "Unrestricted corporate developer syndicate with unlimited quota",
        perks = listOf(
            "Unlimited daily neural queries",
            "All current & future Gemini models unlocked",
            "Multi-repo cloud sync with Firestore",
            "Dedicated developer copilot channels"
        )
    );

    fun isModelAllowed(modelId: String): Boolean = allowedModels.contains(modelId)

    companion object {
        fun fromId(id: String?): SubscriptionTier {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: FREE
        }
    }
}

data class UserQuotaState(
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val queriesUsedToday: Int = 0,
    val lastResetDateStr: String = ""
) {
    val dailyLimit: Int get() = tier.dailyQuota
    val remainingQueries: Int get() = maxOf(0, dailyLimit - queriesUsedToday)
    val hasQuota: Boolean get() = tier == SubscriptionTier.ENTERPRISE || remainingQueries > 0
    val progressPercent: Float
        get() {
            if (tier == SubscriptionTier.ENTERPRISE) return 1.0f
            if (dailyLimit <= 0) return 0f
            return (remainingQueries.toFloat() / dailyLimit.toFloat()).coerceIn(0f, 1f)
        }
}
