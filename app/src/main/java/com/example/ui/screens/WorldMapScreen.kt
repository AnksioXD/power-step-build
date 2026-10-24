package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MarioAvatar
import com.example.ui.components.MarioCoinIcon
import com.example.ui.theme.LuigiGreen
import com.example.ui.theme.MarioRed
import com.example.ui.theme.StarGold
import com.example.ui.viewmodel.StarStepViewModel

data class WorldInfo(
    val worldNum: Int,
    val name: String,
    val description: String,
    val themeColorHex: Long,
    val totalStepsRequired: Int
)

val WORLD_LIST = listOf(
    WorldInfo(1, "Grassland Kingdom", "Sunny plains & Goomba hills", 0xFF43A047, 6000),
    WorldInfo(2, "Desert Dunes", "Quicksand pyramids & sun rays", 0xFFFFA000, 12000),
    WorldInfo(3, "Cheep Cheep Bay", "Coral reefs & underwater currents", 0xFF0288D1, 18000),
    WorldInfo(4, "Giant Land", "Massive blocks & towering pipes", 0xFF7B1FA2, 24000),
    WorldInfo(5, "Sky Athletic", "Bouncing beanstalks & flying wings", 0xFF00ACC1, 30000),
    WorldInfo(6, "Ice Caverns", "Slippery glaciers & ice blooms", 0xFF4FC3F7, 36000),
    WorldInfo(7, "Pipe Labyrinth", "Tangled piranha warp network", 0xFF2E7D32, 42000),
    WorldInfo(8, "Bowser Dark Kingdom", "Lava bridges & the final fortress", 0xFFD32F2F, 48000)
)

@Composable
fun WorldMapScreen(
    viewModel: StarStepViewModel,
    modifier: Modifier = Modifier
) {
    val currentWorld by viewModel.currentWorld.collectAsState()
    val currentStage by viewModel.currentStage.collectAsState()
    val stepsToday by viewModel.stepsToday.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = MarioRed,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "MARIO WORLD EXPEDITION",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Walk to advance through all 8 Worlds and 32 Stages!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        itemsIndexed(WORLD_LIST) { index, world ->
            val isCurrent = world.worldNum == currentWorld
            val isUnlocked = world.worldNum <= currentWorld
            val isCompleted = world.worldNum < currentWorld

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("world_card_${world.worldNum}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) Color(world.themeColorHex).copy(alpha = 0.15f)
                    else if (isCompleted) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(
                    if (isCurrent) 2.dp else 1.dp,
                    if (isCurrent) Color(world.themeColorHex) else Color.Transparent
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // World badge
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isUnlocked) Color(world.themeColorHex)
                                    else Color.Gray.copy(alpha = 0.4f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrent) {
                                MarioAvatar(modifier = Modifier, size = 32.dp, isWalking = true)
                            } else if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Cleared",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "WORLD ${world.worldNum}: ${world.name}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                                )
                            }
                            Text(
                                text = world.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // State pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                isCurrent -> Color(world.themeColorHex).copy(alpha = 0.2f)
                                isCompleted -> LuigiGreen.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = when {
                                    isCurrent -> "STAGE $currentStage/4"
                                    isCompleted -> "CLEARED"
                                    else -> "${world.totalStepsRequired} STEPS"
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = when {
                                    isCurrent -> Color(world.themeColorHex)
                                    isCompleted -> LuigiGreen
                                    else -> Color.Gray
                                }
                            )
                        }
                    }

                    // Stages dots 1-1, 1-2, 1-3, 1-4
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (s in 1..4) {
                            val stageDone = isCompleted || (isCurrent && s < currentStage)
                            val stageCurrent = isCurrent && s == currentStage

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    stageCurrent -> Color(world.themeColorHex)
                                    stageDone -> LuigiGreen.copy(alpha = 0.25f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                border = if (stageCurrent) BorderStroke(1.5.dp, StarGold) else null,
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (s == 4) "CASTLE" else "${world.worldNum}-$s",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (stageCurrent) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (stageDone) "✓ DONE" else if (stageCurrent) "HERE!" else "1,500s",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (stageCurrent) StarGold else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
