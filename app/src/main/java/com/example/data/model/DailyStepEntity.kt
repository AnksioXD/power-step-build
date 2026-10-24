package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_steps")
data class DailyStepEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val steps: Int = 0,
    val goal: Int = 6000,
    val distanceKm: Float = 0f,
    val calories: Int = 0,
    val activeMinutes: Int = 0,
    val coinsEarned: Int = 0,
    val starRushesCompleted: Int = 0
)
