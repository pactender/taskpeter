package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val emoji: String = "energy",
    val status: String = TaskStatus.TODO.name,
    val priority: String = Priority.P1.name,
    val category: String = TaskCategory.FEATURE.name,
    val repositoryName: String = "",
    val githubIssueNumber: Int? = null,
    val branchName: String = "main",
    val commitHash: String? = null,
    val commitMessage: String? = null,
    val estimatedMinutes: Int = 30,
    val spentMinutes: Int = 0,
    val dueDateMillis: Long? = null,
    val reminderTimeMillis: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val completedAtMillis: Long? = null,
    val isSyncedToCloud: Boolean = true
)
