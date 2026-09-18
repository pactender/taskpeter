package com.example.data.db

import com.example.data.model.GitCommitEntity
import com.example.data.model.SprintSessionEntity
import com.example.data.model.TaskEntity

/**
 * Local-only sync manager.
 *
 * This build of TaskPeter carries no proprietary cloud dependencies, so all
 * cloud-sync operations are no-ops: the Room database on the device is the
 * single source of truth. The public API is kept identical to the cloud
 * variant so callers compile unchanged.
 */
class FirestoreSyncManager private constructor() {

    companion object {
        val instance: FirestoreSyncManager by lazy { FirestoreSyncManager() }
    }

    suspend fun syncTask(uid: String, task: TaskEntity): Boolean {
        return false
    }

    suspend fun deleteTask(uid: String, taskId: Long): Boolean {
        return false
    }

    suspend fun syncBatchTasks(uid: String, tasks: List<TaskEntity>): Int {
        return 0
    }

    suspend fun recordGitHubSyncTelemetry(
        uid: String,
        githubUsername: String,
        issuesCount: Int,
        prsCount: Int,
        reposCount: Int
    ) {
        // No-op: cloud sync is disabled in this build.
    }

    suspend fun syncUserSubscriptionAndQuota(
        uid: String,
        email: String,
        displayName: String,
        tier: String,
        queriesUsedToday: Int,
        dailyQuota: Int
    ) {
        // No-op: cloud sync is disabled in this build.
    }

    suspend fun recordChatInteraction(
        uid: String,
        userPrompt: String,
        geminiModel: String
    ) {
        // No-op: cloud sync is disabled in this build.
    }
}
