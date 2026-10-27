package com.myworkshopy.vdash.view

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin


@Preview
@Composable
private fun SpeedoMeterPreview() {
    Speedometer(
        speed = 110,
        modifier = Modifier.size(140.dp)
    )
}

@Composable
fun Speedometer(
    speed: Int,
    modifier: Modifier = Modifier,
    maxSpeed: Int = 120
) {
    val safeSpeed = speed.coerceIn(0, maxSpeed)
    val animatedSpeed by animateFloatAsState(
        targetValue = safeSpeed.toFloat(),
        label = "speed_animation"
    )
    val progress = animatedSpeed / maxSpeed.toFloat()

    // Colors sampled from the reference
    val bgTop = Color(0xFF0B1626)
    val bgBottom = Color(0xFF060D18)
    val bezelColor = Color(0xFF1E3350)
    val innerRingColor = Color(0xFF1A2A42)
    val trackColor = Color(0xFF14233A)
    val minorTick = Color(0xFF6C7C93)
    val majorTick = Color(0xFFF4F7FC)
    val labelColor = Color(0xFFE6ECF5)
    val textColor = Color(0xFFF4F7FC)
    val unitColor = Color(0xFF9BAFC5)

    val gradient = listOf(
        Color(0xFF16BFFF), // Cyan-blue
        Color(0xFF16F5AD), // Green
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFFF0018), // Orange
    )

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        val widthPx = with(density) { maxWidth.toPx() }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val center = Offset(w / 2f, size.height / 2f)

            val startAngle = 155f
            val totalSweep = 230f

            // Outer bezel (two soft rings)
            drawCircle(
                color = bezelColor.copy(alpha = 0.55f),
                radius = w * 0.475f,
                center = center,
                style = Stroke(width = w * 0.006f)
            )
            drawCircle(
                color = bezelColor.copy(alpha = 0.30f),
                radius = w * 0.455f,
                center = center,
                style = Stroke(width = w * 0.004f)
            )

            // Inner circle
            drawCircle(
                color = Color(0xFF0A1424),
                radius = w * 0.285f,
                center = center
            )
            drawCircle(
                color = innerRingColor,
                radius = w * 0.285f,
                center = center,
                style = Stroke(width = w * 0.004f)
            )

            // Arc geometry
            val arcRadius = w * 0.43f
            val strokeWidth = w * 0.03f
            val arcTopLeft = Offset(center.x - arcRadius, center.y - arcRadius)
            val arcSize = Size(arcRadius * 2, arcRadius * 2)

            // Background track
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active gradient arc (sweep gradient is rotated so it starts at 135°)
            if (progress > 0f) {
                rotate(degrees = startAngle, pivot = center) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colorStops = arrayOf(
                                0f to gradient[0],
                                0.14f to gradient[1],
                                0.28f to gradient[2],
                                0.75f to gradient[3],
                                0f to gradient[0]
                            ),
                            center = center
                        ),
                        startAngle = 0f,
                        sweepAngle = totalSweep * progress,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // Ticks + labels
            val tickOuter = w * 0.395f
            val minorInner = w * 0.378f
            val majorInner = w * 0.379f
            val labelRadius = w * 0.335f
            val step = 4
            val labelStyle = TextStyle(
                color = labelColor,
                fontSize = with(density) { (w * 0.042f).toSp() },
                fontWeight = FontWeight.Medium
            )

            for (value in 0..maxSpeed step step) {
                val isMajor = value % 20 == 0
                val angle = Math.toRadians(
                    (startAngle + value / maxSpeed.toFloat() * totalSweep).toDouble()
                )
                val c = cos(angle).toFloat()
                val s = sin(angle).toFloat()
                val inner = if (isMajor) majorInner else minorInner

                drawLine(
                    color = if (isMajor) majorTick else minorTick,
                    start = Offset(center.x + c * inner, center.y + s * inner),
                    end = Offset(center.x + c * tickOuter, center.y + s * tickOuter),
                    strokeWidth = if (isMajor) w * 0.007f else w * 0.0035f,
                    cap = StrokeCap.Round
                )

                if (isMajor) {
                    val layout = textMeasurer.measure(value.toString(), labelStyle)
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(
                            center.x + c * labelRadius - layout.size.width / 2f,
                            center.y + s * labelRadius - layout.size.height / 2f
                        )
                    )
                }
            }
        }

        // Center readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = with(density) { (widthPx * 0.01f).toDp() })
        ) {
            Text(
                text = safeSpeed.toString(),
                color = textColor,
                fontSize = with(density) { (widthPx * 0.26f).toSp() },
                fontWeight = FontWeight.ExtraBold,
                lineHeight = with(density) { (widthPx * 0.27f).toSp() }
            )
            Text(
                text = "km/h",
                color = unitColor,
                fontSize = with(density) { (widthPx * 0.058f).toSp() },
                fontWeight = FontWeight.Normal
            )
        }
    }
}

