package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.RetroSoundManager
import com.example.data.db.StarStepDatabase
import com.example.data.model.Achievement
import com.example.data.model.DailyStepEntity
import com.example.data.model.PowerUpType
import com.example.data.model.UserProgressEntity
import com.example.sensor.SimulationSpeed
import com.example.sensor.StepSensorTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class StarIntervalFrequency(val label: String, val minSteps: Int, val maxSteps: Int) {
    FREQUENT("Frequent (~150-250 steps)", 150, 250),
    BALANCED("Balanced (~300-450 steps)", 300, 450),
    ENDURANCE("Endurance (~600-800 steps)", 600, 800)
}

data class AutoDropEvent(
    val id: Long = System.currentTimeMillis() + Random.nextLong(1000),
    val title: String,
    val description: String,
    val timeFormatted: String,
    val iconType: String // "COIN", "MUSHROOM", "FIRE", "ONE_UP", "STAR"
)

data class StarRushResult(
    val stepsTaken: Int,
    val averageSpm: Int,
    val bonusCoins: Int,
    val targetMetPercent: Int
)

class StarStepViewModel(application: Application) : AndroidViewModel(application) {

    private val db = StarStepDatabase.getDatabase(application)
    private val dailyStepDao = db.dailyStepDao()
    private val userProgressDao = db.userProgressDao()

    val soundManager = RetroSoundManager(application)

    private val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Step Sensor & Cadence
    lateinit var sensorTracker: StepSensorTracker
        private set

    // Primary State
    private val _stepsToday = MutableStateFlow(0)
    val stepsToday: StateFlow<Int> = _stepsToday.asStateFlow()

    private val _dailyGoal = MutableStateFlow(6000)
    val dailyGoal: StateFlow<Int> = _dailyGoal.asStateFlow()

    private val _coins = MutableStateFlow(100)
    val coins: StateFlow<Int> = _coins.asStateFlow()

    private val _currentCadence = MutableStateFlow(0)
    val currentCadence: StateFlow<Int> = _currentCadence.asStateFlow()

    // Active Buffs (All auto-equipped upon discovery)
    private val _activePowerUp = MutableStateFlow<PowerUpType?>(null)
    val activePowerUp: StateFlow<PowerUpType?> = _activePowerUp.asStateFlow()

    private val _powerUpRemainingSeconds = MutableStateFlow(0)
    val powerUpRemainingSeconds: StateFlow<Int> = _powerUpRemainingSeconds.asStateFlow()

    // Autonomous Starman Interval State
    private val _starFrequency = MutableStateFlow(StarIntervalFrequency.FREQUENT)
    val starFrequency: StateFlow<StarIntervalFrequency> = _starFrequency.asStateFlow()

    private val _starSprintDuration = MutableStateFlow(40) // 40 seconds sprint
    val starSprintDuration: StateFlow<Int> = _starSprintDuration.asStateFlow()

    private val _stepsUntilNextStar = MutableStateFlow(180)
    val stepsUntilNextStar: StateFlow<Int> = _stepsUntilNextStar.asStateFlow()

    private var stepsWalkedInCurrentStarInterval = 0
    private var currentTargetStarInterval = 200

    private var stepsSinceLastAutoMystery = 0
    private var nextAutoMysterySteps = 160

    private val _totalStarRushesToday = MutableStateFlow(0)
    val totalStarRushesToday: StateFlow<Int> = _totalStarRushesToday.asStateFlow()

    // Star Rush Active Workout State
    private val _isStarRushActive = MutableStateFlow(false)
    val isStarRushActive: StateFlow<Boolean> = _isStarRushActive.asStateFlow()

    private val _starRushSecondsLeft = MutableStateFlow(0)
    val starRushSecondsLeft: StateFlow<Int> = _starRushSecondsLeft.asStateFlow()

    private val _starRushTargetSpm = MutableStateFlow(130)
    val starRushTargetSpm: StateFlow<Int> = _starRushTargetSpm.asStateFlow()

    private val _starRushSteps = MutableStateFlow(0)
    val starRushSteps: StateFlow<Int> = _starRushSteps.asStateFlow()

    private val _starRushCoins = MutableStateFlow(0)
    val starRushCoins: StateFlow<Int> = _starRushCoins.asStateFlow()

