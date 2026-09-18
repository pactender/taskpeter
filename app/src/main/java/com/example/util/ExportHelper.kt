package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.GitCommitEntity
import com.example.data.model.SprintSessionEntity
import com.example.data.model.TaskEntity
import org.json.JSONArray
import org.json.JSONObject

object ExportHelper {

    fun generateStandupMarkdown(
        tasks: List<TaskEntity>,
        sprints: List<SprintSessionEntity>,
        commits: List<GitCommitEntity>,
        userName: String = "Developer"
    ): String {
        val completedToday = tasks.filter { it.status == "DONE" }
        val inProgress = tasks.filter { it.status == "IN_PROGRESS" }
        val blockers = tasks.filter { it.priority == "P0" && it.status != "DONE" }
        val totalFocusMins = sprints.sumOf { it.durationMinutes }

        val sb = StringBuilder()
        sb.appendLine("## [STANDUP REPORT] TaskPeter Daily Standup")
        sb.appendLine("**Developer:** $userName | **Date:** ${DateUtils.formatIsoDate(System.currentTimeMillis())}")
        sb.appendLine("**Deep Focus Time:** ${totalFocusMins}m | **Commits Logged:** ${commits.size}")
        sb.appendLine()

        sb.appendLine("### [COMPLETED] Tasks (${completedToday.size})")
        if (completedToday.isEmpty()) {
            sb.appendLine("- *No tasks marked completed today yet.*")
        } else {
            completedToday.forEach {
                val issue = if (it.githubIssueNumber != null) " (#${it.githubIssueNumber})" else ""
                val hash = if (!it.commitHash.isNullOrBlank()) " [`${it.commitHash}`]" else ""
                sb.appendLine("- [x] **${it.title}**$issue$hash [${it.category}]")
            }
        }
        sb.appendLine()

        sb.appendLine("### [IN PROGRESS] Active Tasks (${inProgress.size})")
        if (inProgress.isEmpty()) {
            sb.appendLine("- *No active tasks in progress.*")
        } else {
            inProgress.forEach {
                sb.appendLine("- [ ] **${it.title}** on branch `${it.branchName}` (${it.spentMinutes}/${it.estimatedMinutes}m)")
            }
        }
        sb.appendLine()

        if (blockers.isNotEmpty()) {
            sb.appendLine("### [BLOCKERS] Critical Priority P0 (${blockers.size})")
            blockers.forEach {
                sb.appendLine("- **[CRITICAL]** **${it.title}** (Repo: `${it.repositoryName}`)")
            }
            sb.appendLine()
        }

        sb.appendLine("### [ACTIVITY] Recent Commit Stream")
        if (commits.isEmpty()) {
            sb.appendLine("- *No automated commits logged.*")
        } else {
            commits.take(5).forEach {
                sb.appendLine("- `${it.commitHash}`: ${it.message} (${it.repo})")
            }
        }
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine("*Generated automatically by TaskPeter - Dev Task & Deep Focus Suite*")

        return sb.toString()
    }

