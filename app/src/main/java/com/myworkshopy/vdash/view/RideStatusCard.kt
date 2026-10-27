package com.myworkshopy.vdash.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val RideGreen = Color(0xFF21F5A6)
private val RideCardShape = RoundedCornerShape(22.dp)

@Composable
fun RideStatusCard(
    rideState: String,
    motorOn: Boolean,
    killSwitchActive: Boolean,
    modifier: Modifier = Modifier
) {
    val stateColor = when (rideState.uppercase()) {
        "MOTOR DISABLED" -> Color(0xFFFF6464)
        "PARKED" -> Color(0xFF9BAFC5)
        else -> Color(0xFF65FF8B)
    }

    Row(
        modifier = modifier
            .height(150.dp)
            .clip(RideCardShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF06251F),
                        Color(0xFF031914),
                        Color(0xFF05251B)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00A77E),
                        Color(0xFF08724D),
                        Color(0xFF65FF8B)
                    )
                ),
                shape = RideCardShape
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Scooter icon
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Color(0xFF032019))
                .border(
                    width = 1.dp,
                    color = Color(0xFF075C45),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(70.dp)) {
                val w = size.width
                val h = size.height

                // Wheels
                drawCircle(
                    color = RideGreen,
                    radius = w * 0.13f,
                    center = androidx.compose.ui.geometry.Offset(
                        w * 0.22f, h * 0.76f
                    ),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx())
                )
                drawCircle(
                    color = RideGreen,
                    radius = w * 0.13f,
                    center = androidx.compose.ui.geometry.Offset(
                        w * 0.78f, h * 0.76f
                    ),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx())
                )

                // Scooter body and handlebar
                val scooter = Path().apply {
                    moveTo(w * 0.10f, h * 0.68f)
                    lineTo(w * 0.34f, h * 0.68f)
                    lineTo(w * 0.48f, h * 0.48f)
                    lineTo(w * 0.63f, h * 0.48f)
                    lineTo(w * 0.75f, h * 0.68f)
                    moveTo(w * 0.48f, h * 0.48f)
                    lineTo(w * 0.57f, h * 0.24f)
                    lineTo(w * 0.73f, h * 0.24f)
                    moveTo(w * 0.53f, h * 0.17f)
                    lineTo(w * 0.70f, h * 0.17f)
                }

                drawPath(
                    path = scooter,
                    color = RideGreen,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "RIDE STATE",
                color = Color(0xFF9BB7C8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = rideState.uppercase(),
                color = stateColor,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "Motor ${if (motorOn) "ON" else "OFF"}  |  Kill Switch ${
                    if (killSwitchActive) "Active" else "Inactive"
                }",
                color = Color(0xFFB4C7D6),
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}