    private val _starRushCompletionNotice = MutableStateFlow<StarRushResult?>(null)
    val starRushCompletionNotice: StateFlow<StarRushResult?> = _starRushCompletionNotice.asStateFlow()

    // World & Stage Progress
    private val _currentWorld = MutableStateFlow(1)
    val currentWorld: StateFlow<Int> = _currentWorld.asStateFlow()

    private val _currentStage = MutableStateFlow(1)
    val currentStage: StateFlow<Int> = _currentStage.asStateFlow()

    private val _streakDays = MutableStateFlow(1)
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    // Mystery Block state (animates autonomously)
    private val _mysteryBlockBounced = MutableStateFlow(false)
    val mysteryBlockBounced: StateFlow<Boolean> = _mysteryBlockBounced.asStateFlow()

    // Autonomous Drops Discovery Log
    private val _autoDropsLog = MutableStateFlow<List<AutoDropEvent>>(emptyList())
    val autoDropsLog: StateFlow<List<AutoDropEvent>> = _autoDropsLog.asStateFlow()

    // History and Achievements
    private val _recentDays = MutableStateFlow<List<DailyStepEntity>>(emptyList())
    val recentDays: StateFlow<List<DailyStepEntity>> = _recentDays.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    // Internal timers & jobs
    private var starRushTimerJob: Job? = null
    private var powerUpTimerJob: Job? = null
    private var completionAutoDismissJob: Job? = null
    private var stepsSinceLastCoin = 0
    private var starRushCadenceSamples = mutableListOf<Int>()

    init {
        initSensorTracker()
        rollNextStarThreshold()
        loadPersistedData()
        startPowerUpWatchdog()
    }

    private fun rollNextStarThreshold() {
        val freq = _starFrequency.value
        currentTargetStarInterval = Random.nextInt(freq.minSteps, freq.maxSteps)
        stepsWalkedInCurrentStarInterval = 0
        _stepsUntilNextStar.value = currentTargetStarInterval
    }

    fun setStarIntervalFrequency(freq: StarIntervalFrequency) {
        _starFrequency.value = freq
        rollNextStarThreshold()
    }

    fun setStarSprintDuration(seconds: Int) {
        _starSprintDuration.value = seconds.coerceIn(20, 90)
    }

    private fun initSensorTracker() {
        sensorTracker = StepSensorTracker(getApplication()) { delta, spm ->
            onStepEvent(delta, spm)
        }
        sensorTracker.startListening()
    }

    private fun loadPersistedData() {
        viewModelScope.launch {
            val today = dailyStepDao.getDailyStep(todayDateString)
            if (today != null) {
                _stepsToday.value = today.steps
                _dailyGoal.value = today.goal
                _totalStarRushesToday.value = today.starRushesCompleted
            } else {
                dailyStepDao.insertOrUpdate(
                    DailyStepEntity(date = todayDateString, goal = _dailyGoal.value)
                )
            }

            val progress = userProgressDao.getProgress()
            if (progress != null) {
                _coins.value = progress.coins
                _currentWorld.value = progress.currentWorld
                _currentStage.value = progress.currentStage
                _streakDays.value = progress.streakDays

                if (!progress.activePowerUp.isNullOrBlank() && progress.powerUpExpiresAt > System.currentTimeMillis()) {
                    try {
                        val type = PowerUpType.valueOf(progress.activePowerUp)
                        _activePowerUp.value = type
                        val remainingSec = ((progress.powerUpExpiresAt - System.currentTimeMillis()) / 1000).toInt()
                        _powerUpRemainingSeconds.value = remainingSec.coerceAtLeast(0)
                    } catch (_: Exception) {}
                }
            } else {
                val initial = UserProgressEntity(
                    coins = 120,
                    unlockedInventory = ""
                )
                userProgressDao.insertOrUpdate(initial)
            }

            dailyStepDao.observeRecentDays().collect { list ->
                _recentDays.value = list
                updateAchievements()
            }
        }
    }

