package com.example.data.repository

import com.example.data.db.GitCommitDao
import com.example.data.db.SprintDao
import com.example.data.db.TaskDao
import com.example.data.model.GitCommitEntity
import com.example.data.model.SprintSessionEntity
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class TaskPeterRepository(
    private val taskDao: TaskDao,
    private val sprintDao: SprintDao,
    private val gitCommitDao: GitCommitDao
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allSprints: Flow<List<SprintSessionEntity>> = sprintDao.getAllSprints()
    val allCommits: Flow<List<GitCommitEntity>> = gitCommitDao.getAllCommits()
    val completedCount: Flow<Int> = taskDao.getCompletedTasksCount()
    val totalCount: Flow<Int> = taskDao.getTotalTasksCount()

    fun searchTasks(query: String): Flow<List<TaskEntity>> = taskDao.searchTasks(query)

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun updateTaskStatus(task: TaskEntity, newStatus: TaskStatus): TaskEntity {
        val now = System.currentTimeMillis()
        val isCompleted = newStatus == TaskStatus.DONE

        val updated = task.copy(
            status = newStatus.name,
            completedAtMillis = if (isCompleted) (task.completedAtMillis ?: now) else null,
            isSyncedToCloud = true
        )
        taskDao.updateTask(updated)
        return updated
    }

    suspend fun recordSprintSession(sprint: SprintSessionEntity, linkedTask: TaskEntity? = null): Long {
        val sprintId = sprintDao.insertSprint(sprint)
        if (linkedTask != null) {
            val updatedTask = linkedTask.copy(
                spentMinutes = linkedTask.spentMinutes + sprint.durationMinutes
            )
            taskDao.updateTask(updatedTask)
        }
        return sprintId
    }

    suspend fun logGitCommit(
        task: TaskEntity?,
        message: String,
        repo: String,
        branch: String
    ): GitCommitEntity {
        val commitSeed = "$repo:$branch:$message:${System.currentTimeMillis()}"
        val hash = try {
            val md = java.security.MessageDigest.getInstance("SHA-1")
            val bytes = md.digest(commitSeed.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }.take(7)
        } catch (_: Exception) {
            val fallback = (task?.id?.toString() ?: "c") + (System.currentTimeMillis() % 1000000).toString(16)
            fallback.take(7)
        }

        val commit = GitCommitEntity(
            taskId = task?.id,
            commitHash = hash,
            message = message,
            repo = repo,
            branch = branch,
            timestamp = System.currentTimeMillis()
        )
        gitCommitDao.insertCommit(commit)

        task?.let {
            val updated = it.copy(
                commitHash = hash,
                commitMessage = message,
                repositoryName = repo,
                branchName = branch
            )
            taskDao.updateTask(updated)
        }

        return commit
    }

    suspend fun restoreBackup(tasks: List<TaskEntity>, commits: List<GitCommitEntity>, sprints: List<SprintSessionEntity>) {
        taskDao.insertTasks(tasks)
        gitCommitDao.insertCommits(commits)
        sprintDao.insertSprints(sprints)
    }

    suspend fun clearAll() {
        taskDao.clearAllTasks()
        gitCommitDao.clearAllCommits()
        sprintDao.clearAllSprints()
    }

    suspend fun prepopulateStarterWorkspaceIfEmpty() {
        val count = taskDao.getTaskCount()
        if (count > 0) return

        val now = System.currentTimeMillis()
        val dayMillis = 86400000L

        val task1 = TaskEntity(
            title = "Implement OAuth2 GitHub Device Flow",
            description = "Complete RFC 8628 Device Authorization flow for frictionless terminal authentication on mobile devices.",
            priority = "P1",
            category = "FEATURE",
            status = "DONE",
            spentMinutes = 45,
            repositoryName = "taskpeter-android",
            branchName = "feat/oauth2-flow",
            emoji = "branch",
            commitHash = "a7c29e1",
            commitMessage = "feat(auth): complete rfc 8628 device flow implementation",
            completedAtMillis = now - (2 * 3600000L),
            createdAtMillis = now - (24 * 3600000L)
        )

        val task2 = TaskEntity(
            title = "Optimize Room SQLite indexes on task query filters",
            description = "Add compound indices on status and repositoryName for sub-10ms backlog filtering.",
            priority = "P0",
            category = "DEVOPS",
            status = "IN_PROGRESS",
            spentMinutes = 30,
            repositoryName = "taskpeter-android",
            branchName = "perf/db-indexing",
            emoji = "energy",
            dueDateMillis = now + dayMillis,
            createdAtMillis = now - (12 * 3600000L)
        )

        val task3 = TaskEntity(
            title = "Refactor sprint timer state to unidirectional Flow",
            description = "Migrate countdown timer ticks to cold stateFlow with lifecycle-aware emission to prevent battery drain.",
            priority = "P2",
            category = "REFACTOR",
            status = "TODO",
            spentMinutes = 0,
            repositoryName = "taskpeter-android",
            branchName = "refactor/sprint-flow",
            emoji = "time",
            dueDateMillis = now + (2 * dayMillis),
            createdAtMillis = now - (6 * 3600000L)
        )

        val task4 = TaskEntity(
            title = "Audit zero-leakage local storage encryption",
            description = "Verify all sensitive tokens and auth payloads remain strictly on-device in secure storage.",
            priority = "P1",
            category = "DEVOPS",
            status = "TODO",
            spentMinutes = 0,
            repositoryName = "taskpeter-android",
            branchName = "security/local-audit",
            emoji = "shield",
            dueDateMillis = now + (3 * dayMillis),
            createdAtMillis = now - (2 * 3600000L)
        )

        val id1 = taskDao.insertTask(task1)
        taskDao.insertTask(task2)
        taskDao.insertTask(task3)
        taskDao.insertTask(task4)

        if (sprintDao.getSprintCount() == 0) {
            sprintDao.insertSprint(
                SprintSessionEntity(
                    taskId = id1,
                    taskTitle = "Implement OAuth2 GitHub Device Flow",
                    durationMinutes = 45,
                    completedTimestamp = now - (2 * 3600000L),
                    notes = "Finished RFC 8628 verification endpoint and response parser."
                )
            )
        }

        gitCommitDao.insertCommit(
            GitCommitEntity(
                taskId = id1,
                commitHash = "a7c29e1",
                message = "feat(auth): complete rfc 8628 device flow implementation",
                repo = "taskpeter-android",
                branch = "feat/oauth2-flow",
                timestamp = now - (2 * 3600000L)
            )
        )
    }
}
