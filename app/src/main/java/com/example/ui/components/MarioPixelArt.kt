package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LuigiGreen
import com.example.ui.theme.MarioRed
import com.example.ui.theme.QuestionBlockYellow
import com.example.ui.theme.StarGold
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural Mario-inspired retro pixel-art characters and items drawn via Compose Canvas
 */

@Composable
fun MarioAvatar(
    modifier: Modifier? = null,
    size: Dp = 48.dp,
    isWalking: Boolean = false,
    isGiant: Boolean = false,
    hasFirePower: Boolean = false,
    isStarInvincible: Boolean = false
) {
    val actualModifier = modifier ?: Modifier
    val infiniteTransition = rememberInfiniteTransition(label = "mario_anim")
    val walkFrame by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 280, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "walk_step"
    )

    val starRainbowShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing)
        ),
        label = "star_rainbow"
    )

    val displaySize = if (isGiant) size * 1.35f else size

    Canvas(modifier = actualModifier.size(displaySize)) {
        val w = this.size.width
        val h = this.size.height

        // Colors
        val hatShirtColor = when {
            isStarInvincible -> Color.hsv(starRainbowShift % 360f, 0.85f, 1f)
            hasFirePower -> Color.White
            else -> MarioRed
        }
        val overallsColor = when {
            isStarInvincible -> Color.hsv((starRainbowShift + 120f) % 360f, 0.85f, 1f)
            hasFirePower -> MarioRed
            else -> Color(0xFF002FBE)
        }
        val skinColor = Color(0xFFFFCC80)
        val hairMoustacheColor = Color(0xFF5D4037)
        val shoesColor = Color(0xFF6D4C41)
        val buttonColor = StarGold

        // 1. Red Hat (top)
        drawRoundRect(
            color = hatShirtColor,
            topLeft = Offset(w * 0.2f, h * 0.08f),
            size = Size(w * 0.65f, h * 0.2f),
            cornerRadius = CornerRadius(w * 0.08f, h * 0.08f)
        )
        // Hat brim
        drawRect(
            color = hatShirtColor,
            topLeft = Offset(w * 0.35f, h * 0.22f),
            size = Size(w * 0.55f, h * 0.08f)
        )

        // 2. Face / Head
        drawRoundRect(
            color = skinColor,
            topLeft = Offset(w * 0.25f, h * 0.24f),
            size = Size(w * 0.5f, h * 0.28f),
            cornerRadius = CornerRadius(w * 0.06f, h * 0.06f)
        )

        // Hair (back)
        drawRect(
            color = hairMoustacheColor,
            topLeft = Offset(w * 0.18f, h * 0.26f),
            size = Size(w * 0.12f, h * 0.22f)
        )

        // Eye
        drawOval(
            color = Color.Black,
            topLeft = Offset(w * 0.58f, h * 0.3f),
            size = Size(w * 0.08f, h * 0.12f)
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.02f,
            center = Offset(w * 0.60f, h * 0.33f)
        )

        // Big Mario Moustache
        drawRoundRect(
            color = hairMoustacheColor,
            topLeft = Offset(w * 0.45f, h * 0.42f),
            size = Size(w * 0.4f, h * 0.1f),
            cornerRadius = CornerRadius(w * 0.04f, h * 0.04f)
        )
        // Nose
        drawOval(
            color = skinColor,
            topLeft = Offset(w * 0.64f, h * 0.36f),
            size = Size(w * 0.22f, h * 0.14f)
        )

        // 3. Torso / Overalls
        // Red / White Shirt Base
        drawRoundRect(
            color = hatShirtColor,
            topLeft = Offset(w * 0.2f, h * 0.52f),
            size = Size(w * 0.6f, h * 0.26f),
            cornerRadius = CornerRadius(w * 0.05f, h * 0.05f)
        )
        // Blue Overalls
        drawRoundRect(
            color = overallsColor,
            topLeft = Offset(w * 0.26f, h * 0.56f),
            size = Size(w * 0.48f, h * 0.24f),
            cornerRadius = CornerRadius(w * 0.04f, h * 0.04f)
        )
        // Yellow Suspender Buttons
        drawCircle(
            color = buttonColor,
            radius = w * 0.035f,
            center = Offset(w * 0.34f, h * 0.62f)
        )
        drawCircle(
            color = buttonColor,
            radius = w * 0.035f,
            center = Offset(w * 0.56f, h * 0.62f)
        )

        // 4. Legs / Running Shoes (Animated step cycle)
        val legOffsetLeft = if (isWalking) (walkFrame - 1f) * (w * 0.08f) else 0f
        val legOffsetRight = if (isWalking) (1f - walkFrame) * (w * 0.08f) else 0f

        // Left Leg & Shoe
        drawRect(
            color = overallsColor,
            topLeft = Offset(w * 0.26f + legOffsetLeft, h * 0.78f),
            size = Size(w * 0.2f, h * 0.12f)
        )
        drawRoundRect(
            color = shoesColor,
            topLeft = Offset(w * 0.18f + legOffsetLeft, h * 0.86f),
            size = Size(w * 0.28f, h * 0.12f),
            cornerRadius = CornerRadius(w * 0.04f, h * 0.04f)
        )

        // Right Leg & Shoe
        drawRect(
            color = overallsColor,
            topLeft = Offset(w * 0.54f + legOffsetRight, h * 0.78f),
            size = Size(w * 0.2f, h * 0.12f)
        )
        drawRoundRect(
            color = shoesColor,
            topLeft = Offset(w * 0.52f + legOffsetRight, h * 0.86f),
            size = Size(w * 0.32f, h * 0.12f),
            cornerRadius = CornerRadius(w * 0.04f, h * 0.04f)
        )

        // White Gloves (Hands)
        drawCircle(
            color = Color.White,
            radius = w * 0.08f,
            center = Offset(w * 0.16f - legOffsetLeft * 0.5f, h * 0.66f)
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.08f,
            center = Offset(w * 0.82f - legOffsetRight * 0.5f, h * 0.66f)
        )
    }
}

