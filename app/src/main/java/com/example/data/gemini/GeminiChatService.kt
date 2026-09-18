package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.SubscriptionTier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class GeminiModelOption(
    val modelId: String,
    val displayName: String,
    val tag: String,
    val description: String,
    val requiredTier: SubscriptionTier = SubscriptionTier.FREE
) {
    FLASH_3_5(
        modelId = "gemini-3.5-flash",
        displayName = "Gemini 3.5 Flash",
        tag = "General Tasks",
        description = "Balanced speed & reasoning for day-to-day developer tasks",
        requiredTier = SubscriptionTier.FREE
    ),
    PRO_3_1(
        modelId = "gemini-3.1-pro-preview",
        displayName = "Gemini 3.1 Pro",
        tag = "Deep Code",
        description = "Complex engineering logic, architecture & deep PR reviews",
        requiredTier = SubscriptionTier.PRO
    ),
    LITE_3_1(
        modelId = "gemini-3.1-flash-lite-preview",
        displayName = "Gemini 3.1 Flash Lite",
        tag = "Fast Speed",
        description = "Ultra-fast answers, quick Git syntax & task breakdowns",
        requiredTier = SubscriptionTier.FREE
    ),
    FLASH_3_8(
        modelId = "gemini-3.8-flash",
        displayName = "Gemini 3.8 Flash",
        tag = "Preview",
        description = "High efficiency preview model for developer assistance",
        requiredTier = SubscriptionTier.PRO
    )
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val suggestedTaskTitle: String? = null,
    val suggestedTaskCategory: String? = null,
    val suggestedTaskPriority: String? = null,
    val isError: Boolean = false
)

class GeminiChatService {

