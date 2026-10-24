package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import com.example.data.model.PowerUpType
import com.example.ui.components.MarioCoinIcon
import com.example.ui.components.SuperMushroomIcon
import com.example.ui.components.SuperStarIcon
import com.example.ui.theme.LuigiGreen
import com.example.ui.theme.MarioRed
import com.example.ui.theme.StarGold
import com.example.ui.viewmodel.StarStepViewModel

@Composable
fun PowerUpShopScreen(
    viewModel: StarStepViewModel,
    modifier: Modifier = Modifier
) {
    val coins by viewModel.coins.collectAsState()
    val activePowerUp by viewModel.activePowerUp.collectAsState()
    val powerUpRemainingSeconds by viewModel.powerUpRemainingSeconds.collectAsState()
    val autoDropsLog by viewModel.autoDropsLog.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Header Card
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AUTO-DROPS & BUFFS",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Fully autonomous: All power-ups drop and equip automatically while walking in your pocket!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = StarGold.copy(alpha = 0.2f),
                        border = BorderStroke(1.5.dp, StarGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MarioCoinIcon(modifier = Modifier, size = 18.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$coins",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace,
                                color = StarGold
                            )
                        }
                    }
                }
            }
        }

        // Active Buff Highlight
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (activePowerUp != null) Color(activePowerUp!!.primaryColorHex).copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.5.dp,
                    if (activePowerUp != null) Color(activePowerUp!!.primaryColorHex)
                    else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (activePowerUp != null) Color(activePowerUp!!.primaryColorHex).copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when (activePowerUp) {
                            PowerUpType.SUPER_MUSHROOM -> SuperMushroomIcon(modifier = Modifier, size = 32.dp)
                            PowerUpType.FIRE_FLOWER -> Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFF7043),
                                modifier = Modifier.size(32.dp)
                            )
                            PowerUpType.SUPER_STAR -> SuperStarIcon(modifier = Modifier, size = 32.dp, isPulsing = false)
                            PowerUpType.TANOOKI_LEAF -> Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFF8D6E63),
                                modifier = Modifier.size(32.dp)
                            )
                            PowerUpType.ONE_UP -> SuperMushroomIcon(modifier = Modifier, size = 32.dp, isGreen1Up = true)
                            null -> Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (activePowerUp != null) "${activePowerUp!!.title} AUTO-ACTIVE" else "NO BUFF ACTIVE RIGHT NOW",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (activePowerUp != null) Color(activePowerUp!!.primaryColorHex) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (activePowerUp != null) activePowerUp!!.subtitle else "Keep walking! Power-ups drop automatically from ? blocks.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (activePowerUp != null) {
                        Text(
                            text = String.format("%02d:%02d", powerUpRemainingSeconds / 60, powerUpRemainingSeconds % 60),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(activePowerUp!!.primaryColorHex)
                        )
                    }
                }
            }
        }

        // Live Pocket Walk Discovery Log
        item {
            Text(
                text = "TODAY'S POCKET DISCOVERIES",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (autoDropsLog.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = StarGold,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Put your phone in your pocket & start walking!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Starman sprints and power-up drops will record here automatically.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(autoDropsLog, key = { it.id }) { event ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("drop_event_${event.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            when (event.iconType) {
                                "STAR" -> SuperStarIcon(modifier = Modifier, size = 24.dp, isPulsing = false)
                                "MUSHROOM" -> SuperMushroomIcon(modifier = Modifier, size = 24.dp)
                                "FIRE" -> Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFFFF7043),
                                    modifier = Modifier.size(24.dp)
                                )
                                "ONE_UP" -> SuperMushroomIcon(modifier = Modifier, size = 24.dp, isGreen1Up = true)
                                else -> MarioCoinIcon(modifier = Modifier, size = 20.dp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.title,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = event.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = event.timeFormatted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
