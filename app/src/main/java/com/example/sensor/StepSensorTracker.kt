package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.ArrayDeque
import kotlin.math.sqrt

enum class SimulationSpeed(val spm: Int, val label: String) {
    IDLE(0, "Resting (0 SPM)"),
    LEISURE(80, "Casual Walk (80 SPM)"),
    BRISK(110, "Brisk Walk (110 SPM)"),
    STAR_RUSH(155, "Star Sprint (155 SPM)"),
    POWER_RUN(185, "Fast Run (185 SPM)")
}

class StepSensorTracker(
    private val context: Context,
    private val onStep: (count: Int, cadenceSpm: Int) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val scope = CoroutineScope(Dispatchers.Default)

    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    // For cadence estimation
    private val recentStepTimes = ArrayDeque<Long>()
    private val maxCadenceWindowMs = 8000L // 8 second window

    // Accelerometer fallback peak detector
    private var lastAccelMagnitude = 9.8f
    private var lastPeakTime = 0L
    private val minStepIntervalMs = 280L
    private val peakThreshold = 12.2f

    // Live state
    private val _isSensorActive = MutableStateFlow(false)
    val isSensorActive: StateFlow<Boolean> = _isSensorActive.asStateFlow()

    private val _currentCadence = MutableStateFlow(0)
    val currentCadence: StateFlow<Int> = _currentCadence.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private val _simSpeed = MutableStateFlow(SimulationSpeed.IDLE)
    val simSpeed: StateFlow<SimulationSpeed> = _simSpeed.asStateFlow()

    private var simulationJob: Job? = null
    private var cadenceDecayJob: Job? = null

    init {
        sensorManager?.let { sm ->
            stepCounterSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            stepDetectorSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            accelerometerSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
        startCadenceDecayWatchdog()
    }

    fun startListening() {
        if (sensorManager == null) return
        var registered = false

        stepDetectorSensor?.let {
            registered = sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) || registered
        }

        if (!registered && stepCounterSensor != null) {
            registered = sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI) || registered
        }

        // Accelerometer fallback ensures the app always detects steps even on test devices/emulators
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
            registered = true
        }

        _isSensorActive.value = registered
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
        _isSensorActive.value = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || _isSimulating.value) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values.isNotEmpty() && event.values[0] > 0) {
                    recordStep()
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                // Tri-axial magnitude peak detector fallback
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                val now = System.currentTimeMillis()
                if (magnitude > peakThreshold && lastAccelMagnitude <= peakThreshold) {
                    if (now - lastPeakTime > minStepIntervalMs) {
                        lastPeakTime = now
                        recordStep()
                    }
                }
                lastAccelMagnitude = magnitude
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun recordStep(count: Int = 1) {
        val now = System.currentTimeMillis()
        synchronized(recentStepTimes) {
            for (i in 0 until count) {
                recentStepTimes.addLast(now)
            }
            // Evict old timestamps outside the window
            while (recentStepTimes.isNotEmpty() && now - recentStepTimes.first() > maxCadenceWindowMs) {
                recentStepTimes.removeFirst()
            }
            val spm = calculateCadence(now)
            _currentCadence.value = spm
            onStep(count, spm)
        }
    }

    private fun calculateCadence(now: Long): Int {
        if (recentStepTimes.size < 2) return 0
        val durationMs = now - recentStepTimes.first()
        if (durationMs < 800) return 0
        val steps = recentStepTimes.size - 1
        val spm = (steps * 60000.0 / durationMs).toInt()
        return spm.coerceIn(0, 240)
    }

    private fun startCadenceDecayWatchdog() {
        cadenceDecayJob?.cancel()
        cadenceDecayJob = scope.launch {
            while (isActive) {
                delay(1200)
                if (!_isSimulating.value) {
                    val now = System.currentTimeMillis()
                    synchronized(recentStepTimes) {
                        while (recentStepTimes.isNotEmpty() && now - recentStepTimes.first() > maxCadenceWindowMs) {
                            recentStepTimes.removeFirst()
                        }
                        _currentCadence.value = calculateCadence(now)
                    }
                }
            }
        }
    }

    // Interactive Demo / Simulation Features for testing anywhere
    fun setSimulation(speed: SimulationSpeed) {
        _simSpeed.value = speed
        simulationJob?.cancel()

        if (speed == SimulationSpeed.IDLE) {
            _isSimulating.value = false
            _currentCadence.value = 0
            return
        }

        _isSimulating.value = true
        _currentCadence.value = speed.spm

        val intervalMs = (60000L / speed.spm).coerceAtLeast(150L)
        simulationJob = scope.launch {
            while (isActive) {
                delay(intervalMs)
                _currentCadence.value = speed.spm
                onStep(1, speed.spm)
            }
        }
    }

    fun addInstantSteps(steps: Int) {
        recordStep(steps)
    }

    fun release() {
        stopListening()
        simulationJob?.cancel()
        cadenceDecayJob?.cancel()
    }
}