    /**
     * CORE AUTONOMOUS ENGINE:
     * Everything triggers automatically while phone is in pocket!
     */
    private fun onStepEvent(delta: Int, spm: Int) {
        _currentCadence.value = spm
        val stepMultiplier = if (_activePowerUp.value == PowerUpType.SUPER_MUSHROOM) 2 else 1
        val actualDelta = delta * stepMultiplier

        val newSteps = _stepsToday.value + actualDelta
        _stepsToday.value = newSteps

        // 1. STAR RUSH ACTIVE SPRINT TRACKING
        if (_isStarRushActive.value) {
            _starRushSteps.value += actualDelta
            starRushCadenceSamples.add(spm)

            // If walking fast enough (>= target), award massive 5x star coin bonus!
            if (spm >= _starRushTargetSpm.value) {
                val bonus = 1 * actualDelta
                _starRushCoins.value += bonus
                _coins.value += bonus
                soundManager.playCoinSound()
            }
        } else {
            // 2. AUTONOMOUS STARMAN INTERVAL CHECK
            stepsWalkedInCurrentStarInterval += actualDelta
            val remaining = (currentTargetStarInterval - stepsWalkedInCurrentStarInterval).coerceAtLeast(0)
            _stepsUntilNextStar.value = remaining

            if (stepsWalkedInCurrentStarInterval >= currentTargetStarInterval) {
                // SURPRISE STARMAN ACTIVATES IN POCKET!
                rollNextStarThreshold()
                triggerAutonomousStarRush()
            }
        }

        // 3. AUTONOMOUS MYSTERY ? BLOCK CHECK
        stepsSinceLastAutoMystery += actualDelta
        if (stepsSinceLastAutoMystery >= nextAutoMysterySteps) {
            stepsSinceLastAutoMystery = 0
            nextAutoMysterySteps = Random.nextInt(150, 260)
            triggerAutonomousMysteryDrop()
        }

        // 4. Standard Coin accumulation (every 20 steps, or 10 steps with Tanooki Leaf)
        val coinStepThreshold = if (_activePowerUp.value == PowerUpType.TANOOKI_LEAF) 10 else 20
        stepsSinceLastCoin += actualDelta
        if (stepsSinceLastCoin >= coinStepThreshold) {
            val earnedCoins = stepsSinceLastCoin / coinStepThreshold
            stepsSinceLastCoin %= coinStepThreshold
            _coins.value += earnedCoins
            soundManager.playCoinSound()
        }

        // 5. Stage Checkpoint
        checkStageProgression(newSteps)

        saveCurrentDailySnapshot()
        updateAchievements()
    }

    private fun triggerAutonomousStarRush() {
        if (_isStarRushActive.value) return

        // 3 heavy vibration pulses in pocket to alert user
        soundManager.vibratePocketStarStart()
        soundManager.playPowerUpSound()

        addAutoDropEvent(
            title = "⭐ Starman Rush Activated!",
            desc = "Random interval reached! Walk faster while the star theme plays!",
            icon = "STAR"
        )

        _isStarRushActive.value = true
        _starRushSecondsLeft.value = _starSprintDuration.value
        _starRushTargetSpm.value = 130
        _starRushSteps.value = 0
        _starRushCoins.value = 0
        starRushCadenceSamples.clear()

        // Starts the official Starman MP3 theme!
        soundManager.startStarRushTheme()

        starRushTimerJob?.cancel()
        starRushTimerJob = viewModelScope.launch {
            while (isActive && _starRushSecondsLeft.value > 0) {
                delay(1000)
                _starRushSecondsLeft.value -= 1
            }
            finishStarRush()
        }
    }

    private fun finishStarRush() {
        _isStarRushActive.value = false
        _totalStarRushesToday.value += 1

        soundManager.stopStarRushTheme()
        soundManager.vibratePocketStarEnd() // 2 pulses: Return to normal pace!
        soundManager.playFanfare()

        val avgSpm = if (starRushCadenceSamples.isNotEmpty()) {
            starRushCadenceSamples.average().toInt()
        } else {
            _currentCadence.value
        }

        val fastSamples = starRushCadenceSamples.count { it >= _starRushTargetSpm.value }
        val percent = if (starRushCadenceSamples.isNotEmpty()) {
            (fastSamples * 100) / starRushCadenceSamples.size
        } else 80

        val bonus = (_starRushSteps.value / 2).coerceAtLeast(15)
        _coins.value += bonus
        _starRushCoins.value += bonus

        val result = StarRushResult(
            stepsTaken = _starRushSteps.value,
            averageSpm = avgSpm,
            bonusCoins = _starRushCoins.value,
            targetMetPercent = percent
        )
        _starRushCompletionNotice.value = result

        addAutoDropEvent(
            title = "⭐ Star Sprint Complete",
            desc = "Sprinted ${result.stepsTaken} steps @ ${result.averageSpm} SPM (+${result.bonusCoins} coins). Cool down now!",
            icon = "STAR"
        )

        // Auto-dismiss the summary notice after 7 seconds so pocket screen stays clean
        completionAutoDismissJob?.cancel()
        completionAutoDismissJob = viewModelScope.launch {
            delay(7000)
            _starRushCompletionNotice.value = null
        }

        saveCurrentDailySnapshot()
        saveProgress()
    }

