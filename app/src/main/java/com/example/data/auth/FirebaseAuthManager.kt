package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface GitHubAuthState {
    data object Initializing : GitHubAuthState
    data object Unauthenticated : GitHubAuthState
    data class Authenticating(val step: String = "Launching GitHub authentication...") : GitHubAuthState
    data class Authenticated(
        val uid: String,
        val displayName: String?,
        val email: String?,
        val photoUrl: String?,
        val githubUsername: String,
        val providerId: String = "github.com",
        val lastSignInTimestamp: Long = System.currentTimeMillis(),
        val bio: String? = null,
        val publicReposCount: Int = 0,
        val followersCount: Int = 0,
        val company: String? = null,
        val location: String? = null,
        val isVerified: Boolean = true,
        val accessToken: String? = null,
        val authMethod: String = "OAuth2"
    ) : GitHubAuthState
    data class AuthError(
        val error: String,
        val isConfigurationError: Boolean = false
    ) : GitHubAuthState
}

/**
 * Local-only authentication manager.
 *
 * This build of TaskPeter carries no proprietary cloud dependencies: Google
 * sign-in and the cloud profile are preserved as a purely local identity
 * stored in shared preferences, and the GitHub OAuth-provider flow (which
 * required proprietary Google Play services) is disabled in favor of
 * Personal Access Token / local profile linking.
 */
class FirebaseAuthManager private constructor() {

    private val _authState = MutableStateFlow<GitHubAuthState>(GitHubAuthState.Initializing)
    val authState: StateFlow<GitHubAuthState> = _authState.asStateFlow()

    private var isInitialized = false
    private var appContext: Context? = null

    companion object {
        private const val TAG = "FirebaseAuthManager"
        val instance: FirebaseAuthManager by lazy { FirebaseAuthManager() }
    }

    /**
     * Initializes the local identity: restores the saved profile from
     * shared preferences, or reports unauthenticated.
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        appContext = context.applicationContext

        try {
            val prefs = context.getSharedPreferences("taskpeter_auth_prefs", Context.MODE_PRIVATE)
            val isSignedOutExplicitly = prefs.getBoolean("is_signed_out_explicitly", false)
            val savedEmail = prefs.getString("saved_email", null)
            if (!isSignedOutExplicitly && savedEmail != null) {
                val savedName = prefs.getString("saved_name", "Developer") ?: "Developer"
                val savedUsername = prefs.getString("saved_gh_user", savedEmail.substringBefore("@"))
                    ?: savedEmail.substringBefore("@")
                _authState.value = GitHubAuthState.Authenticated(
                    uid = "local-${savedEmail.replace("@", "_").replace(".", "_")}",
                    displayName = savedName,
                    email = savedEmail,
                    photoUrl = "https://github.com/$savedUsername.png",
                    githubUsername = savedUsername,
                    providerId = "local",
                    isVerified = true,
                    authMethod = "Local Profile"
                )
            } else {
                _authState.value = GitHubAuthState.Unauthenticated
            }
            isInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Initialization failed: ${e.message}", e)
            _authState.value = GitHubAuthState.Unauthenticated
        }
    }

    /**
     * No pending authentication exists in the local-only build.
     */
    fun checkPendingAuth(onComplete: ((Result<GitHubAuthState.Authenticated>) -> Unit)? = null) {
        // No-op: OAuth provider flows are not available without Google Play services.
    }

    /**
     * GitHub OAuth via the proprietary provider is unavailable in this build.
     * The UI falls back to Personal Access Token / local profile linking.
     */
    fun signInWithGitHub(
        activity: Activity,
        onComplete: ((Result<GitHubAuthState.Authenticated>) -> Unit)? = null
    ) {
        val message = "Cloud sign-in is not available in this build. " +
            "Use a Personal Access Token or a local profile instead."
        _authState.value = GitHubAuthState.AuthError(message, isConfigurationError = true)
        onComplete?.invoke(Result.failure(UnsupportedOperationException(message)))
    }

