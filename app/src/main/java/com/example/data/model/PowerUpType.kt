package com.example.data.model

enum class PowerUpType(
    val title: String,
    val subtitle: String,
    val costCoins: Int,
    val durationSeconds: Int,
    val description: String,
    val primaryColorHex: Long
) {
    SUPER_MUSHROOM(
        title = "Super Mushroom",
        subtitle = "Grow Giant & Double Points",
        costCoins = 50,
        durationSeconds = 600, // 10 minutes
        description = "Turns Mario Giant! Doubles all step progress points and milestone coin drops.",
        primaryColorHex = 0xFFE52521
    ),
    FIRE_FLOWER(
        title = "Fire Flower",
        subtitle = "Calorie Burn Boost",
        costCoins = 80,
        durationSeconds = 900, // 15 minutes
        description = "Ignites your metabolism with 1.5x calorie burn tracking and flame sprint trails.",
        primaryColorHex = 0xFFFF7043
    ),
    SUPER_STAR(
        title = "Super Star",
        subtitle = "Invincible Star Rush",
        costCoins = 120,
        durationSeconds = 45, // 45 seconds timed fast walk/sprint
        description = "Triggers the timed Star Rush! Listen to the Mario star music and walk faster to earn 5x bonus coins!",
        primaryColorHex = 0xFFFFD600
    ),
    TANOOKI_LEAF(
        title = "Super Leaf",
        subtitle = "Tail Spin Coin Magnet",
        costCoins = 100,
        durationSeconds = 1200, // 20 minutes
        description = "Gives Mario raccoon ears and a tail! Automatically pulls in extra coins for every 10 steps.",
        primaryColorHex = 0xFF8D6E63
    ),
    ONE_UP(
        title = "1-Up Mushroom",
        subtitle = "Streak Shield & Bonus",
        costCoins = 150,
        durationSeconds = 0, // Instant
        description = "Instantly awards +300 bonus steps and protects your walking streak from resetting!",
        primaryColorHex = 0xFF43A047
    )
}
