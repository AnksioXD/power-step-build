package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrickBrown
import com.example.ui.theme.LuigiGreen
import com.example.ui.theme.MarioRed
import com.example.ui.theme.MarioSkyBlue
import com.example.ui.theme.PipeGreen
import com.example.ui.theme.StarGold

@Composable
fun WorldProgressBar(
    world: Int,
    stage: Int,
    stepsToday: Int,
    stageTargetSteps: Int = 1500,
    isWalking: Boolean = false,
    isGiant: Boolean = false,
    hasFirePower: Boolean = false,
    isStarInvincible: Boolean = false,
    modifier: Modifier = Modifier
) {
    val stageSteps = stepsToday % stageTargetSteps
    val stageProgress = (stageSteps.toFloat() / stageTargetSteps).coerceIn(0.02f, 0.94f)

    val animatedProgress by animateFloatAsState(
        targetValue = stageProgress,
        animationSpec = tween(durationMillis = 500),
        label = "mario_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MarioSkyBlue.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: World & Stage Label + Steps to Next Checkpoint
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WORLD $world-$stage",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${stageTargetSteps - stageSteps} steps to Flagpole",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stage Canvas with Mario running towards flagpole
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color(0xFF87CEEB).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            ) {
                val availableWidth = maxWidth

                // Ground & Decorative scenery
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = this.size.width
                    val h = this.size.height
                    val groundH = 22.dp.toPx()
                    val groundTop = h - groundH

                    // Bricks / Ground Layer
                    drawRect(
                        color = BrickBrown,
                        topLeft = Offset(0f, groundTop),
                        size = Size(w, groundH)
                    )
                    // Green grass top line
                    drawRect(
                        color = LuigiGreen,
                        topLeft = Offset(0f, groundTop),
                        size = Size(w, 4.dp.toPx())
                    )

                    // Warp Pipe checkpoint at 50%
                    val pipeX = w * 0.48f
                    val pipeW = 20.dp.toPx()
                    val pipeH = 26.dp.toPx()
                    drawRoundRect(
                        color = PipeGreen,
                        topLeft = Offset(pipeX, groundTop - pipeH),
                        size = Size(pipeW, pipeH),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                    drawRoundRect(
                        color = PipeGreen,
                        topLeft = Offset(pipeX - 2.dp.toPx(), groundTop - pipeH),
                        size = Size(pipeW + 4.dp.toPx(), 6.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )

                    // Flagpole & Castle at the end
                    val flagX = w - 24.dp.toPx()
                    val poleTop = groundTop - 48.dp.toPx()
                    // Pole
                    drawRect(
                        color = Color(0xFFD1D5DB),
                        topLeft = Offset(flagX, poleTop),
                        size = Size(3.dp.toPx(), 48.dp.toPx())
                    )
                    // Pole ball
                    drawCircle(
                        color = StarGold,
                        radius = 4.dp.toPx(),
                        center = Offset(flagX + 1.5.dp.toPx(), poleTop)
                    )
                    // Red flag
                    val flagPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(flagX, poleTop + 6.dp.toPx())
                        lineTo(flagX - 16.dp.toPx(), poleTop + 14.dp.toPx())
                        lineTo(flagX, poleTop + 22.dp.toPx())
                        close()
                    }
                    drawPath(flagPath, color = MarioRed)
                }

                // Mario Avatar walking across the stage
                val marioOffset = (availableWidth - 52.dp) * animatedProgress

                Box(
                    modifier = Modifier
                        .offset(x = marioOffset, y = 14.dp)
                ) {
                    MarioAvatar(
                        modifier = Modifier,
                        size = 46.dp,
                        isWalking = isWalking,
                        isGiant = isGiant,
                        hasFirePower = hasFirePower,
                        isStarInvincible = isStarInvincible
                    )
                }
            }
        }
    }
}
