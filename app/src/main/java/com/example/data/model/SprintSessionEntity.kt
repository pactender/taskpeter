package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sprint_sessions")
data class SprintSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val durationMinutes: Int,
    val completedTimestamp: Long = System.currentTimeMillis(),
    val type: String = SprintType.DEEP_FOCUS.name,
    val notes: String = "",
    val soundProfileUsed: String = "Cyber Hum"
)