    fun dismissCompletionNotice() {
        _starRushCompletionNotice.value = null
    }

    private fun triggerAutonomousMysteryDrop() {
        _mysteryBlockBounced.value = true
        soundManager.vibrateMysteryDrop()

        viewModelScope.launch {
            delay(160)
            _mysteryBlockBounced.value = false

            val roll = Random.nextInt(100)
            when {
                roll < 55 -> {
                    // Coin cache
                    val coinsWon = Random.nextInt(15, 35)
                    _coins.value += coinsWon
                    soundManager.playCoinSound()
                    addAutoDropEvent(
                        title = "+$coinsWon Gold Coins",
                        desc = "Autonomous Mystery ? Block popped while walking",
                        icon = "COIN"
                    )
                }
                roll < 75 -> {
                    // Super Mushroom (Auto-Equip 2x multiplier)
                    _activePowerUp.value = PowerUpType.SUPER_MUSHROOM
                    _powerUpRemainingSeconds.value = PowerUpType.SUPER_MUSHROOM.durationSeconds
                    soundManager.playPowerUpSound()
                    addAutoDropEvent(
                        title = "🍄 Super Mushroom Auto-Equipped!",
                        desc = "Giant Mario! 2x step point multiplier active for 10 min",
                        icon = "MUSHROOM"
                    )
                }
                roll < 90 -> {
                    // Fire Flower (Auto-Equip 1.5x calorie burn)
                    _activePowerUp.value = PowerUpType.FIRE_FLOWER
                    _powerUpRemainingSeconds.value = PowerUpType.FIRE_FLOWER.durationSeconds
                    soundManager.playPowerUpSound()
                    addAutoDropEvent(
                        title = "🔥 Fire Flower Auto-Equipped!",
                        desc = "1.5x calorie burn boost active for 15 min",
                        icon = "FIRE"
                    )
                }
                else -> {
                    // 1-Up Mushroom (+300 steps and streak shield)
                    _stepsToday.value += 300
                    _streakDays.value += 1
                    soundManager.playOneUpSound()
                    addAutoDropEvent(
                        title = "🟢 1-Up Mushroom Auto-Applied!",
                        desc = "+300 bonus steps added & walking streak shielded!",
                        icon = "ONE_UP"
                    )
                }
            }
            saveProgress()
        }
    }

    private fun addAutoDropEvent(title: String, desc: String, icon: String) {
        val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val newEvent = AutoDropEvent(
            title = title,
            description = desc,
            timeFormatted = timeStr,
            iconType = icon
        )
        val currentList = _autoDropsLog.value.toMutableList()
        currentList.add(0, newEvent)
        _autoDropsLog.value = currentList.take(25) // Keep last 25 pocket events
    }

    private fun checkStageProgression(steps: Int) {
        val stageRequiredSteps = 1500
        val targetStage = ((steps / stageRequiredSteps) % 4) + 1
        val targetWorld = ((steps / (stageRequiredSteps * 4))) + 1

        if (targetWorld > _currentWorld.value || (targetWorld == _currentWorld.value && targetStage > _currentStage.value)) {
            _currentWorld.value = targetWorld
            _currentStage.value = targetStage
            soundManager.playFanfare()
            addAutoDropEvent(
                title = "🚩 Flagpole Reached: World $targetWorld-$targetStage!",
                desc = "Checkpoint cleared! Stage advancement recorded.",
                icon = "FLAG"
            )
            saveProgress()
        }
    }

    private fun saveCurrentDailySnapshot() {
        viewModelScope.launch {
            val distance = _stepsToday.value * 0.000762f
            val calMultiplier = if (_activePowerUp.value == PowerUpType.FIRE_FLOWER) 1.5f else 1.0f
            val calories = (_stepsToday.value * 0.042f * calMultiplier).toInt()
            val activeMin = (_stepsToday.value / 100).coerceAtLeast(1)

            dailyStepDao.insertOrUpdate(
                DailyStepEntity(
                    date = todayDateString,
                    steps = _stepsToday.value,
                    goal = _dailyGoal.value,
                    distanceKm = distance,
                    calories = calories,
                    activeMinutes = activeMin,
                    coinsEarned = _coins.value,
                    starRushesCompleted = _totalStarRushesToday.value
                )
            )
        }
    }

