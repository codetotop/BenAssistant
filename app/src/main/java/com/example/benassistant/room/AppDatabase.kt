package com.example.benassistant.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ChatLog::class],
    version = 2, // Increased version to 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chatLogDao(): ChatLogDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the isNew column to the chat_log table
                database.execSQL("ALTER TABLE chat_log ADD COLUMN isNew INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ben_database"
                )
                    .addMigrations(MIGRATION_1_2) // Register migration
                    .build().also { INSTANCE = it }
            }
        }
    }
}
