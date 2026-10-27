package com.myworkshopy.vdash.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val MetricMuted = Color(0xFF94A9BF)
private val MetricGreen = Color(0xFF60F579)
private enum class MetricSymbol { ROAD, BATTERY, RANGE, TEMPERATURE }

@Composable
fun MetricsBar(
    odometerKm: Int,
    batteryPercent: Int,
    rangeKm: Int,
    temperatureC: Int,
    modifier: Modifier = Modifier
) {
    val charge = batteryPercent.coerceIn(0, 100)
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier.height(96.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xED030D16), Color(0xE50A1D2C), Color(0xED030D16))), shape)
            .border(0.8.dp, Color(0xFF244258), shape)
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MetricItem(MetricSymbol.ROAD, "ODO",
            NumberFormat.getIntegerInstance(Locale.US).format(odometerKm), "km", Modifier.weight(1f))
        MetricDivider()
        Row(Modifier.weight(1.4f).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically) {
            MetricIcon(MetricSymbol.BATTERY, MetricGreen)
            Spacer(Modifier.width(16.dp))
            MetricText("BATTERY", charge.toString(), "%")
            Spacer(Modifier.width(14.dp))
            Box(Modifier.weight(1f).height(13.dp)
                .background(Color(0xFF1B3041), RoundedCornerShape(8.dp))) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(charge / 100f)
                    .background(Brush.horizontalGradient(listOf(MetricGreen, Color(0xFF39EB60))), RoundedCornerShape(8.dp)))
            }
        }
        MetricDivider()
        MetricItem(MetricSymbol.RANGE, "RANGE (EST.)", rangeKm.toString(), "km",
            Modifier.weight(1.05f).padding(start = 26.dp))
        MetricDivider()
        MetricItem(MetricSymbol.TEMPERATURE, "TEMP", temperatureC.toString(), "°C",
            Modifier.weight(0.75f).padding(start = 26.dp))
    }
}

@Composable
private fun MetricDivider() {
    Box(Modifier.width(1.dp).height(48.dp).background(Color(0xFF223D50)))
}

@Composable
private fun MetricItem(symbol: MetricSymbol, label: String, value: String, unit: String, modifier: Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        MetricIcon(symbol, MetricMuted)
        Spacer(Modifier.width(16.dp))
        MetricText(label, value, unit)
    }
}

@Composable
private fun MetricText(label: String, value: String, unit: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = MetricMuted, fontSize = 12.sp, maxLines = 1)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = Color(0xFFF0F5F9), fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text(" $unit", color = Color(0xFFBDCAD7), fontSize = 19.sp)
        }
    }
}

// Vector-drawn icons stay sharp and do not depend on emoji or device fonts.
@Composable
private fun MetricIcon(symbol: MetricSymbol, color: Color) {
    Canvas(Modifier.size(36.dp, 40.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
        when (symbol) {
            MetricSymbol.ROAD -> {
                val left = Path().apply {
                    moveTo(w * 0.08f, h * 0.9f)
                    cubicTo(w * 0.12f, h * 0.5f, w * 0.9f, h * 0.4f, w * 0.55f, h * 0.1f)
                }
                val right = Path().apply {
                    moveTo(w * 0.94f, h * 0.9f)
                    cubicTo(w * 0.3f, h * 0.55f, w * 1.05f, h * 0.4f, w * 0.68f, h * 0.1f)
                }
                drawPath(left, color, style = stroke)
                drawPath(right, color, style = stroke)
                drawLine(color, Offset(w * 0.4f, h * 0.83f), Offset(w * 0.39f, h * 0.68f), stroke.width)
                drawLine(color, Offset(w * 0.45f, h * 0.55f), Offset(w * 0.55f, h * 0.43f), stroke.width)
            }
            MetricSymbol.BATTERY -> {
                drawRect(color, Offset(w * 0.35f, h * 0.04f), Size(w * 0.3f, h * 0.08f))
                drawRoundRect(color, Offset(w * 0.22f, h * 0.15f), Size(w * 0.56f, h * 0.79f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()), style = stroke)
                repeat(3) { index ->
                    drawRect(color, Offset(w * 0.32f, h * (0.24f + index * 0.21f)), Size(w * 0.36f, h * 0.17f))
                }
            }
            MetricSymbol.RANGE -> {
                val center = Offset(w / 2, h / 2)
                drawCircle(color, w * 0.43f, center, style = stroke)
                drawCircle(color, w * 0.27f, center, style = stroke)
                drawCircle(color.copy(alpha = 0.3f), w * 0.1f, center)
            }
            MetricSymbol.TEMPERATURE -> {
                val outline = Path().apply {
                    moveTo(w * 0.35f, h * 0.64f)
                    lineTo(w * 0.35f, h * 0.16f)
                    cubicTo(w * 0.35f, h * 0.02f, w * 0.65f, h * 0.02f, w * 0.65f, h * 0.16f)
                    lineTo(w * 0.65f, h * 0.64f)
                }
                drawPath(outline, color, style = stroke)
                drawCircle(color, w * 0.24f, Offset(w / 2, h * 0.78f), style = stroke)
                drawCircle(color, w * 0.1f, Offset(w / 2, h * 0.78f))
                drawLine(color, Offset(w / 2, h * 0.35f), Offset(w / 2, h * 0.78f), stroke.width, StrokeCap.Round)
                repeat(3) { index ->
                    val y = h * (0.22f + index * 0.14f)
                    drawLine(color, Offset(w * 0.79f, y), Offset(w * 0.9f, y), stroke.width)
                }
            }
        }
    }
}
