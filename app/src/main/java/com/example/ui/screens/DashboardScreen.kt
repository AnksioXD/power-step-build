package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PowerUpType
import com.example.sensor.SimulationSpeed
import com.example.ui.components.MarioCoinIcon
import com.example.ui.components.MysteryBlockCard
import com.example.ui.components.SuperMushroomIcon
import com.example.ui.components.SuperStarIcon
import com.example.ui.components.WorldProgressBar
import com.example.ui.theme.LuigiGreen
import com.example.ui.theme.MarioRed
import com.example.ui.theme.StarGold
import com.example.ui.viewmodel.StarIntervalFrequency
import com.example.ui.viewmodel.StarStepViewModel

@Composable
fun DashboardScreen(
    viewModel: StarStepViewModel,
    modifier: Modifier = Modifier
) {
    val stepsToday by viewModel.stepsToday.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val currentSpm by viewModel.currentCadence.collectAsState()
    val activePowerUp by viewModel.activePowerUp.collectAsState()
    val powerUpSecsLeft by viewModel.powerUpRemainingSeconds.collectAsState()
    val currentWorld by viewModel.currentWorld.collectAsState()
    val currentStage by viewModel.currentStage.collectAsState()
    val mysteryBounced by viewModel.mysteryBlockBounced.collectAsState()

    // Autonomous Interval States
    val stepsUntilNextStar by viewModel.stepsUntilNextStar.collectAsState()
    val starFrequency by viewModel.starFrequency.collectAsState()
    val starSprintDuration by viewModel.starSprintDuration.collectAsState()
    val totalStarRushesToday by viewModel.totalStarRushesToday.collectAsState()

    val simSpeed by viewModel.sensorTracker.simSpeed.collectAsState()
    val isSimulating by viewModel.sensorTracker.isSimulating.collectAsState()

    var showSimControls by remember { mutableStateOf(false) }

    val stepProgress = (stepsToday.toFloat() / dailyGoal).coerceIn(0f, 1f)
    val distanceKm = stepsToday * 0.000762f
    val calMultiplier = if (activePowerUp == PowerUpType.FIRE_FLOWER) 1.5f else 1.0f
    val calories = (stepsToday * 0.042f * calMultiplier).toInt()
    val activeMinutes = (stepsToday / 100).coerceAtLeast(1)

    val isWalking = currentSpm > 10

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Top Status Header: Coins & Speedometer
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coins pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = StarGold.copy(alpha = 0.2f),
                    border = BorderStroke(1.5.dp, StarGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MarioCoinIcon(modifier = Modifier, size = 20.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$coins COINS",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StarGold
                        )
                    }
                }

                // Cadence indicator pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isWalking) LuigiGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (isWalking) LuigiGreen else Color.Gray)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = if (isWalking) LuigiGreen else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isWalking) "$currentSpm SPM" else "RESTING",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isWalking) LuigiGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Active Power-Up Banner (Auto-applied)
        if (activePowerUp != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(activePowerUp!!.primaryColorHex).copy(alpha = 0.22f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, Color(activePowerUp!!.primaryColorHex))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (activePowerUp) {
                            PowerUpType.SUPER_MUSHROOM -> SuperMushroomIcon(modifier = Modifier, size = 32.dp)
                            PowerUpType.FIRE_FLOWER -> Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFF7043),
                                modifier = Modifier.size(32.dp)
                            )
                            else -> SuperStarIcon(modifier = Modifier, size = 32.dp, isPulsing = false)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${activePowerUp!!.title.uppercase()} AUTO-ACTIVE",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(activePowerUp!!.primaryColorHex)
                            )
                            Text(
                                text = activePowerUp!!.subtitle,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = String.format("%02d:%02d", powerUpSecsLeft / 60, powerUpSecsLeft % 60),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(activePowerUp!!.primaryColorHex)
                        )
                    }
                }
            }
        }

        // ⭐ AUTONOMOUS STARMAN INTERVAL CARD (POCKET MODE) ⭐
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("autonomous_starman_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                border = BorderStroke(
                    2.dp,
                    Brush.horizontalGradient(listOf(StarGold, MarioRed, Color(0xFF00E676)))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SuperStarIcon(modifier = Modifier, size = 42.dp, isPulsing = true)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AUTONOMOUS STARMAN INTERVAL",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = StarGold
                            )
                            Text(
                                text = "Walk with phone in pocket. Music & 3 vibration pulses will cue you to sprint!",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Next Starman Countdown Tracker
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, StarGold.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "NEXT RANDOM STAR IN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "~$stepsUntilNextStar STEPS",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = StarGold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "SPRINTS TODAY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "$totalStarRushesToday DONE",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LuigiGreen,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pocket Awareness Hint
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = StarGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Auto-triggers while walking. Pick up pace when music plays!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Frequency Interval Chips
                    Text(
                        text = "INTERVAL FREQUENCY:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StarIntervalFrequency.values().forEach { freq ->
                            FilterChip(
                                selected = starFrequency == freq,
                                onClick = { viewModel.setStarIntervalFrequency(freq) },
                                label = { Text(freq.name.lowercase().capitalize(), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StarGold,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }
            }
        }

        // Daily Pedometer Circle Gauge Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(190.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 14.dp,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        CircularProgressIndicator(
                            progress = { stepProgress },
                            modifier = Modifier.size(190.dp),
                            color = MarioRed,
                            strokeWidth = 14.dp,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$stepsToday",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "GOAL: $dailyGoal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(stepProgress * 100).toInt()}% COMPLETED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (stepProgress >= 1f) LuigiGreen else MarioRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%.2f km", distanceKm),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Distance",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$calories",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (activePowerUp == PowerUpType.FIRE_FLOWER) Color(0xFFFF7043) else MaterialTheme.colorScheme.onSurface
                                )
                                if (activePowerUp == PowerUpType.FIRE_FLOWER) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("🔥", fontSize = 12.sp)
                                }
                            }
                            Text(
                                text = "Calories",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$activeMinutes min",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Active Walk",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Mario World & Stage Progress Bar
        item {
            WorldProgressBar(
                world = currentWorld,
                stage = currentStage,
                stepsToday = stepsToday,
                isWalking = isWalking,
                isGiant = activePowerUp == PowerUpType.SUPER_MUSHROOM,
                hasFirePower = activePowerUp == PowerUpType.FIRE_FLOWER,
                isStarInvincible = activePowerUp == PowerUpType.SUPER_STAR
            )
        }

        // Autonomous Mystery ? Block Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, StarGold.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MysteryBlockCard(
                        isBounced = mysteryBounced,
                        onHit = { /* Autonomous */ },
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AUTONOMOUS ? BLOCKS",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                color = StarGold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "POCKET AUTO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuigiGreen
                            )
                        }
                        Text(
                            text = "Every ~200 steps while walking, mystery blocks automatically pop coins & auto-equip mushrooms and flowers!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Testing & Simulation Control Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSimControls = !showSimControls },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = LuigiGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSimulating) "SIMULATOR ACTIVE: ${simSpeed.label}" else "HARDWARE SENSORS LIVE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = if (showSimControls) "HIDE ▲" else "TEST MODES ▼",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MarioRed
                        )
                    }

                    AnimatedVisibility(visible = showSimControls) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text(
                                text = "Test autonomous Starman intervals & drops at your desk:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = simSpeed == SimulationSpeed.IDLE,
                                    onClick = { viewModel.setSimulationSpeed(SimulationSpeed.IDLE) },
                                    label = { Text("Sensor", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = simSpeed == SimulationSpeed.LEISURE,
                                    onClick = { viewModel.setSimulationSpeed(SimulationSpeed.LEISURE) },
                                    label = { Text("Walk (80)", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = simSpeed == SimulationSpeed.STAR_RUSH,
                                    onClick = { viewModel.setSimulationSpeed(SimulationSpeed.STAR_RUSH) },
                                    label = { Text("Rush (155)", fontSize = 11.sp) }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.addManualSteps(50) },
                                    modifier = Modifier.weight(1f).testTag("add_50_steps_button")
                                ) {
                                    Text("+50 Steps", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.addManualSteps(200) },
                                    modifier = Modifier.weight(1f).testTag("add_200_steps_button")
                                ) {
                                    Text("+200 Steps (Auto Star!)", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
