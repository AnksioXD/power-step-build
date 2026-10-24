package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyStepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStepDao {
    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    suspend fun getDailyStep(date: String): DailyStepEntity?

    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    fun observeDailyStep(date: String): Flow<DailyStepEntity?>

    @Query("SELECT * FROM daily_steps ORDER BY date DESC LIMIT 14")
    fun observeRecentDays(): Flow<List<DailyStepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dailyStep: DailyStepEntity)

    @Update
    suspend fun update(dailyStep: DailyStepEntity)

    @Query("SELECT SUM(steps) FROM daily_steps")
    suspend fun getTotalLifetimeSteps(): Long?
}