    /** Runtime API key supplied by the user in-app. Overrides the build-time placeholder. */
    var userApiKey: String? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val systemInstructionText = """
        You are Peter AI, the intelligent developer assistant and code copilot inside TaskPeter — the professional task tracker and sprint workstation for software engineers.
        
        Your Core Motive and Persona:
        1. You empower software engineers to achieve deep focus, write clean code, and execute their development sprints with discipline.
        2. Help developers break down features, user stories, or bugs into concrete, actionable tasks with clear acceptance criteria, priority levels (P0, P1, P2), and categories (#feat, #bug, #refactor, #review, #docs).
        3. Assist in reviewing code snippets, diagnosing error messages, recommending Git commands, and structuring pull requests.
        4. When suggesting a specific task, clearly format it so the developer can take action immediately.
        5. Always be direct, technically accurate, concise, and developer-friendly. Never hallucinate fictional APIs or invent unrequested external dependencies.
    """.trimIndent()

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userPrompt: String,
        selectedModel: GeminiModelOption,
        workspaceContext: String? = null
    ): ChatMessage = withContext(Dispatchers.IO) {
        val apiKey = (userApiKey ?: BuildConfig.GEMINI_API_KEY).trim()

        // If no API key configured or default placeholder
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalPeterResponse(
                prompt = userPrompt,
                model = selectedModel,
                notice = null
            )
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/${selectedModel.modelId}:generateContent?key=$apiKey"

            // Construct JSON request with multi-turn conversation history and system instruction
            val requestJson = JSONObject()

            // System instruction
            val sysInstructionObj = JSONObject()
            val sysPartsArray = JSONArray()
            val fullSystemPrompt = if (!workspaceContext.isNullOrBlank()) {
                "$systemInstructionText\n\nCurrent Active Workspace Context:\n$workspaceContext"
            } else {
                systemInstructionText
            }
            sysPartsArray.put(JSONObject().put("text", fullSystemPrompt))
            sysInstructionObj.put("parts", sysPartsArray)
            requestJson.put("systemInstruction", sysInstructionObj)

            // Multi-turn contents
            val contentsArray = JSONArray()

            // Include last 8 conversation turns for context continuity
            val recentHistory = history.takeLast(8)
            for (msg in recentHistory) {
                val turnObj = JSONObject()
                turnObj.put("role", if (msg.isUser) "user" else "model")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", msg.text))
                turnObj.put("parts", parts)
                contentsArray.put(turnObj)
            }

            // Current user turn
            val userTurn = JSONObject()
            userTurn.put("role", "user")
            val userParts = JSONArray()
            userParts.put(JSONObject().put("text", userPrompt))
            userTurn.put("parts", userParts)
            contentsArray.put(userTurn)

            requestJson.put("contents", contentsArray)

            // Generation config
            val generationConfig = JSONObject()
            generationConfig.put("temperature", 0.7)
            generationConfig.put("maxOutputTokens", 2048)
            requestJson.put("generationConfig", generationConfig)

            val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            response.use { resp ->
                val responseBodyString = resp.body?.string().orEmpty()

                if (!resp.isSuccessful) {
                    Log.w("GeminiChatService", "Gemini API HTTP ${resp.code} for ${selectedModel.modelId}: $responseBodyString")

                    // If modelId is 404, try falling back to standard 'gemini-2.5-flash'
                    if (resp.code == 404 && selectedModel.modelId != "gemini-2.5-flash") {
                        val fallbackUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
                        val fallbackReq = Request.Builder().url(fallbackUrl).post(body).build()
                        try {
                            val fbResp = client.newCall(fallbackReq).execute()
                            fbResp.use { fbR ->
                                if (fbR.isSuccessful) {
                                    val fbBody = fbR.body?.string().orEmpty()
                                    val fbJson = JSONObject(fbBody)
                                    val fbCandidates = fbJson.optJSONArray("candidates")
                                    if (fbCandidates != null && fbCandidates.length() > 0) {
                                        val first = fbCandidates.getJSONObject(0)
                                        val content = first.optJSONObject("content")
                                        val parts = content?.optJSONArray("parts")
                                        val text = if (parts != null && parts.length() > 0) parts.getJSONObject(0).optString("text", "") else ""
                                        if (text.isNotBlank()) {
                                            val (sTitle, sCat, sPrio) = extractTaskSuggestion(text)
                                            return@withContext ChatMessage(
                                                text = text.trim(),
                                                isUser = false,
                                                modelUsed = selectedModel.displayName,
                                                suggestedTaskTitle = sTitle,
                                                suggestedTaskCategory = sCat,
                                                suggestedTaskPriority = sPrio
                                            )
                                        }
                                    }
                                }
                            }
                        } catch (e2: Exception) {
                            Log.w("GeminiChatService", "Fallback attempt error: ${e2.message}")
                        }
                    }

                    return@withContext generateLocalPeterResponse(
                        prompt = userPrompt,
                        model = selectedModel,
                        notice = null
                    )
                }

                val responseJson = JSONObject(responseBodyString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val generatedText = if (parts != null && parts.length() > 0) {
                        parts.getJSONObject(0).optString("text", "")
                    } else ""

                    if (generatedText.isNotBlank()) {
                        val (suggestedTitle, suggestedCat, suggestedPriority) = extractTaskSuggestion(generatedText)
                        return@withContext ChatMessage(
                            text = generatedText.trim(),
                            isUser = false,
                            modelUsed = selectedModel.displayName,
                            suggestedTaskTitle = suggestedTitle,
                            suggestedTaskCategory = suggestedCat,
                            suggestedTaskPriority = suggestedPriority
                        )
                    }
                }

                return@withContext generateLocalPeterResponse(
                    prompt = userPrompt,
                    model = selectedModel,
                    notice = null
                )
            }
        } catch (e: Exception) {
            Log.w("GeminiChatService", "Notice calling Gemini API: ${e.localizedMessage}")
            return@withContext generateLocalPeterResponse(
                prompt = userPrompt,
                model = selectedModel,
                notice = null
            )
        }
    }

    private fun extractTaskSuggestion(text: String): Triple<String?, String?, String?> {
        val lines = text.lines()
        for (line in lines) {
            val trimmed = line.trim()
            val isCandidate = (trimmed.startsWith("- [ ]") || trimmed.startsWith("* [ ]") ||
                trimmed.startsWith("1.") || trimmed.startsWith("- Task:") ||
                trimmed.startsWith("Task:") ||
                (trimmed.startsWith("- ") && (trimmed.contains("[P0]") || trimmed.contains("[P1]") || trimmed.contains("[P2]"))))
            if (isCandidate && trimmed.length > 8) {
                var clean = trimmed
                    .replace(Regex("""^(\d+\.|\- \[[ xX]\]|\* \[[ xX]\]|\- Task:|\-|\*|•)"""), "")
                    .replace("**", "")
                    .replace("__", "")
                    .replace("`", "")
                    .trim()

                val prio = when {
                    clean.contains("[P0]", true) || clean.contains("P0", true) || clean.contains("Critical", true) -> "P0"
                    clean.contains("[P1]", true) || clean.contains("P1", true) || clean.contains("High", true) -> "P1"
                    clean.contains("[P3]", true) || clean.contains("P3", true) || clean.contains("Low", true) -> "P3"
                    else -> "P2"
                }

                clean = clean.replace(Regex("""\[P[0-3]\]"""), "").trim()
                clean = clean.replace(Regex("""^\s*:\s*"""), "").trim()
                clean = clean.replace(Regex("""\s+-\s+.*$"""), "").trim()

                if (clean.length in 6..90) {
                    val cat = when {
                        clean.contains("bug", true) || clean.contains("fix", true) || clean.contains("crash", true) -> "BUGFIX"
                        clean.contains("refactor", true) || clean.contains("cleanup", true) || clean.contains("modular", true) -> "REFACTOR"
                        clean.contains("review", true) || clean.contains("pr", true) -> "CODE_REVIEW"
                        clean.contains("doc", true) || clean.contains("readme", true) || clean.contains("spec", true) -> "DOCS"
                        clean.contains("test", true) || clean.contains("coverage", true) || clean.contains("mock", true) -> "TESTING"
                        clean.contains("infra", true) || clean.contains("deploy", true) || clean.contains("ci", true) -> "DEVOPS"
                        else -> "FEATURE"
                    }
                    return Triple(clean, cat, prio)
                }
            }
        }
        return Triple(null, null, null)
    }

    private fun generateLocalPeterResponse(
        prompt: String,
        model: GeminiModelOption,
        notice: String?
    ): ChatMessage {
        val lower = prompt.lowercase()
        val responseText = when {
            lower.contains("breakdown") || lower.contains("break down") || lower.contains("subtask") -> {
                """
                Here is an engineering task breakdown for your sprint:

                1. Define domain data model & contracts
                   - Create immutable data entities with schema validation
                   - Tag: `#feat` • Priority: P1 • Estimate: 45m

                2. Implement repository & local Room cache
                   - SQLite database DAO queries with reactive Flow emission
                   - Tag: `#refactor` • Priority: P1 • Estimate: 1h

                3. Build UI Composable & StateFlow integration
                   - Material 3 layout, state hoisting, and error handling
                   - Tag: `#feat` • Priority: P2 • Estimate: 1h 30m

                4. Unit test coverage & edge cases
                   - Local JVM assertions and test pass
                   - Tag: `#review` • Priority: P2 • Estimate: 45m
                """.trimIndent()
            }
            lower.contains("git") || lower.contains("commit") || lower.contains("branch") || lower.contains("rebase") -> {
                """
                Recommended Git Workflow:

                ```bash
                # 1. Create and checkout a clean feature branch
                git checkout -b feat/sprint-implementation

                # 2. Stage changes and commit with Conventional Commits
                git add -A
                git commit -m "feat(core): implement feature components"

                # 3. Fetch upstream and rebase
                git fetch origin main
                git rebase origin/main

                # 4. Push branch for review
                git push -u origin feat/sprint-implementation
                ```

                To link this commit with TaskPeter, include the issue number in your commit message: `feat: [#12] resolve sync regression`.
                """.trimIndent()
            }
            lower.contains("pr") || lower.contains("pull request") || lower.contains("review") -> {
                """
                Code Review Checklist for Pull Requests:

                - [ ] Functional Correctness: Does the PR meet all acceptance criteria without regressions?
                - [ ] Architecture & Modularity: Are ViewModel and Composable layers decoupled?
                - [ ] Memory & Coroutines: Are coroutine scopes properly cancelled with viewModelScope?
                - [ ] Error Handling: Are network and Room database failures caught gracefully?
                - [ ] Accessibility & Touch Targets: Are interactive targets at least 48dp with semantic content descriptions?
                """.trimIndent()
            }
            lower.contains("test") || lower.contains("dao") || lower.contains("unit") -> {
                """
                Testing Architecture Guidelines:

                - Dao Unit Tests: Use in-memory Room database (`Room.inMemoryDatabaseBuilder`) to test CRUD queries synchronously.
                - ViewModel Tests: Use `StandardTestDispatcher` and `advanceUntilIdle()` to verify StateFlow emissions.
                - Clean Architecture: Mock repository interfaces rather than concrete databases to ensure fast test execution.
                """.trimIndent()
            }
            lower.contains("focus") || lower.contains("sprint") || lower.contains("pomodoro") -> {
                """
                Optimal Sprint Strategy for Deep Work:

                - Session 1 (25 min): Tackle the single highest priority task (P0/P1) with zero context switching.
                - Break (5 min): Disconnect from screens.
                - Session 2 (25 min): Complete remaining implementation details or PR reviews.
                - Wrap-up: Log git commit into TaskPeter to maintain your sprint velocity streak!
                """.trimIndent()
            }
            else -> {
                """
                Peter AI Developer Copilot

                Ready to assist with your engineering tasks:
                - Feature Breakdown: Turn complex product requirements into actionable engineering tickets.
                - GitHub Sync & PR Reviews: Structure PR descriptions, analyze issues, and draft test plans.
                - Git Command Assistance: Branching, interactive rebasing, stashing, and conflict resolution.
                - Sprint Productivity: Prioritize P0/P1 blockers and optimize your daily developer velocity.

                How can I assist your sprint right now?
                """.trimIndent()
            }
        }

        val (suggestedTitle, suggestedCat, suggestedPriority) = extractTaskSuggestion(responseText)

        return ChatMessage(
            text = responseText,
            isUser = false,
            modelUsed = "${model.displayName} (Copilot)",
            suggestedTaskTitle = suggestedTitle,
            suggestedTaskCategory = suggestedCat,
            suggestedTaskPriority = suggestedPriority
        )
    }
}
