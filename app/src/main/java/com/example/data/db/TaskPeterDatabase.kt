package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.GitCommitEntity
import com.example.data.model.SprintSessionEntity
import com.example.data.model.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [TaskEntity::class, SprintSessionEntity::class, GitCommitEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TaskPeterDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun sprintDao(): SprintDao
    abstract fun gitCommitDao(): GitCommitDao

    companion object {
        @Volatile
        private var INSTANCE: TaskPeterDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): TaskPeterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskPeterDatabase::class.java,
                    "taskpeter_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
