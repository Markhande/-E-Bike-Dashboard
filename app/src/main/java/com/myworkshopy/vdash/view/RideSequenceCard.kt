package com.myworkshopy.vdash.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SequenceBlue = Color(0xFF20BFFF)
private val SequenceGreen = Color(0xFF65FF78)
private val SequenceGrey = Color(0xFF9BAFC5)
private val rideStages = listOf("Parked", "Ready", "Riding", "Accelerating", "Motor\nDisabled")

@Composable
fun RideSequenceCard(currentStage: Int, modifier: Modifier = Modifier) {
    val active = currentStage.coerceIn(0, rideStages.lastIndex)
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier.height(150.dp).clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xE6102235), Color(0xE603101C))))
            .border(0.8.dp, Color(0xFF2585B9), shape)
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Text("NEXT IN SEQUENCE", color = SequenceGrey, fontSize = 14.sp,
            modifier = Modifier.padding(start = 8.dp))
        Spacer(Modifier.weight(1f))
        Canvas(Modifier.fillMaxWidth().height(28.dp)) {
            val step = size.width / rideStages.size
            val y = size.height / 2
            for (index in 0 until rideStages.lastIndex) {
                val color = when {
                    index == active - 1 -> SequenceGreen
                    index < active -> SequenceBlue
                    else -> SequenceGrey.copy(alpha = 0.45f)
                }
                drawLine(color, Offset(step * (index + 0.5f), y),
                    Offset(step * (index + 1.5f), y), 2.dp.toPx(), StrokeCap.Round)
            }
            rideStages.indices.forEach { index ->
                val center = Offset(step * (index + 0.5f), y)
                val color = when {
                    index == active -> SequenceGreen
                    index < active -> SequenceBlue
                    else -> SequenceGrey
                }
                if (index == active) drawCircle(color.copy(alpha = 0.15f), 14.dp.toPx(), center)
                drawCircle(Color(0xFF071827), 10.dp.toPx(), center)
                drawCircle(color, 10.dp.toPx(), center, style = Stroke(2.dp.toPx()))
                if (index == active) drawCircle(color, 6.dp.toPx(), center)
            }
        }
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth()) {
            rideStages.forEachIndexed { index, label ->
                Text(label, modifier = Modifier.weight(1f),
                    color = if (index == active) SequenceGreen else Color(0xFFB7CADB),
                    fontSize = 10.sp, lineHeight = 14.sp, textAlign = TextAlign.Center,
                    maxLines = 2)
            }
        }
    }
}
