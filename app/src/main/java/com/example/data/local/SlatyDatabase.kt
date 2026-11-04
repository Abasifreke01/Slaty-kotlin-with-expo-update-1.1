package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskEntity::class,
        StudentTaskEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SlatyDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun studentTaskDao(): StudentTaskDao

    companion object {
        @Volatile
        private var INSTANCE: SlatyDatabase? = null

        fun getInstance(context: Context): SlatyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SlatyDatabase::class.java,
                    "slaty_tasks.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
