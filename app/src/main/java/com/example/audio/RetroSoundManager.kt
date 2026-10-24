package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Build
import com.example.R
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * High-performance 8-bit NES chiptune synthesizer and sound effect engine
 * generates authentic retro waveforms entirely on-device without external asset files.
 */
class RetroSoundManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var starMusicJob: Job? = null
    private var starMediaPlayer: MediaPlayer? = null

    var isMuted: Boolean = false
        set(value) {
            field = value
            if (value) {
                stopStarRushTheme()
            }
        }

    private val sampleRate = 22050

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int = 180) {
        if (isMuted) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    /**
     * 3-pulse pocket haptic burst to alert user in their pocket that Starman activated!
     */
    fun vibratePocketStarStart() {
        if (isMuted) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 200, 100, 200, 100, 300)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 200, 100, 200, 100, 300), -1)
            }
        } catch (_: Exception) {}
    }

    /**
     * 2-pulse pocket haptic to alert user that Star sprint ended and to cool down
     */
    fun vibratePocketStarEnd() {
        if (isMuted) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 180, 120, 250)
                val amplitudes = intArrayOf(0, 200, 0, 220)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 180, 120, 250), -1)
            }
        } catch (_: Exception) {}
    }

    fun vibrateMysteryDrop() {
        vibrate(80, 200)
    }

    /**
     * Plays the legendary 2-tone Coin pickup chime (B5 -> E6 square wave)
     */
    fun playCoinSound() {
        if (isMuted) return
        vibrate(35, 120)
        scope.launch {
            val track = createAudioTrack()
            try {
                track.play()
                // B5 (988 Hz) for 70ms
                val part1 = generateSquareTone(987.77, 0.07, 0.35, dutyCycle = 0.5)
                // E6 (1319 Hz) for 280ms
                val part2 = generateSquareTone(1318.51, 0.28, 0.4, dutyCycle = 0.5, fadeOut = true)

                track.write(part1, 0, part1.size)
                track.write(part2, 0, part2.size)
            } catch (_: Exception) {
            } finally {
                track.release()
            }
        }
    }

    /**
     * Plays the Mario Power-Up ascending arpeggio
     */
    fun playPowerUpSound() {
        if (isMuted) return
        vibrate(100, 220)
        scope.launch {
            val track = createAudioTrack()
            try {
                track.play()
                val notes = doubleArrayOf(330.0, 392.0, 659.0, 523.0, 587.0, 784.0, 659.0, 1046.5)
                for (freq in notes) {
                    val tone = generateSquareTone(freq, 0.05, 0.35, dutyCycle = 0.25)
                    track.write(tone, 0, tone.size)
                }
            } catch (_: Exception) {
            } finally {
                track.release()
            }
        }
    }

    /**
     * Plays the 1-Up Mushroom chime
     */
    fun playOneUpSound() {
        if (isMuted) return
        vibrate(120, 250)
        scope.launch {
            val track = createAudioTrack()
            try {
                track.play()
                val notes = doubleArrayOf(659.25, 783.99, 1318.51, 1046.50, 1174.66, 1567.98)
                for ((index, freq) in notes.withIndex()) {
                    val dur = if (index == notes.size - 1) 0.25 else 0.06
                    val tone = generateSquareTone(freq, dur, 0.38, dutyCycle = 0.5, fadeOut = index == notes.size - 1)
                    track.write(tone, 0, tone.size)
                }
            } catch (_: Exception) {
            } finally {
                track.release()
            }
        }
    }

    /**
     * Plays a classic spring/jump bounce sound
     */
    fun playJumpSound() {
        if (isMuted) return
        vibrate(25, 100)
        scope.launch {
            val track = createAudioTrack()
            try {
                track.play()
                val sweep = generateFrequencySweep(200.0, 650.0, 0.12, 0.3)
                track.write(sweep, 0, sweep.size)
            } catch (_: Exception) {
            } finally {
                track.release()
            }
        }
    }

    /**
     * Plays a warp pipe descending sound
     */
    fun playPipeSound() {
        if (isMuted) return
        vibrate(60, 180)
        scope.launch {
            val track = createAudioTrack()
            try {
                track.play()
                val sweep = generateFrequencySweep(750.0, 220.0, 0.2, 0.35)
                track.write(sweep, 0, sweep.size)
            } catch (_: Exception) {
            } finally {
                track.release()
            }
        }
    }

    /**
     * Plays victory / Stage Clear Fanfare
     */
    fun playFanfare() {
        if (isMuted) return
        vibrate(200, 255)
        scope.launch {
            val track = createAudioTrack()
            try {
                track.play()
                val notes = listOf(
                    523.25 to 0.10, // C5
                    523.25 to 0.10, // C5
                    523.25 to 0.10, // C5
                    659.25 to 0.22, // E5
                    523.25 to 0.10, // C5
                    659.25 to 0.10, // E5
                    783.99 to 0.40  // G5
                )
                for ((freq, dur) in notes) {
                    val tone = generateSquareTone(freq, dur, 0.4, dutyCycle = 0.5, fadeOut = dur > 0.2)
                    track.write(tone, 0, tone.size)
                    delay((dur * 850).toLong())
                }
            } catch (_: Exception) {
            } finally {
                track.release()
            }
        }
    }

    /**
     * Synthesizes the authentic original Super Mario Bros. Invincible Starman theme (Koji Kondo, 1985)
     * Two-measure syncopated disco vamp with Pulse 1/2 chords and Triangle bass at 150 BPM.
     */
    private val originalStarmanLoopBuffer: ShortArray by lazy {
        generateOriginalStarmanLoop()
    }

    private fun generateOriginalStarmanLoop(): ShortArray {
        val bpm = 150.0
        val stepSec = (60.0 / bpm) / 4.0 // 0.100s per 16th note
        val stepSamples = (stepSec * sampleRate).toInt()
        val totalSteps = 32
        val totalSamples = stepSamples * totalSteps
        val buffer = ShortArray(totalSamples)

        val chordSteps = setOf(0, 1, 2, 5, 6, 12, 13)
        val singleSteps = setOf(4, 8, 10)

        // Measure 1: Dm7 / F chords [F5, C6], bass D3/D4, root note F4
        val m1Chords = doubleArrayOf(698.46, 1046.50) // F5, C6
        val m1Single = 349.23 // F4
        val m1BassRoot = 146.83 // D3
        val m1BassOctave = 293.66 // D4

        // Measure 2: Cmaj7 / Em chords [E5, B5], bass C3/C4, root note E4
        val m2Chords = doubleArrayOf(659.25, 987.77) // E5, B5
        val m2Single = 329.63 // E4
        val m2BassRoot = 130.81 // C3
        val m2BassOctave = 261.63 // C4

        for (step in 0 until totalSteps) {
            val isMeasure2 = step >= 16
            val localStep = step % 16
            val offset = step * stepSamples

            val chords = if (isMeasure2) m2Chords else m1Chords
            val singleFreq = if (isMeasure2) m2Single else m1Single
            val bassFreq = if (isMeasure2) {
                if (localStep % 2 == 1) m2BassOctave else m2BassRoot
            } else {
                if (localStep % 2 == 1) m1BassOctave else m1BassRoot
            }

            val isChord = localStep in chordSteps
            val isSingle = localStep in singleSteps

            for (i in 0 until stepSamples) {
                val progress = i.toDouble() / stepSamples
                // Staccato decay
                val decay = (1.0 - progress * 0.35).coerceIn(0.0, 1.0)

                // Melody Voice (Square Waves)
                var melVal = 0.0
                if (isChord) {
                    for (freq in chords) {
                        val period = sampleRate / freq
                        val phase = i % period
                        melVal += if (phase < period * 0.5) 0.5 else -0.5
                    }
                    melVal /= chords.size
                } else if (isSingle) {
                    val period = sampleRate / singleFreq
                    val phase = i % period
                    melVal = if (phase < period * 0.5) 0.45 else -0.45
                }
                melVal *= decay

                // Bass Voice (NES Triangle Wave)
                val bassPeriod = sampleRate / bassFreq
                val bassPhase = (i % bassPeriod) / bassPeriod
                val bassVal = if (bassPhase < 0.5) {
                    -1.0 + 4.0 * bassPhase
                } else {
                    3.0 - 4.0 * bassPhase
                }
                val bassDecay = (1.0 - progress * 0.15).coerceIn(0.0, 1.0)
                val finalBass = bassVal * 0.35 * bassDecay

                // Mix: 60% melody pulse, 40% triangle bass
                val mixed = (melVal * 0.60 + finalBass * 0.40) * 32767.0
                val clamped = mixed.toInt().coerceIn(-32768, 32767).toShort()
                buffer[offset + i] = clamped
            }
        }
        return buffer
    }

    /**
     * Loops the official Starman theme soundtrack requested by the user!
     * When this sound plays, the user is challenged to walk/run faster!
     */
    fun startStarRushTheme() {
        stopStarRushTheme()
        if (isMuted) return

        try {
            val mp = MediaPlayer.create(context, R.raw.starman)
            if (mp != null) {
                mp.isLooping = true
                mp.setVolume(1.0f, 1.0f)
                mp.start()
                starMediaPlayer = mp
                return
            }
        } catch (_: Exception) {
            // Fallback to procedural chiptune synth if audio track fails to instantiate
        }

        starMusicJob = scope.launch {
            val track = createAudioTrack()
            try {
                track.play()
                val loopData = originalStarmanLoopBuffer
                while (isActive) {
                    track.write(loopData, 0, loopData.size)
                }
            } catch (_: Exception) {
            } finally {
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        }
    }

    fun stopStarRushTheme() {
        try {
            starMediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {}
        starMediaPlayer = null

        starMusicJob?.cancel()
        starMusicJob = null
    }

    private fun createAudioTrack(): AudioTrack {
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = minBufferSize.coerceAtLeast(sampleRate / 4)

        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }

    private fun generateSquareTone(
        frequency: Double,
        durationSeconds: Double,
        volume: Double = 0.3,
        dutyCycle: Double = 0.5,
        fadeOut: Boolean = false
    ): ShortArray {
        val numSamples = (durationSeconds * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val periodSamples = sampleRate / frequency
        val threshold = (periodSamples * dutyCycle).toInt()
        val maxAmp = (32767 * volume).toInt()

        for (i in 0 until numSamples) {
            val phase = (i % periodSamples.toInt())
            var amp = if (phase < threshold) maxAmp else -maxAmp
            if (fadeOut) {
                val decay = 1.0 - (i.toDouble() / numSamples)
                amp = (amp * decay).toInt()
            }
            buffer[i] = amp.toShort()
        }
        return buffer
    }

    private fun generateFrequencySweep(
        startFreq: Double,
        endFreq: Double,
        durationSeconds: Double,
        volume: Double = 0.3
    ): ShortArray {
        val numSamples = (durationSeconds * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val maxAmp = (32767 * volume)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val deltaPhase = 2.0 * PI * currentFreq / sampleRate
            phase += deltaPhase
            val sample = (sin(phase) * maxAmp).toInt()
            buffer[i] = sample.toShort()
        }
        return buffer
    }
}
