package com.example.data.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val current: Int,
    val isUnlocked: Boolean,
    val coinReward: Int,
    val iconType: String // "COIN", "STAR", "MUSHROOM", "FLAG", "BOOTS"
)
