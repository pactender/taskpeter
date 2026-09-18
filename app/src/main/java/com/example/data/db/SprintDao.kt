package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SprintSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SprintDao {
    @Query("SELECT * FROM sprint_sessions ORDER BY completedTimestamp DESC")
    fun getAllSprints(): Flow<List<SprintSessionEntity>>

    @Query("SELECT * FROM sprint_sessions ORDER BY completedTimestamp DESC LIMIT :limit")
    fun getRecentSprints(limit: Int): Flow<List<SprintSessionEntity>>

    @Query("SELECT * FROM sprint_sessions WHERE completedTimestamp >= :startMillis AND completedTimestamp <= :endMillis")
    fun getSprintsInRange(startMillis: Long, endMillis: Long): Flow<List<SprintSessionEntity>>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM sprint_sessions WHERE completedTimestamp >= :startMillis")
    fun getFocusMinutesSince(startMillis: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM sprint_sessions")
    suspend fun getSprintCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSprint(sprint: SprintSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSprints(sprints: List<SprintSessionEntity>)

    @Query("DELETE FROM sprint_sessions WHERE id = :id")
    suspend fun deleteSprintById(id: Long)

    @Query("DELETE FROM sprint_sessions")
    suspend fun clearAllSprints()
}