@Composable
fun SuperStarIcon(
    modifier: Modifier? = null,
    size: Dp = 36.dp,
    isPulsing: Boolean = true
) {
    val actualModifier = modifier ?: Modifier
    val infiniteTransition = rememberInfiniteTransition(label = "star_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (isPulsing) 0.95f else 1f,
        targetValue = if (isPulsing) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = actualModifier.size(size * pulseScale)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val rOuter = w * 0.46f
        val rInner = w * 0.21f

        val path = Path()
        val numPoints = 5
        var angle = -Math.PI / 2.0
        val step = Math.PI / numPoints

        for (i in 0 until numPoints * 2) {
            val r = if (i % 2 == 0) rOuter else rInner
            val px = (cx + cos(angle) * r).toFloat()
            val py = (cy + sin(angle) * r).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            angle += step
        }
        path.close()

        // Outer Dark Outline
        drawPath(path = path, color = Color(0xFF4A3800))
        // Inner Gold Body
        val innerPath = Path()
        angle = -Math.PI / 2.0
        for (i in 0 until numPoints * 2) {
            val r = (if (i % 2 == 0) rOuter else rInner) * 0.92f
            val px = (cx + cos(angle) * r).toFloat()
            val py = (cy + sin(angle) * r).toFloat()
            if (i == 0) innerPath.moveTo(px, py) else innerPath.lineTo(px, py)
            angle += step
        }
        innerPath.close()
        drawPath(path = innerPath, color = StarGold)

        // Cute vertical Mario Star Eyes
        drawOval(
            color = Color.Black,
            topLeft = Offset(cx - w * 0.14f, cy - h * 0.12f),
            size = Size(w * 0.08f, h * 0.24f)
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.02f,
            center = Offset(cx - w * 0.12f, cy - h * 0.06f)
        )

        drawOval(
            color = Color.Black,
            topLeft = Offset(cx + w * 0.06f, cy - h * 0.12f),
            size = Size(w * 0.08f, h * 0.24f)
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.02f,
            center = Offset(cx + w * 0.08f, cy - h * 0.06f)
        )
    }
}

