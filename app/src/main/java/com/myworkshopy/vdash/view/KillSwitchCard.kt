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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NeonBlue = Color(0xFF20BFFF)
private val CardShape = RoundedCornerShape(22.dp)


@Preview
@Composable
private fun KillSwitchCardPreview() {
    KillSwitchCard(isActive = false)
}

@Composable
fun KillSwitchCard(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val statusColor = if (isActive) {
        Color(0xFFFF6464)
    } else {
        NeonBlue
    }

    Row(
        modifier = modifier
            .height(150.dp)
            .clip(CardShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF061727),
                        Color(0xFF03101D),
                        Color(0xFF04182A)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF168FE0),
                        Color(0xFF07518A),
                        Color(0xFF278FD6)
                    )
                ),
                shape = CardShape
            )
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xFF031321))
                .border(
                    width = 1.5.dp,
                    color = Color(0xFF0873B9),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(48.dp)
            ) {
                val shield = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.05f)
                    lineTo(size.width * 0.83f, size.height * 0.20f)
                    lineTo(size.width * 0.83f, size.height * 0.52f)
                    quadraticTo(
                        size.width * 0.80f,
                        size.height * 0.77f,
                        size.width * 0.5f,
                        size.height * 0.96f
                    )
                    quadraticTo(
                        size.width * 0.20f,
                        size.height * 0.77f,
                        size.width * 0.17f,
                        size.height * 0.52f
                    )
                    lineTo(size.width * 0.17f, size.height * 0.20f)
                    close()
                }

                drawPath(
                    path = shield,
                    color = statusColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3.dp.toPx()
                    )
                )
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "KILL SWITCH",
                color = Color(0xFF9BB7D0),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = if (isActive) "ACTIVE" else "INACTIVE",
                color = statusColor,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                maxLines = 1
            )
        }
    }
}
