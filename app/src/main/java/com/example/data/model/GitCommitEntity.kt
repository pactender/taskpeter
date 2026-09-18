package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "git_commits")
data class GitCommitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val commitHash: String,
    val message: String,
    val repo: String,
    val branch: String,
    val timestamp: Long = System.currentTimeMillis(),
    val author: String = "coder@taskpeter.dev",
    val isAutomated: Boolean = true
)
