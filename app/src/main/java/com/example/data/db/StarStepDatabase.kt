package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DailyStepEntity
import com.example.data.model.UserProgressEntity

@Database(
    entities = [DailyStepEntity::class, UserProgressEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StarStepDatabase : RoomDatabase() {
    abstract fun dailyStepDao(): DailyStepDao
    abstract fun userProgressDao(): UserProgressDao

    companion object {
        @Volatile
        private var INSTANCE: StarStepDatabase? = null

        fun getDatabase(context: Context): StarStepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StarStepDatabase::class.java,
                    "starstep_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