    /**
     * Signs in a purely local profile (no cloud service involved).
     */
    fun signInWithGoogleAccount(
        context: Context,
        email: String,
        displayName: String,
        photoUrl: String? = null,
        onComplete: ((Result<GitHubAuthState.Authenticated>) -> Unit)? = null
    ) {
        try {
            _authState.value = GitHubAuthState.Authenticating("Creating local profile...")

            val cleanEmail = email.trim().ifBlank { "developer@gmail.com" }
            val cleanName = displayName.trim().ifBlank { cleanEmail.substringBefore("@") }
            val safeUid = "local-${cleanEmail.replace("@", "_").replace(".", "_")}"

            val authenticated = GitHubAuthState.Authenticated(
                uid = safeUid,
                displayName = cleanName,
                email = cleanEmail,
                photoUrl = photoUrl ?: "https://github.com/${cleanEmail.substringBefore("@")}.png",
                githubUsername = cleanEmail.substringBefore("@"),
                providerId = "local",
                isVerified = true,
                authMethod = "Local Profile"
            )
            val prefs = context.applicationContext.getSharedPreferences("taskpeter_auth_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("is_signed_out_explicitly", false)
                .putString("saved_email", cleanEmail)
                .putString("saved_name", cleanName)
                .putString("saved_gh_user", cleanEmail.substringBefore("@"))
                .apply()

            _authState.value = authenticated
            onComplete?.invoke(Result.success(authenticated))
        } catch (e: Exception) {
            val errorState = GitHubAuthState.AuthError(e.localizedMessage ?: "Local profile creation failed")
            _authState.value = errorState
            onComplete?.invoke(Result.failure(e))
        }
    }

    /**
     * Signs out and clears the local profile.
     */
    fun signOut() {
        try {
            appContext?.let { ctx ->
                val prefs = ctx.getSharedPreferences("taskpeter_auth_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("is_signed_out_explicitly", true).apply()
            }
            _authState.value = GitHubAuthState.Unauthenticated
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error: ${e.message}", e)
            _authState.value = GitHubAuthState.Unauthenticated
        }
    }

    /**
     * Dismisses the error state and resets back to Unauthenticated.
     */
    fun resetError() {
        if (_authState.value is GitHubAuthState.AuthError) {
            _authState.value = GitHubAuthState.Unauthenticated
        }
    }

    /**
     * Allows linking an authentic GitHub account profile or Personal Access Token directly.
     */
    fun linkDirectGitHubProfile(
        username: String,
        displayName: String? = null,
        email: String? = null,
        avatarUrl: String? = null,
        bio: String? = null,
        publicRepos: Int = 0,
        followers: Int = 0,
        company: String? = null,
        location: String? = null,
        token: String = "",
        authMethod: String = if (token.isNotBlank()) "OAuth2 Token / PAT" else "Local Profile"
    ) {
        val cleanUser = username.trim().removePrefix("@")
        if (cleanUser.isNotBlank()) {
            _authState.value = GitHubAuthState.Authenticated(
                uid = "gh-$cleanUser-${System.currentTimeMillis()}",
                displayName = displayName?.takeIf { it.isNotBlank() } ?: cleanUser,
                email = email?.takeIf { it.isNotBlank() } ?: "$cleanUser@users.noreply.github.com",
                photoUrl = avatarUrl?.takeIf { it.isNotBlank() } ?: "https://github.com/$cleanUser.png",
                githubUsername = cleanUser,
                providerId = "github.com",
                bio = bio,
                publicReposCount = publicRepos,
                followersCount = followers,
                company = company,
                location = location,
                isVerified = true,
                accessToken = token.takeIf { it.isNotBlank() },
                authMethod = authMethod
            )
            appContext?.let { ctx ->
                val prefs = ctx.getSharedPreferences("taskpeter_auth_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("is_signed_out_explicitly", false)
                    .putString("saved_gh_user", cleanUser)
                    .putString("saved_name", displayName?.takeIf { it.isNotBlank() } ?: cleanUser)
                    .apply()
            }
        }
    }
}