@Composable
fun SuperMushroomIcon(
    modifier: Modifier? = null,
    size: Dp = 36.dp,
    isGreen1Up: Boolean = false
) {
    val actualModifier = modifier ?: Modifier
    Canvas(modifier = actualModifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val capColor = if (isGreen1Up) LuigiGreen else MarioRed

        // Stem (beige)
        drawRoundRect(
            color = Color(0xFFFFECB3),
            topLeft = Offset(w * 0.28f, h * 0.52f),
            size = Size(w * 0.44f, h * 0.42f),
            cornerRadius = CornerRadius(w * 0.1f, h * 0.1f)
        )
        // Stem eyes
        drawOval(
            color = Color.Black,
            topLeft = Offset(w * 0.36f, h * 0.62f),
            size = Size(w * 0.06f, h * 0.16f)
        )
        drawOval(
            color = Color.Black,
            topLeft = Offset(w * 0.58f, h * 0.62f),
            size = Size(w * 0.06f, h * 0.16f)
        )

        // Cap (Red or Green Dome)
        drawRoundRect(
            color = capColor,
            topLeft = Offset(w * 0.08f, h * 0.1f),
            size = Size(w * 0.84f, h * 0.54f),
            cornerRadius = CornerRadius(w * 0.4f, h * 0.3f)
        )
        // White Polka Dots on Cap
        drawCircle(
            color = Color.White,
            radius = w * 0.14f,
            center = Offset(w * 0.5f, h * 0.32f)
        )
        drawOval(
            color = Color.White,
            topLeft = Offset(w * 0.08f, h * 0.26f),
            size = Size(w * 0.14f, h * 0.22f)
        )
        drawOval(
            color = Color.White,
            topLeft = Offset(w * 0.78f, h * 0.26f),
            size = Size(w * 0.14f, h * 0.22f)
        )
    }
}

@Composable
fun MarioCoinIcon(
    modifier: Modifier? = null,
    size: Dp = 28.dp
) {
    val actualModifier = modifier ?: Modifier
    val infiniteTransition = rememberInfiniteTransition(label = "coin_spin")
    val spinWidthFactor by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coin_spin_anim"
    )

    Canvas(modifier = actualModifier.size(size)) {
        val w = this.size.width * spinWidthFactor
        val h = this.size.height
        val left = (this.size.width - w) / 2f

        // Outer Dark Golden Rim
        drawOval(
            color = Color(0xFFC67D00),
            topLeft = Offset(left, h * 0.05f),
            size = Size(w, h * 0.9f)
        )
        // Bright Yellow Core
        drawOval(
            color = StarGold,
            topLeft = Offset(left + w * 0.1f, h * 0.12f),
            size = Size(w * 0.8f, h * 0.76f)
        )
        // Inner Embossed Line
        drawRoundRect(
            color = Color(0xFFFFEA00),
            topLeft = Offset(left + w * 0.4f, h * 0.25f),
            size = Size(w * 0.2f, h * 0.5f),
            cornerRadius = CornerRadius(w * 0.05f, h * 0.05f)
        )
    }
}

@Composable
fun QuestionBlockIcon(
    modifier: Modifier? = null,
    size: Dp = 44.dp
) {
    val actualModifier = modifier ?: Modifier
    Canvas(modifier = actualModifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Outer Brown Border
        drawRoundRect(
            color = Color(0xFF8B4513),
            topLeft = Offset.Zero,
            size = Size(w, h),
            cornerRadius = CornerRadius(w * 0.1f, h * 0.1f)
        )
        // Inner Yellow Face
        drawRoundRect(
            color = QuestionBlockYellow,
            topLeft = Offset(w * 0.08f, h * 0.08f),
            size = Size(w * 0.84f, h * 0.84f),
            cornerRadius = CornerRadius(w * 0.06f, h * 0.06f)
        )
        // 4 Corner Screws / Rivets
        val rivetColor = Color(0xFF5D2E0C)
        val r = w * 0.04f
        drawCircle(rivetColor, r, Offset(w * 0.18f, h * 0.18f))
        drawCircle(rivetColor, r, Offset(w * 0.82f, h * 0.18f))
        drawCircle(rivetColor, r, Offset(w * 0.18f, h * 0.82f))
        drawCircle(rivetColor, r, Offset(w * 0.82f, h * 0.82f))

        // Question Mark '?'
        // Top hook
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.36f, h * 0.24f),
            size = Size(w * 0.28f, h * 0.12f),
            cornerRadius = CornerRadius(w * 0.04f, h * 0.04f)
        )
        drawRect(
            color = Color.White,
            topLeft = Offset(w * 0.52f, h * 0.34f),
            size = Size(w * 0.12f, h * 0.18f)
        )
        drawRect(
            color = Color.White,
            topLeft = Offset(w * 0.42f, h * 0.48f),
            size = Size(w * 0.16f, h * 0.12f)
        )
        // Bottom Dot
        drawCircle(
            color = Color.White,
            radius = w * 0.065f,
            center = Offset(w * 0.5f, h * 0.72f)
        )
    }
}
