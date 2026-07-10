package com.tvcostabrava.intercom

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Puntito decorativo de esquina (equivalente al "radio_button_checked" opacity-40 del mockup). */
@Composable
fun CornerDot(color: Color = Color.White, size: Dp = 8.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        drawCircle(
            color = color.copy(alpha = 0.4f),
            radius = this.size.minDimension / 2f - this.size.minDimension * 0.1f,
            style = Stroke(width = this.size.minDimension * 0.15f),
        )
    }
}

/** Icono decorativo de tornillo ("settings_b_roll") para las esquinas del chasis. */
@Composable
fun ScrewIcon(color: Color = Color.White, size: Dp = 14.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val r = this.size.minDimension / 2f
        drawCircle(color = color.copy(alpha = 0.3f), radius = r, style = Stroke(width = r * 0.3f))
        drawLine(
            color = color.copy(alpha = 0.3f),
            start = Offset(this.size.width * 0.25f, this.size.height * 0.5f),
            end = Offset(this.size.width * 0.75f, this.size.height * 0.5f),
            strokeWidth = r * 0.3f,
        )
    }
}

/** Icono de engranaje para la pestana SETTINGS. */
@Composable
fun GearIcon(color: Color, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w * 0.5f
        val cy = h * 0.5f
        val outerR = w * 0.42f
        val innerR = w * 0.18f
        val stroke = Stroke(width = w * 0.1f)

        repeat(6) { i ->
            val angle = Math.toRadians((i * 60).toDouble())
            val toothLen = w * 0.14f
            val x1 = cx + (outerR * kotlin.math.cos(angle)).toFloat()
            val y1 = cy + (outerR * kotlin.math.sin(angle)).toFloat()
            val x2 = cx + ((outerR + toothLen) * kotlin.math.cos(angle)).toFloat()
            val y2 = cy + ((outerR + toothLen) * kotlin.math.sin(angle)).toFloat()
            drawLine(color, Offset(x1, y1), Offset(x2, y2), strokeWidth = stroke.width)
        }
        drawCircle(color, radius = outerR, center = Offset(cx, cy), style = stroke)
        drawCircle(color, radius = innerR, center = Offset(cx, cy))
    }
}

/**
 * Iconos dibujados a mano con Canvas (sin depender de material-icons-extended)
 * para el look "radio industrial 80s" del diseño Stitch.
 */

@Composable
fun AntennaIcon(color: Color, size: Dp = 18.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = size.toPx() * 0.09f)
        drawLine(color, Offset(this.size.width * 0.5f, 0f), Offset(this.size.width * 0.5f, this.size.height * 0.55f), stroke.width)
        val cx = this.size.width * 0.5f
        val cy = this.size.height * 0.55f
        val r1 = this.size.width * 0.28f
        val r2 = this.size.width * 0.46f
        drawArc(color, 200f, 140f, false, topLeft = Offset(cx - r1, cy - r1), size = androidx.compose.ui.geometry.Size(r1 * 2, r1 * 2), style = stroke)
        drawArc(color, 200f, 140f, false, topLeft = Offset(cx - r2, cy - r2), size = androidx.compose.ui.geometry.Size(r2 * 2, r2 * 2), style = stroke)
    }
}

@Composable
fun BatteryIcon(color: Color, size: Dp = 18.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = this.size.minDimension * 0.1f)
        val bodyWidth = this.size.width * 0.8f
        val bodyHeight = this.size.height * 0.55f
        val left = (this.size.width - bodyWidth) / 2f
        val top = (this.size.height - bodyHeight) / 2f
        drawRoundRect(
            color = color,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(bodyWidth, bodyHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(this.size.width * 0.08f),
            style = stroke,
        )
        // nub del polo positivo
        drawRoundRect(
            color = color,
            topLeft = Offset(left + bodyWidth, top + bodyHeight * 0.3f),
            size = androidx.compose.ui.geometry.Size(this.size.width * 0.08f, bodyHeight * 0.4f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f),
        )
        // nivel de carga
        drawRoundRect(
            color = color,
            topLeft = Offset(left + bodyWidth * 0.15f, top + bodyHeight * 0.2f),
            size = androidx.compose.ui.geometry.Size(bodyWidth * 0.55f, bodyHeight * 0.6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(this.size.width * 0.04f),
        )
    }
}

@Composable
fun MicIcon(color: Color, size: Dp = 28.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = w * 0.09f)
        // capsula del microfono
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.35f, h * 0.05f),
            size = androidx.compose.ui.geometry.Size(w * 0.3f, h * 0.45f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.15f),
        )
        // soporte en U
        val r = w * 0.28f
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.5f - r, h * 0.35f),
            size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
            style = stroke,
        )
        // pie
        drawLine(color, Offset(w * 0.5f, h * 0.63f), Offset(w * 0.5f, h * 0.85f), stroke.width)
        drawLine(color, Offset(w * 0.32f, h * 0.85f), Offset(w * 0.68f, h * 0.85f), stroke.width)
    }
}

@Composable
fun HeadsetIcon(color: Color, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = w * 0.1f)
        val r = w * 0.4f
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.5f - r, h * 0.05f),
            size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
            style = stroke,
        )
        drawCircle(color, radius = w * 0.13f, center = Offset(w * 0.5f - r, h * 0.55f))
        drawCircle(color, radius = w * 0.13f, center = Offset(w * 0.5f + r, h * 0.55f))
    }
}

@Composable
fun TuneIcon(color: Color, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = w * 0.09f)
        drawLine(color, Offset(w * 0.1f, h * 0.3f), Offset(w * 0.9f, h * 0.3f), stroke.width)
        drawLine(color, Offset(w * 0.1f, h * 0.7f), Offset(w * 0.9f, h * 0.7f), stroke.width)
        drawCircle(color, radius = w * 0.09f, center = Offset(w * 0.35f, h * 0.3f))
        drawCircle(color, radius = w * 0.09f, center = Offset(w * 0.65f, h * 0.7f))
    }
}

@Composable
fun HistoryIcon(color: Color, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = w * 0.09f)
        val r = w * 0.4f
        drawCircle(color, radius = r, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
        drawLine(color, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.5f, h * 0.25f), stroke.width)
        drawLine(color, Offset(w * 0.5f, h * 0.5f), Offset(w * 0.68f, h * 0.58f), stroke.width)
    }
}

@Composable
fun ChannelsIcon(color: Color, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = w * 0.1f)
        val barWidth = w * 0.16f
        val gap = w * 0.12f
        val heights = listOf(0.35f, 0.55f, 0.8f, 0.5f)
        heights.forEachIndexed { i, hf ->
            val x = w * 0.08f + i * (barWidth + gap)
            drawLine(
                color,
                Offset(x, h * (1f - hf)),
                Offset(x, h),
                strokeWidth = stroke.width,
            )
        }
    }
}

@Composable
fun RadioIcon(color: Color, size: Dp = 20.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = w * 0.09f)
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.12f, h * 0.35f),
            size = androidx.compose.ui.geometry.Size(w * 0.76f, h * 0.55f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f),
            style = stroke,
        )
        drawCircle(color, radius = w * 0.1f, center = Offset(w * 0.5f, h * 0.62f), style = stroke)
        drawLine(color, Offset(w * 0.32f, h * 0.35f), Offset(w * 0.2f, h * 0.1f), stroke.width)
        drawLine(color, Offset(w * 0.68f, h * 0.35f), Offset(w * 0.8f, h * 0.1f), stroke.width)
    }
}
