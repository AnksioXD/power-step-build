package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 100, // Starts with a welcome stash of coins!
    val currentWorld: Int = 1,
    val currentStage: Int = 1,
    val totalStepsAllTime: Long = 0L,
    val streakDays: Int = 1,
    val activePowerUp: String? = null, // PowerUpType name or null
    val powerUpExpiresAt: Long = 0L,   // Epoch timestamp ms
    val unlockedInventory: String = "SUPER_MUSHROOM:1,SUPER_STAR:1" // Format: TYPE:qty,TYPE:qty
)
