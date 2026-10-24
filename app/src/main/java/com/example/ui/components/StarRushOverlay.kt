package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LuigiGreen
import com.example.ui.theme.MarioRed
import com.example.ui.theme.StarGold
import com.example.ui.viewmodel.StarRushResult

@Composable
fun StarRushOverlay(
    isActive: Boolean,
    secondsLeft: Int,
    targetSpm: Int,
    currentSpm: Int,
    stepsInRush: Int,
    coinsEarned: Int,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "star_rainbow")
    val rainbowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbow_anim"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rush_pulse"
    )

    val isSpeedSatisfied = currentSpm >= targetSpm

    val rainbowBrush = Brush.sweepGradient(
        listOf(
            Color(0xFFFF1744),
            Color(0xFFFFEA00),
            Color(0xFF00E676),
            Color(0xFF00B0FF),
            Color(0xFFD500F9),
            Color(0xFFFF1744)
        )
    )

    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xEE0B0F19))
                .border(BorderStroke(6.dp, rainbowBrush))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(pulseScale),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Top close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.testTag("close_star_rush_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Stop Star Rush",
                            tint = Color.White
                        )
                    }
                }

                // Star Icon with Glow
                SuperStarIcon(size = 72.dp, isPulsing = true)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "STAR RUSH ACTIVE!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = StarGold,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                )

                Text(
                    text = "WALK FASTER WHILE MUSIC PLAYS!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Timer Countdown Circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .border(4.dp, if (secondsLeft <= 10) MarioRed else StarGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%02d", secondsLeft),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = if (secondsLeft <= 10) MarioRed else Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "SEC LEFT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Dynamic Cadence / Pace Feedback Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            2.dp,
                            if (isSpeedSatisfied) LuigiGreen else Color(0xFFFF9800),
                            RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSpeedSatisfied) Color(0x3343A047) else Color(0x33FF9800)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSpeedSatisfied) Icons.Default.DirectionsRun else Icons.Default.Speed,
                                contentDescription = null,
                                tint = if (isSpeedSatisfied) LuigiGreen else Color(0xFFFF9800),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSpeedSatisfied) "PERFECT SPEED! 5X BONUS!" else "SPEED UP! WALK FASTER!",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = if (isSpeedSatisfied) LuigiGreen else Color(0xFFFF9800),
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("YOUR SPEED", fontSize = 11.sp, color = Color.LightGray)
                                Text(
                                    text = "$currentSpm SPM",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSpeedSatisfied) LuigiGreen else Color(0xFFFF9800),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("TARGET GOAL", fontSize = 11.sp, color = Color.LightGray)
                                Text(
                                    text = "≥ $targetSpm SPM",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Rush Session Stats (Steps & Coins)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Card(
                        modifier = Modifier.weight(1f).padding(end = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("RUSH STEPS", fontSize = 11.sp, color = Color.LightGray)
                            Text(
                                text = "$stepsInRush",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f).padding(start = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MarioCoinIcon(size = 18.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("BONUS COINS", fontSize = 11.sp, color = StarGold)
                            }
                            Text(
                                text = "+$coinsEarned",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = StarGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Simulated Chiptune Visualizer bars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(24.dp)
                ) {
                    val heights = listOf(14.dp, 22.dp, 10.dp, 24.dp, 18.dp, 12.dp, 20.dp, 16.dp)
                    heights.forEachIndexed { idx, h ->
                        val barScale by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(150 + idx * 40, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "bar_$idx"
                        )
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(h * barScale)
                                .background(StarGold, RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StarRushCompletionDialog(
    result: StarRushResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MarioRed),
                modifier = Modifier.testTag("star_rush_dialog_ok")
            ) {
                Text("COOL DOWN TO NORMAL PACE", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SuperStarIcon(size = 32.dp, isPulsing = false)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STAR RUSH COMPLETE!",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Incredible sprint! You pushed your speed while the star theme played. Now return to your comfortable, steady walking pace.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sprint Steps:", fontWeight = FontWeight.Medium)
                            Text("${result.stepsTaken} steps", fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Average Cadence:", fontWeight = FontWeight.Medium)
                            Text("${result.averageSpm} SPM", fontWeight = FontWeight.Bold, color = LuigiGreen)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Target Pace Kept:", fontWeight = FontWeight.Medium)
                            Text("${result.targetMetPercent}%", fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bonus Coins Earned:", fontWeight = FontWeight.Medium)
                            Text("+${result.bonusCoins} 🪙", fontWeight = FontWeight.Bold, color = StarGold)
                        }
                    }
                }
            }
        }
    )
}
