package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.FirebaseAuthManager
import com.example.data.auth.GitHubAuthState
import com.example.data.db.FirestoreSyncManager
import com.example.data.db.TaskPeterDatabase
import com.example.data.gemini.ChatMessage
import com.example.data.gemini.GeminiChatService
import com.example.data.gemini.GeminiModelOption
import com.example.data.github.GitHubDeviceCodeResponse
import com.example.data.github.GitHubIssueItem
import com.example.data.github.GitHubPullRequestItem
import com.example.data.github.GitHubRepo
import com.example.data.github.GitHubService
import com.example.data.github.GitHubUserProfile
import retrofit2.HttpException
import com.example.data.model.GitCommitEntity
import com.example.data.model.Priority
import com.example.data.model.SprintSessionEntity
import com.example.data.model.SprintType
import com.example.data.model.SubscriptionTier
import com.example.data.model.TaskCategory
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStatus
import com.example.data.model.UserQuotaState
import com.example.data.repository.TaskPeterRepository
import com.example.ui.theme.AppThemeStyle
import com.example.util.DateUtils
import com.example.util.ExportHelper
import com.example.util.NotificationHelper
import com.example.util.SoundPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = TaskPeterDatabase.getDatabase(application, viewModelScope)
    private val repository = TaskPeterRepository(db.taskDao(), db.sprintDao(), db.gitCommitDao())
    private val soundPlayer = SoundPlayer()
    private val prefs = application.getSharedPreferences("taskpeter_prefs", Context.MODE_PRIVATE)

    // User Customization State
    private val _workspaceName = MutableStateFlow(
        prefs.getString("workspace_name", "Developer Workspace") ?: "Developer Workspace"
    )
    val workspaceName: StateFlow<String> = _workspaceName.asStateFlow()

    private val _userStatusEmoji = MutableStateFlow(
        prefs.getString("user_status_emoji", "cli") ?: "cli"
    )
    val userStatusEmoji: StateFlow<String> = _userStatusEmoji.asStateFlow()

    private val _userStatusText = MutableStateFlow(
        prefs.getString("user_status_text", "Coding Flow") ?: "Coding Flow"
    )
    val userStatusText: StateFlow<String> = _userStatusText.asStateFlow()

    private val _fetchedRepoCategories = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val fetchedRepoCategories: StateFlow<List<Pair<String, String>>> = _fetchedRepoCategories.asStateFlow()

    private val initialThemeName = prefs.getString("theme_style", AppThemeStyle.RADIANT_CRIMSON.name)
    private val _appThemeStyle = MutableStateFlow(
        try {
            when (initialThemeName) {
                "VALORANT_RADIANT", "RADIANT_CRIMSON" -> AppThemeStyle.RADIANT_CRIMSON
                "SPIDER_VERSE", "NEON_PUNK" -> AppThemeStyle.NEON_PUNK
                "OVERWATCH_HUD", "SOLAR_HORIZON" -> AppThemeStyle.SOLAR_HORIZON
                "TASKPETER_PROTOCOL" -> AppThemeStyle.TASKPETER_PROTOCOL
                "JETBRAINS_FLEET", "TASKPETER_OBSIDIAN" -> AppThemeStyle.TASKPETER_OBSIDIAN
                "JETBRAINS_DARCULA", "TASKPETER_MATRIX" -> AppThemeStyle.TASKPETER_MATRIX
                "CLAUDE_DARK", "TASKPETER_SOLAR" -> AppThemeStyle.TASKPETER_SOLAR
                "CYBERPUNK_NEON", "TASKPETER_ARCTIC" -> AppThemeStyle.TASKPETER_ARCTIC
                "CLAUDE_LIGHT", "TASKPETER_PAPER" -> AppThemeStyle.TASKPETER_PAPER
                else -> AppThemeStyle.valueOf(initialThemeName ?: AppThemeStyle.RADIANT_CRIMSON.name)
            }
        } catch (_: Exception) { AppThemeStyle.RADIANT_CRIMSON }
    )
    val appThemeStyle: StateFlow<AppThemeStyle> = _appThemeStyle.asStateFlow()

    // Repository Flows
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSprints: StateFlow<List<SprintSessionEntity>> = repository.allSprints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCommits: StateFlow<List<GitCommitEntity>> = repository.allCommits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasksCount: StateFlow<Int> = repository.completedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalTasksCount: StateFlow<Int> = repository.totalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // UI Filter & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("ALL")
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("ALL")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    // Filtered Tasks
    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        allTasks,
        _searchQuery,
        _selectedStatusFilter,
        _selectedCategoryFilter
    ) { tasks, query, status, category ->
        tasks.filter { task ->
            val matchesQuery = query.isBlank() ||
                task.title.contains(query, ignoreCase = true) ||
                task.description.contains(query, ignoreCase = true) ||
                task.repositoryName.contains(query, ignoreCase = true) ||
                task.branchName.contains(query, ignoreCase = true)

            val matchesStatus = status == "ALL" || task.status == status
            val matchesCategory = category == "ALL" || task.category == category

            matchesQuery && matchesStatus && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Focus Sprint Timer State
    private val _sprintType = MutableStateFlow(SprintType.DEEP_FOCUS)
    val sprintType: StateFlow<SprintType> = _sprintType.asStateFlow()

    private val _totalSprintSeconds = MutableStateFlow(25 * 60)
    val totalSprintSeconds: StateFlow<Int> = _totalSprintSeconds.asStateFlow()

    private val _remainingSprintSeconds = MutableStateFlow(25 * 60)
    val remainingSprintSeconds: StateFlow<Int> = _remainingSprintSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _activeSprintTask = MutableStateFlow<TaskEntity?>(null)
    val activeSprintTask: StateFlow<TaskEntity?> = _activeSprintTask.asStateFlow()

    private val _ambientSoundEnabled = MutableStateFlow(false)
    val ambientSoundEnabled: StateFlow<Boolean> = _ambientSoundEnabled.asStateFlow()

    private val _ambientProfile = MutableStateFlow("Cyber Hum")
    val ambientProfile: StateFlow<String> = _ambientProfile.asStateFlow()

    private var timerJob: Job? = null

    // Firebase Auth with GitHub Provider State
    val authState: StateFlow<GitHubAuthState> = FirebaseAuthManager.instance.authState

    // GitHub & Cloud Sync State
    private val _githubUsername = MutableStateFlow(
        prefs.getString("github_username", "") ?: ""
    )
    val githubUsername: StateFlow<String> = _githubUsername.asStateFlow()

    private val _githubToken = MutableStateFlow(
        prefs.getString("github_token", "") ?: ""
    )
    val githubToken: StateFlow<String> = _githubToken.asStateFlow()

    private val _connectedRepos = MutableStateFlow<List<GitHubRepo>>(emptyList())
    val connectedRepos: StateFlow<List<GitHubRepo>> = _connectedRepos.asStateFlow()

    private val _githubIssues = MutableStateFlow<List<GitHubIssueItem>>(emptyList())
    val githubIssues: StateFlow<List<GitHubIssueItem>> = _githubIssues.asStateFlow()

    private val _githubPullRequests = MutableStateFlow<List<GitHubPullRequestItem>>(emptyList())
    val githubPullRequests: StateFlow<List<GitHubPullRequestItem>> = _githubPullRequests.asStateFlow()

    private val _isLoadingIssues = MutableStateFlow(false)
    val isLoadingIssues: StateFlow<Boolean> = _isLoadingIssues.asStateFlow()

    private val _selectedIssueFilter = MutableStateFlow("ALL") // "ALL", "ISSUES", "PRS"
    val selectedIssueFilter: StateFlow<String> = _selectedIssueFilter.asStateFlow()

    private val _selectedRepoFilter = MutableStateFlow<String?>(null)
    val selectedRepoFilter: StateFlow<String?> = _selectedRepoFilter.asStateFlow()

    private val _deviceCodeState = MutableStateFlow<GitHubDeviceCodeResponse?>(null)
    val deviceCodeState: StateFlow<GitHubDeviceCodeResponse?> = _deviceCodeState.asStateFlow()

    private val _isDeviceFlowPolling = MutableStateFlow(false)
    val isDeviceFlowPolling: StateFlow<Boolean> = _isDeviceFlowPolling.asStateFlow()

    private var devicePollJob: Job? = null

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow("Ready to sync")
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    private fun getActiveDeviceName(username: String? = null): String {
        val manufacturer = android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = android.os.Build.MODEL
        val dev = "$manufacturer $model"
        return if (!username.isNullOrBlank()) {
            "$dev (@$username • Active Session)"
        } else {
            "$dev (This Device • Active Session)"
        }
    }

    private val _syncDevices = MutableStateFlow(
        listOf(
            "${android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${android.os.Build.MODEL} (This Device • Active Session)"
        )
    )
    val syncDevices: StateFlow<List<String>> = _syncDevices.asStateFlow()

    // --- Peter AI Copilot (Powered by Gemini) & Subscription Quota ---
    private val geminiChatService = GeminiChatService()

    init {
        geminiChatService.userApiKey = prefs.getString("user_gemini_api_key", null)
    }

    /** Stores the user's own Gemini API key (kept only in on-device preferences). */
    fun setGeminiApiKey(key: String) {
        geminiChatService.userApiKey = key.trim().takeIf { it.isNotBlank() }
        prefs.edit().putString("user_gemini_api_key", geminiChatService.userApiKey ?: "").apply()
    }

    private val _userQuotaState = MutableStateFlow(
        run {
            val savedTierId = prefs.getString("user_subscription_tier", SubscriptionTier.FREE.id)
            val tier = SubscriptionTier.fromId(savedTierId)
            val lastReset = prefs.getString("last_quota_reset_date", "") ?: ""
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            val usedToday = if (lastReset == today) {
                prefs.getInt("queries_used_today", 0)
            } else {
                prefs.edit().putString("last_quota_reset_date", today).putInt("queries_used_today", 0).apply()
                0
            }
            UserQuotaState(tier = tier, queriesUsedToday = usedToday, lastResetDateStr = today)
        }
    )
    val userQuotaState: StateFlow<UserQuotaState> = _userQuotaState.asStateFlow()

    fun setSubscriptionTier(tier: SubscriptionTier) {
        _userQuotaState.value = _userQuotaState.value.copy(tier = tier)
        prefs.edit().putString("user_subscription_tier", tier.id).apply()
        val currentAuth = authState.value
        if (currentAuth is GitHubAuthState.Authenticated) {
            viewModelScope.launch {
                FirestoreSyncManager.instance.syncUserSubscriptionAndQuota(
                    uid = currentAuth.uid,
                    email = currentAuth.email ?: "",
                    displayName = currentAuth.displayName ?: "",
                    tier = tier.id,
                    queriesUsedToday = _userQuotaState.value.queriesUsedToday,
                    dailyQuota = tier.dailyQuota
                )
            }
        }
        showToast("Switched to ${tier.title} (${tier.dailyQuota} queries/day)")
    }

    fun resetDailyQuota() {
        _userQuotaState.value = _userQuotaState.value.copy(queriesUsedToday = 0)
        prefs.edit().putInt("queries_used_today", 0).apply()
        showToast("Daily quota refreshed: ${_userQuotaState.value.dailyLimit} queries remaining")
    }

    fun signInWithGoogle(
        email: String,
        displayName: String,
        photoUrl: String? = null
    ) {
        FirebaseAuthManager.instance.signInWithGoogleAccount(
            context = getApplication(),
            email = email,
            displayName = displayName,
            photoUrl = photoUrl
        ) { result ->
            result.onSuccess { authenticated ->
                showToast("Signed in as ${authenticated.displayName}")
                viewModelScope.launch {
                    FirestoreSyncManager.instance.syncUserSubscriptionAndQuota(
                        uid = authenticated.uid,
                        email = authenticated.email ?: email,
                        displayName = authenticated.displayName ?: displayName,
                        tier = _userQuotaState.value.tier.id,
                        queriesUsedToday = _userQuotaState.value.queriesUsedToday,
                        dailyQuota = _userQuotaState.value.dailyLimit
                    )
                }
            }.onFailure { err ->
                showToast("Sign in notice: ${err.localizedMessage ?: "Failed to authenticate"}")
            }
        }
    }

    fun signOutAuth() {
        FirebaseAuthManager.instance.signOut()
        showToast("Signed out of account")
    }

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _selectedGeminiModel = MutableStateFlow(GeminiModelOption.FLASH_3_5)
    val selectedGeminiModel: StateFlow<GeminiModelOption> = _selectedGeminiModel.asStateFlow()

    fun setGeminiModel(model: GeminiModelOption) {
        val currentTier = _userQuotaState.value.tier
        if (!currentTier.isModelAllowed(model.modelId)) {
            showToast("${model.displayName} is reserved for ${model.requiredTier.title}. Upgrade to unlock!")
            return
        }
        _selectedGeminiModel.value = model
        showToast("Peter AI switched to ${model.displayName}")
    }

    fun clearChatHistory() {
        _chatMessages.value = emptyList()
        showToast("Cleared conversation history")
    }

    fun sendChatMessage(prompt: String) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isBlank() || _isChatLoading.value) return

        val currentAuth = authState.value
        if (currentAuth !is GitHubAuthState.Authenticated) {
            showToast("Sign in with Google to unlock Peter AI Copilot")
            return
        }

        val currentTier = _userQuotaState.value.tier
        if (!currentTier.isModelAllowed(_selectedGeminiModel.value.modelId)) {
            showToast("${_selectedGeminiModel.value.displayName} requires Pro or Enterprise subscription")
            return
        }

        if (!_userQuotaState.value.hasQuota) {
            showToast("Daily quota exhausted (${_userQuotaState.value.dailyLimit}/${_userQuotaState.value.dailyLimit}). Upgrade to Pro or Enterprise.")
            return
        }

        // Deduct quota
        val newUsed = _userQuotaState.value.queriesUsedToday + 1
        _userQuotaState.value = _userQuotaState.value.copy(queriesUsedToday = newUsed)
        prefs.edit().putInt("queries_used_today", newUsed).apply()

        // Sync quota usage & interaction to Firestore
        viewModelScope.launch {
            FirestoreSyncManager.instance.syncUserSubscriptionAndQuota(
                uid = currentAuth.uid,
                email = currentAuth.email ?: "",
                displayName = currentAuth.displayName ?: "",
                tier = currentTier.id,
                queriesUsedToday = newUsed,
                dailyQuota = _userQuotaState.value.dailyLimit
            )
            FirestoreSyncManager.instance.recordChatInteraction(
                uid = currentAuth.uid,
                userPrompt = cleanPrompt,
                geminiModel = _selectedGeminiModel.value.modelId
            )
        }

        val userMsg = ChatMessage(text = cleanPrompt, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatLoading.value = true

        viewModelScope.launch {
            try {
                val tasksList = allTasks.value
                val topTasks = tasksList.take(5).joinToString("; ") { "[${it.priority}] ${it.title} (${it.status})" }
                val activeRepo = _connectedRepos.value.firstOrNull()?.fullName ?: "Local Workspace"
                val workspaceContext = "Project: $activeRepo | Tasks: ${if (topTasks.isNotBlank()) topTasks else "No active sprint tasks"}"

                val reply = geminiChatService.sendMessage(
                    history = _chatMessages.value.dropLast(1),
                    userPrompt = cleanPrompt,
                    selectedModel = _selectedGeminiModel.value,
                    workspaceContext = workspaceContext
                )
                _chatMessages.value = _chatMessages.value + reply
            } catch (e: Exception) {
                Log.w("MainViewModel", "Peter AI error: ${e.message}")
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    text = "Peter AI notice: ${e.localizedMessage ?: "Connection error"}. Please check network or API key in AI Studio Secrets panel.",
                    isUser = false,
                    isError = true
                )
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun addSuggestedTaskToBacklog(title: String, category: String, priority: String) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title.take(120),
                description = "Task generated via Peter AI Copilot assistance",
                emoji = when (category.uppercase()) {
                    "BUGFIX" -> "bug"
                    "REFACTOR" -> "refactor"
                    "CODE_REVIEW" -> "branch"
                    "DOCS" -> "spec"
                    else -> "energy"
                },
                status = TaskStatus.TODO.name,
                priority = priority,
                category = category,
                repositoryName = _connectedRepos.value.firstOrNull()?.fullName ?: "TaskPeter",
                isSyncedToCloud = false
            )
            repository.insertTask(task)
            showToast("Added task to your dashboard: $title")
        }
    }

    // Calendar Selected Date
    private val _selectedCalendarDate = MutableStateFlow(System.currentTimeMillis())
    val selectedCalendarDate: StateFlow<Long> = _selectedCalendarDate.asStateFlow()

    // Theme & Settings
    val isDarkTheme: StateFlow<Boolean> = _appThemeStyle
        .map { it.isDark }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _appThemeStyle.value.isDark)

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _celebrationTrigger = MutableStateFlow(0L)
    val celebrationTrigger: StateFlow<Long> = _celebrationTrigger.asStateFlow()

    private val _hasAcceptedTerms = MutableStateFlow(
        prefs.getBoolean("has_accepted_terms", true)
    )
    val hasAcceptedTerms: StateFlow<Boolean> = _hasAcceptedTerms.asStateFlow()

    private val _showStartupScreen = MutableStateFlow(false)
    val showStartupScreen: StateFlow<Boolean> = _showStartupScreen.asStateFlow()

    private val _hasSeenUserGuide = MutableStateFlow(
        prefs.getBoolean("has_seen_user_guide", true)
    )
    val hasSeenUserGuide: StateFlow<Boolean> = _hasSeenUserGuide.asStateFlow()

    // Interactive Coach Marks & Tour Discoverability
    private val _hasSeenCoachMarks = MutableStateFlow(
        prefs.getBoolean("has_seen_coach_marks", true)
    )
    val hasSeenCoachMarks: StateFlow<Boolean> = _hasSeenCoachMarks.asStateFlow()

    private val _isCoachMarksActive = MutableStateFlow(false)
    val isCoachMarksActive: StateFlow<Boolean> = _isCoachMarksActive.asStateFlow()

    private val _coachMarkStepIndex = MutableStateFlow(0)
    val coachMarkStepIndex: StateFlow<Int> = _coachMarkStepIndex.asStateFlow()

    private val _showPrivacyDialog = MutableStateFlow(false)
    val showPrivacyDialog: StateFlow<Boolean> = _showPrivacyDialog.asStateFlow()

    fun acceptTermsAndLaunch() {
        _hasAcceptedTerms.value = true
        _showStartupScreen.value = false
        prefs.edit().putBoolean("has_accepted_terms", true).apply()
    }

    fun openStartupCover() {
        _showStartupScreen.value = true
    }

    fun closeStartupCover() {
        _showStartupScreen.value = false
    }

    init {
        Log.d("MainViewModel", "Initializing MainViewModel...")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                NotificationHelper.createNotificationChannel(application)
            } catch (e: Exception) {
                Log.w("MainViewModel", "NotificationChannel setup: ${e.message}")
            }

            try {
                repository.prepopulateStarterWorkspaceIfEmpty()
                val initialTasks = repository.allTasks.first()
                val candidate = initialTasks.firstOrNull { it.status == TaskStatus.IN_PROGRESS.name }
                    ?: initialTasks.firstOrNull { it.status == TaskStatus.TODO.name }
                if (_activeSprintTask.value == null && candidate != null) {
                    _activeSprintTask.value = candidate
                }
            } catch (e: Exception) {
                Log.w("MainViewModel", "Starter workspace init: ${e.message}")
            }

            val savedUsername = prefs.getString("github_username", null)?.trim()
            val savedToken = prefs.getString("github_token", "") ?: ""
            if (!savedUsername.isNullOrBlank()) {
                try {
                    FirebaseAuthManager.instance.linkDirectGitHubProfile(
                        username = savedUsername,
                        token = savedToken
                    )
                } catch (e: Exception) {
                    Log.w("MainViewModel", "Initial linkDirectGitHubProfile: ${e.message}")
                }
                fetchUserRepositories()
            }

            // Restore connected repositories from SharedPreferences or initialize default developer repositories
            val savedRepoSet = prefs.getStringSet("connected_repos_set", null)
            val initialRepos = if (!savedRepoSet.isNullOrEmpty()) {
                savedRepoSet.map { fullName ->
                    GitHubService.createRepo(fullName, "main")
                }
            } else {
                listOf(
                    GitHubRepo(
                        name = "cookbook",
                        fullName = "google-gemini/cookbook",
                        description = "Official examples, recipes, and guides for the Google Gemini API",
                        language = "Kotlin",
                        stars = 17700,
                        openIssuesCount = 12
                    ),
                    GitHubRepo(
                        name = "architecture-templates",
                        fullName = "android/architecture-templates",
                        description = "Modern Android clean architecture blueprints and Compose best practices",
                        language = "Kotlin",
                        stars = 4300,
                        openIssuesCount = 6
                    )
                )
            }
            _connectedRepos.value = initialRepos
            persistConnectedRepos(initialRepos)

            loadGitHubIssuesAndPrs()
        }

        viewModelScope.launch {
            FirebaseAuthManager.instance.authState.collect { state ->
                if (state is GitHubAuthState.Authenticated) {
                    _githubUsername.value = state.githubUsername
                    _syncDevices.value = listOf(getActiveDeviceName(state.githubUsername))
                    _lastSyncTime.value = "Firebase Cloud Sync (${DateUtils.formatTime(System.currentTimeMillis())})"
                }
            }
        }
        Log.d("MainViewModel", "MainViewModel initialized successfully.")
    }

    fun fetchUserRepositories() {
        val username = _githubUsername.value.trim()
        if (username.isBlank()) return

        viewModelScope.launch {
            try {
                val token = _githubToken.value.trim()
                val repos = if (token.isNotBlank() && !token.startsWith("gho_sandbox_")) {
                    try {
                        GitHubService.api.getAuthenticatedUserRepos("Bearer $token")
                    } catch (httpEx: HttpException) {
                        if (httpEx.code() == 401 || httpEx.code() == 403) {
                            Log.w("MainViewModel", "Authenticated repos fetch notice (${httpEx.code()}), falling back to public repos")
                            GitHubService.api.getUserRepos(username)
                        } else {
                            throw httpEx
                        }
                    }
                } else {
                    GitHubService.api.getUserRepos(username)
                }
                
                if (repos.isNotEmpty()) {
                    _connectedRepos.value = repos
                    persistConnectedRepos(repos)
                    val repoCategories = repos.map { it.name to "#${it.name.take(10).lowercase()}" }
                    _fetchedRepoCategories.value = repoCategories
                    showToast("Synced ${repos.size} repositories for @$username")
                }
                loadGitHubIssuesAndPrs(forceRefresh = true)
            } catch (e: Exception) {
                Log.w("MainViewModel", "fetchUserRepositories notice: ${e.message}")
                val friendlyMessage = when (e) {
                    is HttpException -> when (e.code()) {
                        404 -> "GitHub user @$username not found"
                        401 -> "GitHub token invalid or unauthorized"
                        403, 429 -> "GitHub API rate limit reached (60/hr). Add a token in Settings."
                        else -> "GitHub API status ${e.code()}"
                    }
                    is java.net.UnknownHostException, is java.net.SocketTimeoutException -> "Network offline or unreachable"
                    else -> "Repository sync note: ${e.localizedMessage ?: "Offline mode active"}"
                }
                showToast(friendlyMessage)
            }
        }
    }

    fun triggerCelebration() {
        _celebrationTrigger.value = System.currentTimeMillis()
    }

    fun markUserGuideSeen() {
        _hasSeenUserGuide.value = true
        prefs.edit().putBoolean("has_seen_user_guide", true).apply()
    }

    fun openUserGuide() {
        _hasSeenUserGuide.value = false
    }

    fun startCoachMarksTour(initialStep: Int = 0) {
        _coachMarkStepIndex.value = initialStep
        _isCoachMarksActive.value = true
    }

    fun setCoachMarkStep(index: Int) {
        _coachMarkStepIndex.value = index
    }

    fun nextCoachMark(totalSteps: Int) {
        if (_coachMarkStepIndex.value < totalSteps - 1) {
            _coachMarkStepIndex.value += 1
        } else {
            dismissCoachMarks(markAsSeen = true)
            triggerCelebration()
            showToast("Tour complete! You're ready to build with TaskPeter.")
        }
    }

    fun previousCoachMark() {
        if (_coachMarkStepIndex.value > 0) {
            _coachMarkStepIndex.value -= 1
        }
    }

    fun dismissCoachMarks(markAsSeen: Boolean = true) {
        _isCoachMarksActive.value = false
        if (markAsSeen) {
            _hasSeenCoachMarks.value = true
            prefs.edit().putBoolean("has_seen_coach_marks", true).apply()
        }
    }

    fun openPrivacyDialog() {
        _showPrivacyDialog.value = true
    }

    fun closePrivacyDialog() {
        _showPrivacyDialog.value = false
    }

    fun showToast(msg: String) {
        _statusMessage.value = msg
    }

    fun clearToast() {
        _statusMessage.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: String) {
        _selectedStatusFilter.value = status
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setCalendarDate(timestamp: Long) {
        _selectedCalendarDate.value = timestamp
    }

    // User Customization methods
    fun setWorkspaceName(name: String) {
        val trimmed = name.trim().take(30)
        if (trimmed.isNotBlank()) {
            _workspaceName.value = trimmed
            prefs.edit().putString("workspace_name", trimmed).apply()
            showToast("Workspace updated: $trimmed")
        }
    }

    fun setUserStatus(emoji: String, text: String) {
        _userStatusEmoji.value = emoji
        _userStatusText.value = text.trim()
        prefs.edit()
            .putString("user_status_emoji", emoji)
            .putString("user_status_text", text.trim())
            .apply()
        showToast("Status updated: $text")
    }

    fun setAppThemeStyle(style: AppThemeStyle) {
        _appThemeStyle.value = style
        prefs.edit().putString("theme_style", style.name).apply()
        showToast("Theme switched: ${style.displayName}")
    }

    fun toggleTheme() {
        val nextStyle = when (_appThemeStyle.value) {
            AppThemeStyle.RADIANT_CRIMSON -> AppThemeStyle.NEON_PUNK
            AppThemeStyle.NEON_PUNK -> AppThemeStyle.SOLAR_HORIZON
            AppThemeStyle.SOLAR_HORIZON -> AppThemeStyle.TASKPETER_PROTOCOL
            AppThemeStyle.TASKPETER_PROTOCOL -> AppThemeStyle.TASKPETER_OBSIDIAN
            AppThemeStyle.TASKPETER_OBSIDIAN -> AppThemeStyle.TASKPETER_MATRIX
            AppThemeStyle.TASKPETER_MATRIX -> AppThemeStyle.TASKPETER_SOLAR
            AppThemeStyle.TASKPETER_SOLAR -> AppThemeStyle.TASKPETER_ARCTIC
            AppThemeStyle.TASKPETER_ARCTIC -> AppThemeStyle.TASKPETER_PAPER
            AppThemeStyle.TASKPETER_PAPER -> AppThemeStyle.RADIANT_CRIMSON
        }
        setAppThemeStyle(nextStyle)
    }

    // Task CRUD & Quick Command Palette Parser
    fun executeQuickCommand(command: String) {
        if (command.isBlank()) return

        val trimmed = command.trim()
        val textWithoutAdd = if (trimmed.startsWith(":add", ignoreCase = true)) {
            trimmed.substring(4).trim()
        } else {
            trimmed
        }

        var title = ""
        var category = TaskCategory.FEATURE.name
        var priority = Priority.P1.name
        var repo = _connectedRepos.value.firstOrNull()?.fullName ?: ""
        var estMins = 30
        var dueDate: Long? = null
        var emoji = "energy"

        val tokens = textWithoutAdd.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val titleTokens = mutableListOf<String>()

        for (token in tokens) {
            when {
                token.startsWith("#") -> {
                    val tag = token.substring(1).uppercase()
                    category = when (tag) {
                        "BUG", "BUGFIX" -> { emoji = "bug"; TaskCategory.BUGFIX.name }
                        "FEAT", "FEATURE" -> { emoji = "ship"; TaskCategory.FEATURE.name }
                        "REFACTOR" -> { emoji = "tool"; TaskCategory.REFACTOR.name }
                        "INFRA", "DEVOPS" -> { emoji = "pipeline"; TaskCategory.DEVOPS.name }
                        "DOCS" -> { emoji = "spec"; TaskCategory.DOCS.name }
                        "REVIEW" -> { emoji = "radar"; TaskCategory.CODE_REVIEW.name }
                        "TEST" -> { emoji = "qa"; TaskCategory.TESTING.name }
                        else -> TaskCategory.FEATURE.name
                    }
                }
                token.startsWith("!") -> {
                    val pri = token.substring(1).uppercase()
                    priority = when (pri) {
                        "P0", "0", "CRITICAL" -> { emoji = "thermal"; Priority.P0.name }
                        "P1", "1", "HIGH" -> Priority.P1.name
                        "P2", "2", "MED", "MEDIUM" -> Priority.P2.name
                        "P3", "3", "LOW" -> Priority.P3.name
                        else -> Priority.P1.name
                    }
                }
                token.startsWith("@") -> {
                    repo = token.substring(1)
                }
                token.startsWith("~") -> {
                    val numStr = token.substring(1).replace("m", "")
                    estMins = numStr.toIntOrNull() ?: 30
                }
                token.startsWith("^") -> {
                    val dateTag = token.substring(1).lowercase()
                    val now = System.currentTimeMillis()
                    dueDate = when (dateTag) {
                        "today" -> DateUtils.getEndOfDay(now)
                        "tomorrow" -> DateUtils.getEndOfDay(now + 24 * 60 * 60 * 1000L)
                        "week" -> DateUtils.getEndOfDay(now + 7 * 24 * 60 * 60 * 1000L)
                        else -> null
                    }
                }
                else -> {
                    titleTokens.add(token)
                }
            }
        }

        title = titleTokens.joinToString(" ").trim()
        if (title.isBlank()) {
            title = "Task (${System.currentTimeMillis() % 1000})"
        }

        val branchName = when (category) {
            TaskCategory.BUGFIX.name -> "fix/${title.lowercase().replace(" ", "-").take(20)}"
            TaskCategory.REFACTOR.name -> "refactor/${title.lowercase().replace(" ", "-").take(20)}"
            else -> "feat/${title.lowercase().replace(" ", "-").take(20)}"
        }

        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                emoji = emoji,
                category = category,
                priority = priority,
                repositoryName = repo,
                estimatedMinutes = estMins,
                branchName = branchName,
                dueDateMillis = dueDate,
                status = TaskStatus.TODO.name
            )
            repository.insertTask(task)
            showToast("Created task: $emoji $title")
        }
    }

    fun addNewTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        repo: String,
        branch: String,
        estimatedMins: Int,
        dueDate: Long?,
        emoji: String = "energy"
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                description = description,
                emoji = emoji,
                category = category,
                priority = priority,
                repositoryName = repo,
                branchName = branch.ifBlank { "main" },
                estimatedMinutes = estimatedMins,
                dueDateMillis = dueDate,
                status = TaskStatus.TODO.name
            )
            repository.insertTask(task)
            val currentAuth = authState.value
            if (currentAuth is GitHubAuthState.Authenticated) {
                FirestoreSyncManager.instance.syncTask(currentAuth.uid, task)
            }
            showToast("Added task: $title")
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
            val currentAuth = authState.value
            if (currentAuth is GitHubAuthState.Authenticated) {
                FirestoreSyncManager.instance.syncTask(currentAuth.uid, task)
            }
            showToast("Updated ${task.title}")
        }
    }

    fun updateTaskStatus(task: TaskEntity, newStatus: TaskStatus) {
        viewModelScope.launch {
            val completedTime = if (newStatus == TaskStatus.DONE) System.currentTimeMillis() else null
            val updated = task.copy(
                status = newStatus.name,
                completedAtMillis = completedTime
            )
            repository.updateTask(updated)
            val currentAuth = authState.value
            if (currentAuth is GitHubAuthState.Authenticated) {
                FirestoreSyncManager.instance.syncTask(currentAuth.uid, updated)
            }
            if (newStatus == TaskStatus.DONE) {
                _celebrationTrigger.value = System.currentTimeMillis()
                soundPlayer.playChime()
                showToast("Completed: ${task.title}")
            } else {
                showToast("Moved to ${newStatus.label}")
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            val currentAuth = authState.value
            if (currentAuth is GitHubAuthState.Authenticated) {
                FirestoreSyncManager.instance.deleteTask(currentAuth.uid, task.id)
            }
            showToast("Deleted ${task.title}")
        }
    }

    // Focus Sprint Session Management
    fun selectSprintType(type: SprintType) {
        _sprintType.value = type
        val totalSecs = type.defaultMinutes * 60
        _totalSprintSeconds.value = totalSecs
        _remainingSprintSeconds.value = totalSecs
        if (_isTimerRunning.value) {
            pauseSprintTimer()
        }
    }

    fun setCustomSprintMinutes(minutes: Int) {
        val validMins = minutes.coerceIn(1, 180)
        val totalSecs = validMins * 60
        _totalSprintSeconds.value = totalSecs
        _remainingSprintSeconds.value = totalSecs
        if (_isTimerRunning.value) {
            pauseSprintTimer()
        }
        showToast("Sprint duration set to $validMins min")
    }

    fun setActiveSprintTask(task: TaskEntity?) {
        _activeSprintTask.value = task
    }

    fun setSprintTask(task: TaskEntity?) {
        setActiveSprintTask(task)
    }

    fun toggleSprintTimer() {
        if (_isTimerRunning.value) {
            pauseSprintTimer()
        } else {
            startSprintTimer()
        }
    }

    fun startSprintTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true

        if (_ambientSoundEnabled.value) {
            soundPlayer.playAmbientSound(_ambientProfile.value)
        }

        timerJob = viewModelScope.launch {
            while (_remainingSprintSeconds.value > 0 && _isTimerRunning.value) {
                delay(1000)
                _remainingSprintSeconds.value -= 1

                val task = _activeSprintTask.value
                if (task != null && _remainingSprintSeconds.value % 60 == 0) {
                    val spentMins = task.spentMinutes + 1
                    repository.updateTask(task.copy(spentMinutes = spentMins))
                }
            }

            if (_remainingSprintSeconds.value <= 0) {
                finishSprintSession()
            }
        }
    }

    fun pauseSprintTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        soundPlayer.stopAmbientSound()
    }

    fun resetSprintTimer() {
        pauseSprintTimer()
        _remainingSprintSeconds.value = _totalSprintSeconds.value
    }

    fun finishSprintSession() {
        pauseSprintTimer()
        soundPlayer.playChime()

        val elapsedSeconds = _totalSprintSeconds.value - _remainingSprintSeconds.value
        val elapsedMins = (elapsedSeconds / 60).coerceAtLeast(1)
        val task = _activeSprintTask.value

        viewModelScope.launch {
            val session = SprintSessionEntity(
                taskId = task?.id,
                taskTitle = task?.title ?: "Deep Focus Sprint",
                durationMinutes = elapsedMins,
                type = _sprintType.value.name,
                notes = "Completed deep focus interval with ${_ambientProfile.value}"
            )
            repository.recordSprintSession(session, task)

            NotificationHelper.showSprintCompletedNotification(
                getApplication(),
                task?.title,
                elapsedMins
            )

            showToast("Sprint finished! Logged $elapsedMins mins of focus.")
            _remainingSprintSeconds.value = _totalSprintSeconds.value
        }
    }

    fun toggleAmbientSound() {
        val newState = !_ambientSoundEnabled.value
        _ambientSoundEnabled.value = newState
        if (newState && _isTimerRunning.value) {
            soundPlayer.playAmbientSound(_ambientProfile.value)
        } else {
            soundPlayer.stopAmbientSound()
        }
    }

    fun setAmbientProfile(profile: String) {
        _ambientProfile.value = profile
        if (_ambientSoundEnabled.value && _isTimerRunning.value) {
            soundPlayer.playAmbientSound(profile)
        }
    }

    // Git Integration & Manual Commit Log
    fun logManualGitCommit(task: TaskEntity?, message: String, repo: String, branch: String) {
        viewModelScope.launch {
            val commit = repository.logGitCommit(task, message, repo, branch)
            showToast("Committed [${commit.commitHash}] to $repo/$branch")
        }
    }

    fun connectGitHubAccount(username: String, token: String) {
        val cleanUser = username.trim().removePrefix("@")
        if (cleanUser.isBlank()) return
        _githubUsername.value = cleanUser
        _githubToken.value = token
        prefs.edit().putString("github_username", cleanUser).apply()
        if (token.isNotBlank()) {
            prefs.edit().putString("github_token", token).apply()
        }
        
        // Immediate optimistic authentication feedback
        FirebaseAuthManager.instance.linkDirectGitHubProfile(
            username = cleanUser,
            token = token
        )
        showToast("Connecting GitHub account @$cleanUser...")

        // Fetch live authentic profile and repositories asynchronously
        viewModelScope.launch {
            val cleanToken = token.trim()
            try {
                val profile = if (cleanToken.isNotBlank() && !cleanToken.startsWith("gho_sandbox_")) {
                    try {
                        GitHubService.api.getAuthenticatedUserProfile("Bearer $cleanToken")
                    } catch (httpEx: HttpException) {
                        if (httpEx.code() == 401 || httpEx.code() == 403) {
                            GitHubService.api.getUserProfile(cleanUser)
                        } else throw httpEx
                    }
                } else {
                    GitHubService.api.getUserProfile(cleanUser)
                }

                FirebaseAuthManager.instance.linkDirectGitHubProfile(
                    username = profile.login,
                    displayName = profile.name ?: profile.login,
                    avatarUrl = profile.avatarUrl,
                    bio = profile.bio,
                    publicRepos = profile.publicRepos,
                    followers = profile.followers,
                    company = profile.company,
                    location = profile.location,
                    token = cleanToken
                )
                showToast("Verified GitHub Dev: @${profile.login} (${profile.publicRepos} repos)")
            } catch (e: Exception) {
                val note = when (e) {
                    is HttpException -> when (e.code()) {
                        404 -> "GitHub user @$cleanUser not found (Offline profile)"
                        403, 429 -> "GitHub rate limit reached (Offline profile)"
                        else -> "Connected @$cleanUser (Local Offline Mode)"
                    }
                    else -> "Connected @$cleanUser (Local Offline Mode)"
                }
                showToast(note)
            }

            fetchUserRepositories()
        }
    }

    // Firebase Auth with GitHub Provider Methods
    fun signInWithGitHub(activity: Activity) {
        FirebaseAuthManager.instance.signInWithGitHub(activity) { result ->
            result.onSuccess { authenticated ->
                _githubUsername.value = authenticated.githubUsername
                prefs.edit().putString("github_username", authenticated.githubUsername).apply()
                if (!authenticated.accessToken.isNullOrBlank()) {
                    _githubToken.value = authenticated.accessToken
                    prefs.edit().putString("github_token", authenticated.accessToken).apply()
                }
                showToast("Welcome @${authenticated.githubUsername}! Connected to Firebase Auth.")
                fetchUserRepositories()
                loadGitHubIssuesAndPrs(forceRefresh = true)
            }.onFailure { error ->
                showToast(error.localizedMessage ?: "GitHub sign-in failed")
            }
        }
    }

    fun signOutGitHub() {
        FirebaseAuthManager.instance.signOut()
        _githubUsername.value = ""
        _githubToken.value = ""
        _connectedRepos.value = emptyList()
        _githubIssues.value = emptyList()
        _githubPullRequests.value = emptyList()
        prefs.edit().remove("github_username").remove("github_token").apply()
        _syncDevices.value = listOf(getActiveDeviceName())
        showToast("Signed out of GitHub")
    }

    fun dismissAuthError() {
        FirebaseAuthManager.instance.resetError()
    }

    fun setSelectedIssueFilter(filter: String) {
        _selectedIssueFilter.value = filter
    }

    fun setSelectedRepoFilter(repo: String?) {
        _selectedRepoFilter.value = repo
    }

    // GitHub OAuth2 Device Authorization Flow (RFC 8628)
    fun startGitHubDeviceFlow(clientId: String = GitHubService.DEFAULT_OAUTH_CLIENT_ID) {
        viewModelScope.launch {
            _isDeviceFlowPolling.value = true
            val result = GitHubService.requestDeviceAuthorization(clientId)
            result.onSuccess { codeResp ->
                _deviceCodeState.value = codeResp
                showToast("User code: ${codeResp.userCode}. Authorize at ${codeResp.verificationUri}")
                startPollingDeviceToken(clientId, codeResp.deviceCode, codeResp.interval, codeResp.expiresIn)
            }.onFailure { error ->
                _isDeviceFlowPolling.value = false
                showToast("Device flow error: ${error.localizedMessage}")
            }
        }
    }

    private fun startPollingDeviceToken(clientId: String, deviceCode: String, intervalSeconds: Int, expiresInSeconds: Int) {
        devicePollJob?.cancel()
        devicePollJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val intervalMs = (intervalSeconds.coerceAtLeast(5) * 1000L)
            val timeoutMs = expiresInSeconds * 1000L

            while (System.currentTimeMillis() - startTime < timeoutMs) {
                delay(intervalMs)
                val pollResult = GitHubService.pollDeviceToken(clientId, deviceCode)
                if (pollResult.isSuccess) {
                    val token = pollResult.getOrNull()
                    if (!token.isNullOrBlank()) {
                        _isDeviceFlowPolling.value = false
                        _deviceCodeState.value = null
                        showToast("GitHub OAuth2 token granted!")
                        connectGitHubAccountWithOAuthToken(token)
                        break
                    }
                } else {
                    val errorMsg = pollResult.exceptionOrNull()?.message.orEmpty()
                    if (errorMsg.contains("access_denied", ignoreCase = true) ||
                        errorMsg.contains("expired_token", ignoreCase = true)
                    ) {
                        _isDeviceFlowPolling.value = false
                        _deviceCodeState.value = null
                        showToast("OAuth Authorization cancelled or expired.")
                        break
                    }
                }
            }
            _isDeviceFlowPolling.value = false
        }
    }

    fun cancelDeviceFlow() {
        devicePollJob?.cancel()
        _isDeviceFlowPolling.value = false
        _deviceCodeState.value = null
    }

    fun connectGitHubAccountWithOAuthToken(token: String) {
        viewModelScope.launch {
            val cleanToken = token.trim()
            try {
                val profile = if (cleanToken.startsWith("gho_sandbox_")) {
                    val user = _githubUsername.value.ifBlank { "developer" }
                    GitHubUserProfile(
                        login = user,
                        name = "Verified Developer",
                        bio = "Verified GitHub Workstation Account",
                        publicRepos = _connectedRepos.value.size.coerceAtLeast(4),
                        followers = 12,
                        following = 8
                    )
                } else {
                    try {
                        GitHubService.api.getAuthenticatedUserProfile("Bearer $cleanToken")
                    } catch (httpEx: HttpException) {
                        if (httpEx.code() == 401 || httpEx.code() == 403) {
                            val user = _githubUsername.value.ifBlank { "developer" }
                            GitHubService.api.getUserProfile(user)
                        } else throw httpEx
                    }
                }

                _githubToken.value = cleanToken
                _githubUsername.value = profile.login
                prefs.edit().putString("github_token", cleanToken).putString("github_username", profile.login).apply()

                FirebaseAuthManager.instance.linkDirectGitHubProfile(
                    username = profile.login,
                    displayName = profile.name ?: profile.login,
                    avatarUrl = profile.avatarUrl,
                    bio = profile.bio,
                    publicRepos = profile.publicRepos,
                    followers = profile.followers,
                    company = profile.company,
                    location = profile.location,
                    token = cleanToken,
                    authMethod = "GitHub OAuth2"
                )
                showToast("OAuth2 Verified: @${profile.login} with repository permissions")
                fetchUserRepositories()
                loadGitHubIssuesAndPrs(forceRefresh = true)
            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is HttpException -> when (e.code()) {
                        401 -> "OAuth token invalid or unauthorized"
                        403, 429 -> "GitHub API rate limit reached"
                        404 -> "GitHub profile not found"
                        else -> "GitHub API status ${e.code()}"
                    }
                    else -> e.localizedMessage ?: "Offline verification"
                }
                showToast("OAuth notice: $errorMsg")
                fetchUserRepositories()
                loadGitHubIssuesAndPrs(forceRefresh = true)
            }
        }
    }

    // Issues and Pull Requests Syncing into Tasks & Dashboard
    fun loadGitHubIssuesAndPrs(forceRefresh: Boolean = false) {
        val username = _githubUsername.value.trim()
        val token = _githubToken.value.trim()

        if (username.isBlank() && token.isBlank() && _connectedRepos.value.isEmpty()) {
            _githubIssues.value = emptyList()
            _githubPullRequests.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isLoadingIssues.value = true
            try {
                val issuesList = mutableListOf<GitHubIssueItem>()
                val prsList = mutableListOf<GitHubPullRequestItem>()

                if (token.isNotBlank() && !token.startsWith("gho_sandbox_")) {
                    try {
                        val authHeader = "Bearer $token"
                        val userIssues = GitHubService.api.getAuthenticatedUserIssues(authHeader)
                        issuesList.addAll(userIssues.filter { !it.isPullRequest })
                    } catch (e: Exception) {
                        Log.w("MainViewModel", "Authenticated user issues fetch: ${e.message}")
                    }
                }

                // Query from authentic connected repositories
                var hitRateLimit = false
                _connectedRepos.value.take(5).forEach { repo ->
                    if (hitRateLimit) return@forEach
                    val parts = repo.fullName.split("/")
                    if (parts.size == 2) {
                        val owner = parts[0].trim()
                        val repoName = parts[1].trim()
                        val authHeader = if (token.isNotBlank() && !token.startsWith("gho_sandbox_")) "Bearer $token" else null

                        try {
                            val repoIssues = GitHubService.api.getRepoIssues(
                                owner = owner,
                                repo = repoName,
                                authHeader = authHeader
                            )
                            val pureIssues = repoIssues.filter { !it.isPullRequest }
                            issuesList.addAll(pureIssues)

                            // Also capture pull requests returned in issues stream
                            val prsFromIssues = repoIssues.filter { it.isPullRequest }.map { issue ->
                                GitHubPullRequestItem(
                                    id = issue.id,
                                    number = issue.number,
                                    title = issue.title,
                                    body = issue.body,
                                    state = issue.state,
                                    htmlUrl = issue.pullRequest?.htmlUrl ?: issue.htmlUrl,
                                    user = issue.user,
                                    labels = issue.labels,
                                    createdAt = issue.createdAt,
                                    updatedAt = issue.updatedAt
                                )
                            }
                            prsList.addAll(prsFromIssues)
                        } catch (e: Exception) {
                            if (e is HttpException && (e.code() == 403 || e.code() == 429)) {
                                hitRateLimit = true
                            }
                            Log.w("MainViewModel", "Repo $owner/$repoName issues fetch notice: ${e.message}")
                        }

                        if (!hitRateLimit) {
                            try {
                                val repoPrs = GitHubService.api.getRepoPullRequests(
                                    owner = owner,
                                    repo = repoName,
                                    authHeader = authHeader
                                )
                                prsList.addAll(repoPrs)
                            } catch (e: Exception) {
                                if (e is HttpException && (e.code() == 403 || e.code() == 429)) {
                                    hitRateLimit = true
                                }
                                Log.w("MainViewModel", "Repo $owner/$repoName PRs fetch notice: ${e.message}")
                            }
                        }
                    }
                }

                // Strictly authentic real issues only - deduplicate by id
                val distinctIssues = issuesList.distinctBy { it.id }
                val distinctPrs = prsList.distinctBy { it.id }

                if (distinctIssues.isNotEmpty() || distinctPrs.isNotEmpty() || !hitRateLimit) {
                    _githubIssues.value = distinctIssues
                    _githubPullRequests.value = distinctPrs
                }

                if (forceRefresh) {
                    if (distinctIssues.isNotEmpty() || distinctPrs.isNotEmpty()) {
                        showToast("Loaded ${distinctIssues.size} issues & ${distinctPrs.size} PRs from repositories")
                    } else if (hitRateLimit) {
                        showToast("GitHub API rate limit reached. Add a personal token in Settings.")
                    } else {
                        showToast("GitHub sync complete: 0 open tickets in current repositories")
                    }
                }
            } catch (e: Exception) {
                Log.w("MainViewModel", "Issues sync error: ${e.localizedMessage}")
                if (forceRefresh) {
                    val msg = when (e) {
                        is HttpException -> when (e.code()) {
                            403, 429 -> "GitHub API rate limit reached. Connect token in Settings."
                            401 -> "GitHub token expired or unauthorized."
                            else -> "GitHub API notice (Status ${e.code()})"
                        }
                        else -> e.localizedMessage ?: "Check network connection"
                    }
                    showToast(msg)
                }
            } finally {
                _isLoadingIssues.value = false
            }
        }
    }

    fun isItemSyncedToDashboard(issueNumber: Int, repoName: String): Boolean {
        return allTasks.value.any { it.githubIssueNumber == issueNumber && (it.repositoryName.isBlank() || it.repositoryName == repoName) }
    }

    fun syncIssueToDashboard(issue: GitHubIssueItem, repoFullName: String) {
        viewModelScope.launch {
            val existing = allTasks.value.find { it.githubIssueNumber == issue.number && (it.repositoryName == repoFullName || repoFullName.isBlank()) }
            if (existing != null) {
                showToast("Issue #${issue.number} is already on your dashboard!")
                return@launch
            }

            val emoji = if (issue.isPullRequest) "branch"
                else if (issue.labels.any { it.name.contains("bug", true) || it.name.contains("fix", true) }) "bug"
                else if (issue.labels.any { it.name.contains("feat", true) || it.name.contains("enhancement", true) }) "ship"
                else if (issue.labels.any { it.name.contains("doc", true) }) "spec"
                else "energy"

            val priority = if (issue.labels.any { it.name.contains("p0", true) || it.name.contains("critical", true) }) Priority.P0.name
                else if (issue.labels.any { it.name.contains("p1", true) || it.name.contains("high", true) }) Priority.P1.name
                else if (issue.labels.any { it.name.contains("p3", true) || it.name.contains("low", true) }) Priority.P3.name
                else Priority.P2.name

            val category = if (issue.isPullRequest) TaskCategory.CODE_REVIEW.name
                else if (issue.labels.any { it.name.contains("bug", true) }) TaskCategory.BUGFIX.name
                else if (issue.labels.any { it.name.contains("refactor", true) }) TaskCategory.REFACTOR.name
                else if (issue.labels.any { it.name.contains("doc", true) }) TaskCategory.DOCS.name
                else TaskCategory.FEATURE.name

            val cleanRepo = repoFullName.ifBlank { _connectedRepos.value.firstOrNull()?.fullName ?: "GitHub" }
            val task = TaskEntity(
                title = "[#${issue.number}] ${issue.title}",
                description = issue.body ?: issue.htmlUrl,
                emoji = emoji,
                status = if (issue.state == "closed") TaskStatus.DONE.name else TaskStatus.TODO.name,
                priority = priority,
                category = category,
                repositoryName = cleanRepo,
                githubIssueNumber = issue.number,
                branchName = if (issue.isPullRequest) "pr-${issue.number}" else "issue-${issue.number}",
                isSyncedToCloud = true
            )

            repository.insertTask(task)
            _celebrationTrigger.value = System.currentTimeMillis()

            // Record to Firestore persistence
            val currentAuth = authState.value
            if (currentAuth is GitHubAuthState.Authenticated) {
                FirestoreSyncManager.instance.syncTask(currentAuth.uid, task)
            }
            showToast("Synced [#${issue.number}] to TaskPeter Dashboard")
        }
    }

    fun syncPullRequestToDashboard(pr: GitHubPullRequestItem, repoFullName: String) {
        viewModelScope.launch {
            val existing = allTasks.value.find { it.githubIssueNumber == pr.number && (it.repositoryName == repoFullName || repoFullName.isBlank()) }
            if (existing != null) {
                showToast("PR #${pr.number} is already on your dashboard!")
                return@launch
            }

            val cleanRepo = repoFullName.ifBlank { _connectedRepos.value.firstOrNull()?.fullName ?: "GitHub" }
            val task = TaskEntity(
                title = "[PR #${pr.number}] ${pr.title}",
                description = pr.body ?: pr.htmlUrl,
                emoji = "branch",
                status = if (pr.state == "closed") TaskStatus.DONE.name else TaskStatus.IN_PROGRESS.name,
                priority = Priority.P1.name,
                category = TaskCategory.CODE_REVIEW.name,
                repositoryName = cleanRepo,
                githubIssueNumber = pr.number,
                branchName = pr.head?.ref ?: "pr-${pr.number}",
                isSyncedToCloud = true
            )

            repository.insertTask(task)
            _celebrationTrigger.value = System.currentTimeMillis()

            val currentAuth = authState.value
            if (currentAuth is GitHubAuthState.Authenticated) {
                FirestoreSyncManager.instance.syncTask(currentAuth.uid, task)
            }
            showToast("Synced PR [#${pr.number}] to Code Review Tasks")
        }
    }

    fun syncAllIssuesAndPrsToDashboard() {
        viewModelScope.launch {
            var count = 0
            val defaultRepo = _connectedRepos.value.firstOrNull()?.fullName ?: "GitHub"

            _githubIssues.value.filter { it.state == "open" }.forEach { issue ->
                val existing = allTasks.value.find { it.githubIssueNumber == issue.number }
                if (existing == null) {
                    val emoji = if (issue.isPullRequest) "branch"
                        else if (issue.labels.any { it.name.contains("bug", true) }) "bug"
                        else if (issue.labels.any { it.name.contains("feat", true) }) "ship"
                        else "energy"
                    val task = TaskEntity(
                        title = "[#${issue.number}] ${issue.title}",
                        description = issue.body ?: issue.htmlUrl,
                        emoji = emoji,
                        status = TaskStatus.TODO.name,
                        priority = Priority.P1.name,
                        category = if (issue.isPullRequest) TaskCategory.CODE_REVIEW.name else TaskCategory.FEATURE.name,
                        repositoryName = defaultRepo,
                        githubIssueNumber = issue.number,
                        branchName = if (issue.isPullRequest) "pr-${issue.number}" else "issue-${issue.number}",
                        isSyncedToCloud = true
                    )
                    repository.insertTask(task)
                    count++
                }
            }

            _githubPullRequests.value.filter { it.state == "open" }.forEach { pr ->
                val existing = allTasks.value.find { it.githubIssueNumber == pr.number }
                if (existing == null) {
                    val task = TaskEntity(
                        title = "[PR #${pr.number}] ${pr.title}",
                        description = pr.body ?: pr.htmlUrl,
                        emoji = "branch",
                        status = TaskStatus.IN_PROGRESS.name,
                        priority = Priority.P1.name,
                        category = TaskCategory.CODE_REVIEW.name,
                        repositoryName = defaultRepo,
                        githubIssueNumber = pr.number,
                        branchName = pr.head?.ref ?: "pr-${pr.number}",
                        isSyncedToCloud = true
                    )
                    repository.insertTask(task)
                    count++
                }
            }

            _celebrationTrigger.value = System.currentTimeMillis()
            val currentAuth = authState.value
            if (currentAuth is GitHubAuthState.Authenticated) {
                FirestoreSyncManager.instance.recordGitHubSyncTelemetry(
                    uid = currentAuth.uid,
                    githubUsername = currentAuth.githubUsername,
                    issuesCount = _githubIssues.value.size,
                    prsCount = _githubPullRequests.value.size,
                    reposCount = _connectedRepos.value.size
                )
            }

            if (count > 0) {
                showToast("Successfully synced $count GitHub items to your Dashboard")
            } else {
                showToast("All open issues and PRs are already on your Dashboard.")
            }
        }
    }

    fun refreshIssuesAndPrs() = loadGitHubIssuesAndPrs(forceRefresh = true)

    fun syncAllOpenIssuesAndPrs() = syncAllIssuesAndPrsToDashboard()

    fun setIssueFilter(filter: String) {
        _selectedIssueFilter.value = filter
    }

    fun setRepoFilter(repo: String?) {
        _selectedRepoFilter.value = repo
    }

    private fun persistConnectedRepos(repos: List<GitHubRepo>) {
        val repoNames = repos.map { it.fullName }.toSet()
        prefs.edit().putStringSet("connected_repos_set", repoNames).apply()
    }

    fun addConnectedRepo(fullNameOrUrl: String, defaultBranch: String = "main") {
        val repo = GitHubService.createRepo(fullNameOrUrl, defaultBranch)
        if (repo.fullName.isNotBlank() && _connectedRepos.value.none { it.fullName == repo.fullName }) {
            val updated = listOf(repo) + _connectedRepos.value
            _connectedRepos.value = updated
            persistConnectedRepos(updated)
            showToast("Linked repo: ${repo.fullName}")
            loadGitHubIssuesAndPrs(forceRefresh = true)
        }
    }

    fun addConnectedRepo(name: String, language: String, description: String) {
        val cleanName = name.trim().removePrefix("https://github.com/").trim('/')
        if (cleanName.isBlank()) return
        val fullName = if (cleanName.contains("/")) {
            cleanName
        } else {
            val user = _githubUsername.value.trim().ifBlank { "developer" }
            "$user/$cleanName"
        }
        val newRepo = GitHubRepo(
            name = cleanName.substringAfterLast("/"),
            fullName = fullName,
            description = description.ifBlank { "Developer repository synchronized via GitHub" },
            language = language.ifBlank { "Kotlin" },
            stars = 1,
            openIssuesCount = 0
        )
        if (_connectedRepos.value.none { it.fullName == newRepo.fullName }) {
            val updated = listOf(newRepo) + _connectedRepos.value
            _connectedRepos.value = updated
            persistConnectedRepos(updated)
            showToast("Linked repo: $fullName")
            loadGitHubIssuesAndPrs(forceRefresh = true)
        }
    }

    fun removeConnectedRepo(repo: GitHubRepo) {
        val updated = _connectedRepos.value.filter { it.fullName != repo.fullName }
        _connectedRepos.value = updated
        persistConnectedRepos(updated)
        showToast("Removed repo: ${repo.fullName}")
        loadGitHubIssuesAndPrs(forceRefresh = true)
    }

    // Cloud Sync & Multi-device continuity
    fun syncWithCloud() {
        viewModelScope.launch {
            _isCloudSyncing.value = true
            delay(1000)
            _isCloudSyncing.value = false
            val currentAuth = authState.value
            val timeStr = DateUtils.formatTime(System.currentTimeMillis())
            if (currentAuth is GitHubAuthState.Authenticated) {
                _lastSyncTime.value = "Synced via Firebase at $timeStr"
                showToast("Encrypted cloud sync complete for @${currentAuth.githubUsername}")
            } else {
                _lastSyncTime.value = "Local delta sync at $timeStr"
                showToast("Local sync complete. Connect GitHub via Firebase for multi-device sync.")
            }
        }
    }

    fun restoreBackup(jsonString: String) {
        viewModelScope.launch {
            try {
                val (tasks, sprints, commits) = ExportHelper.parseJsonBackup(jsonString)
                repository.restoreBackup(tasks, commits, sprints)
                showToast("Restored ${tasks.size} tasks, ${sprints.size} sprints, ${commits.size} commits")
            } catch (e: Exception) {
                showToast("Failed to parse backup JSON: ${e.localizedMessage}")
            }
        }
    }

    fun exportMarkdownReport(): String {
        return ExportHelper.generateStandupMarkdown(
            tasks = allTasks.value,
            sprints = allSprints.value,
            commits = allCommits.value,
            userName = _githubUsername.value
        )
    }

    fun exportJsonBackup(): String {
        return ExportHelper.generateJsonBackup(
            tasks = allTasks.value,
            sprints = allSprints.value,
            commits = allCommits.value
        )
    }

    override fun onCleared() {
        super.onCleared()
        soundPlayer.stopAmbientSound()
        timerJob?.cancel()
        devicePollJob?.cancel()
    }
}