    private fun saveProgress() {
        viewModelScope.launch {
            val activeType = _activePowerUp.value?.name
            val expiresAt = if (activeType != null) {
                System.currentTimeMillis() + (_powerUpRemainingSeconds.value * 1000L)
            } else 0L

            userProgressDao.insertOrUpdate(
                UserProgressEntity(
                    id = 1,
                    coins = _coins.value,
                    currentWorld = _currentWorld.value,
                    currentStage = _currentStage.value,
                    totalStepsAllTime = (_recentDays.value.sumOf { it.steps.toLong() } + _stepsToday.value),
                    streakDays = _streakDays.value,
                    activePowerUp = activeType,
                    powerUpExpiresAt = expiresAt,
                    unlockedInventory = ""
                )
            )
        }
    }

    private fun startPowerUpWatchdog() {
        powerUpTimerJob?.cancel()
        powerUpTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val current = _activePowerUp.value
                val rem = _powerUpRemainingSeconds.value
                if (current != null && current != PowerUpType.SUPER_STAR) {
                    if (rem > 1) {
                        _powerUpRemainingSeconds.value = rem - 1
                    } else {
                        _activePowerUp.value = null
                        _powerUpRemainingSeconds.value = 0
                        soundManager.playPipeSound()
                        saveProgress()
                    }
                }
            }
        }
    }

    fun toggleSound(): Boolean {
        soundManager.isMuted = !soundManager.isMuted
        return !soundManager.isMuted
    }

    // Testing / Simulator Speed controls
    fun setSimulationSpeed(speed: SimulationSpeed) {
        sensorTracker.setSimulation(speed)
    }

    fun addManualSteps(steps: Int) {
        sensorTracker.addInstantSteps(steps)
    }

    fun updateDailyGoal(newGoal: Int) {
        _dailyGoal.value = newGoal.coerceIn(1000, 50000)
        saveCurrentDailySnapshot()
    }

    private fun updateAchievements() {
        val totalSteps = _stepsToday.value + _recentDays.value.sumOf { it.steps }
        val starRushes = _totalStarRushesToday.value + _recentDays.value.sumOf { it.starRushesCompleted }

        _achievements.value = listOf(
            Achievement(
                id = "first_steps",
                title = "World 1-1 Starter",
                description = "Take your first 500 steps in StarStep.",
                target = 500,
                current = totalSteps.coerceAtMost(500),
                isUnlocked = totalSteps >= 500,
                coinReward = 25,
                iconType = "BOOTS"
            ),
            Achievement(
                id = "star_sprinter",
                title = "Autonomous Star Sprinter",
                description = "Complete 3 random Starman interval sprints.",
                target = 3,
                current = starRushes.coerceAtMost(3),
                isUnlocked = starRushes >= 3,
                coinReward = 75,
                iconType = "STAR"
            ),
            Achievement(
                id = "coin_collector",
                title = "Mushroom Tycoon",
                description = "Collect 250 lifetime gold coins.",
                target = 250,
                current = _coins.value.coerceAtMost(250),
                isUnlocked = _coins.value >= 250,
                coinReward = 100,
                iconType = "COIN"
            ),
            Achievement(
                id = "goal_conqueror",
                title = "Flagpole Climber",
                description = "Reach your daily step goal of 6,000 steps.",
                target = _dailyGoal.value,
                current = _stepsToday.value.coerceAtMost(_dailyGoal.value),
                isUnlocked = _stepsToday.value >= _dailyGoal.value,
                coinReward = 150,
                iconType = "FLAG"
            ),
            Achievement(
                id = "marathon_hero",
                title = "Super Star Marathoner",
                description = "Accumulate 15,000 total steps across worlds.",
                target = 15000,
                current = totalSteps.coerceAtMost(15000),
                isUnlocked = totalSteps >= 15000,
                coinReward = 300,
                iconType = "MUSHROOM"
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        sensorTracker.release()
        soundManager.stopStarRushTheme()
        completionAutoDismissJob?.cancel()
    }
}