    fun generateJsonBackup(
        tasks: List<TaskEntity>,
        sprints: List<SprintSessionEntity>,
        commits: List<GitCommitEntity>
    ): String {
        val root = JSONObject()
        root.put("app", "TaskPeter")
        root.put("version", "1.0")
        root.put("backupTimestamp", System.currentTimeMillis())

        val tasksArray = JSONArray()
        tasks.forEach { t ->
            val obj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("description", t.description)
                put("status", t.status)
                put("priority", t.priority)
                put("category", t.category)
                put("repositoryName", t.repositoryName)
                put("githubIssueNumber", t.githubIssueNumber ?: JSONObject.NULL)
                put("branchName", t.branchName)
                put("commitHash", t.commitHash ?: JSONObject.NULL)
                put("commitMessage", t.commitMessage ?: JSONObject.NULL)
                put("estimatedMinutes", t.estimatedMinutes)
                put("spentMinutes", t.spentMinutes)
                put("dueDateMillis", t.dueDateMillis ?: JSONObject.NULL)
                put("createdAtMillis", t.createdAtMillis)
                put("completedAtMillis", t.completedAtMillis ?: JSONObject.NULL)
            }
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        val sprintsArray = JSONArray()
        sprints.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("taskTitle", s.taskTitle ?: JSONObject.NULL)
                put("durationMinutes", s.durationMinutes)
                put("completedTimestamp", s.completedTimestamp)
                put("type", s.type)
                put("notes", s.notes)
            }
            sprintsArray.put(obj)
        }
        root.put("sprints", sprintsArray)

        val commitsArray = JSONArray()
        commits.forEach { c ->
            val obj = JSONObject().apply {
                put("id", c.id)
                put("commitHash", c.commitHash)
                put("message", c.message)
                put("repo", c.repo)
                put("branch", c.branch)
                put("timestamp", c.timestamp)
            }
            commitsArray.put(obj)
        }
        root.put("commits", commitsArray)

        return root.toString(2)
    }

    fun parseJsonBackup(jsonString: String): Triple<List<TaskEntity>, List<SprintSessionEntity>, List<GitCommitEntity>> {
        val root = JSONObject(jsonString)
        val taskList = mutableListOf<TaskEntity>()
        val sprintList = mutableListOf<SprintSessionEntity>()
        val commitList = mutableListOf<GitCommitEntity>()

        if (root.has("tasks")) {
            val arr = root.getJSONArray("tasks")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                taskList.add(
                    TaskEntity(
                        title = obj.getString("title"),
                        description = obj.optString("description", ""),
                        status = obj.optString("status", "TODO"),
                        priority = obj.optString("priority", "P1"),
                        category = obj.optString("category", "FEATURE"),
                        repositoryName = obj.optString("repositoryName", "taskpeter/core"),
                        githubIssueNumber = if (obj.isNull("githubIssueNumber")) null else obj.getInt("githubIssueNumber"),
                        branchName = obj.optString("branchName", "main"),
                        commitHash = if (obj.isNull("commitHash")) null else obj.getString("commitHash"),
                        commitMessage = if (obj.isNull("commitMessage")) null else obj.getString("commitMessage"),
                        estimatedMinutes = obj.optInt("estimatedMinutes", 30),
                        spentMinutes = obj.optInt("spentMinutes", 0),
                        dueDateMillis = if (obj.isNull("dueDateMillis")) null else obj.getLong("dueDateMillis"),
                        createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                        completedAtMillis = if (obj.isNull("completedAtMillis")) null else obj.getLong("completedAtMillis")
                    )
                )
            }
        }

        if (root.has("sprints")) {
            val arr = root.getJSONArray("sprints")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                sprintList.add(
                    SprintSessionEntity(
                        taskTitle = if (obj.isNull("taskTitle")) null else obj.getString("taskTitle"),
                        durationMinutes = obj.optInt("durationMinutes", 25),
                        completedTimestamp = obj.optLong("completedTimestamp", System.currentTimeMillis()),
                        type = obj.optString("type", "DEEP_FOCUS"),
                        notes = obj.optString("notes", "")
                    )
                )
            }
        }

        if (root.has("commits")) {
            val arr = root.getJSONArray("commits")
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                commitList.add(
                    GitCommitEntity(
                        commitHash = obj.getString("commitHash"),
                        message = obj.getString("message"),
                        repo = obj.optString("repo", "taskpeter/core"),
                        branch = obj.optString("branch", "main"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        return Triple(taskList, sprintList, commitList)
    }

    fun shareText(context: Context, text: String, subject: String = "TaskPeter Developer Report") {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            val chooser = Intent.createChooser(intent, "Share with Team / Standup")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Exception) {
            // Share target resolution failed or no handler on device
        }
    }
}
