package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.GitCommitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GitCommitDao {
    @Query("SELECT * FROM git_commits ORDER BY timestamp DESC")
    fun getAllCommits(): Flow<List<GitCommitEntity>>

    @Query("SELECT * FROM git_commits ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCommits(limit: Int): Flow<List<GitCommitEntity>>

    @Query("SELECT * FROM git_commits WHERE taskId = :taskId ORDER BY timestamp DESC")
    fun getCommitsForTask(taskId: Long): Flow<List<GitCommitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommit(commit: GitCommitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommits(commits: List<GitCommitEntity>)

    @Query("DELETE FROM git_commits")
    suspend fun clearAllCommits()
}
