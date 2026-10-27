package com.myworkshopy.vdash.view

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NeonGreen = Color(0xFF50FF79)
private val CardShape = RoundedCornerShape(22.dp)

@Preview
@Composable
private fun MotorStatusCardPreview() {
    MotorStatusCard(isMotorOn = true)
}

@Composable
fun MotorStatusCard(
    isMotorOn: Boolean,
    modifier: Modifier = Modifier
) {
    val statusColor =
        if (isMotorOn) NeonGreen else Color(0xFFFF6464)

    Row(
        modifier = modifier
            .height(150.dp)
            .clip(CardShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF06201D),
                        Color(0xFF001311),
                        Color(0xFF031A18)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00C89B),
                        Color(0xFF08704F),
                        Color(0xFF00A67C)
                    )
                ),
                shape = CardShape
            )
            .padding(horizontal = 19.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular lightning icon
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xFF031A13))
                .border(
                    width = 1.5.dp,
                    color = Color(0xFF087B37),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isMotorOn) {
                Canvas(
                    modifier = Modifier.size(52.dp)
                ) {
                    val bolt = Path().apply {
                        moveTo(size.width * 0.58f, 0f)
                        lineTo(size.width * 0.12f, size.height * 0.54f)
                        lineTo(size.width * 0.43f, size.height * 0.54f)
                        lineTo(size.width * 0.34f, size.height)
                        lineTo(size.width * 0.88f, size.height * 0.37f)
                        lineTo(size.width * 0.56f, size.height * 0.37f)
                        close()
                    }

                    drawPath(
                        path = bolt,
                        color = NeonGreen
                    )
                }
            } else {
                Text(
                    text = "×",
                    color = statusColor,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "MOTOR STATUS",
                color = Color(0xFF9BB7C8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = if (isMotorOn) "ON" else "OFF",
                color = statusColor,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
