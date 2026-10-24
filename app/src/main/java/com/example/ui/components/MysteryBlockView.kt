package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.QuestionBlockYellow
import com.example.ui.theme.StarGold

@Composable
fun MysteryBlockCard(
    isBounced: Boolean,
    onHit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bumpOffset by animateDpAsState(
        targetValue = if (isBounced) (-16).dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 600f),
        label = "block_bump"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onHit
            )
            .testTag("mystery_question_block"),
        colors = CardDefaults.cardColors(
            containerColor = QuestionBlockYellow.copy(alpha = 0.18f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .offset(y = bumpOffset),
                contentAlignment = Alignment.Center
            ) {
                QuestionBlockIcon(size = 52.dp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "MYSTERY ? BLOCK",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        color = QuestionBlockYellow
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TAP!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = StarGold
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Tap to hit the block! Win bonus coins, 1-Ups, or surprise Mario power-ups.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
