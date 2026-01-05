package com.jinbo.smartsleep.ui

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.jinbo.smartsleep.ui.theme.GraphLine
import com.jinbo.smartsleep.ui.theme.SnoreHigh
import com.jinbo.smartsleep.ui.theme.SnoreLow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enhanced AmplitudeGraph with bezier smoothing and gradient fill
 */
@Composable
fun AmplitudeGraph(
    amplitudes: List<Pair<Long, Float>>, // Time, Amplitude
    modifier: Modifier = Modifier,
    threshold: Float
) {
    // Get colors outside of Canvas
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Pulse animation for threshold line
    val infiniteTransition = rememberInfiniteTransition(label = "threshold_pulse")
    val thresholdAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "threshold_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(surfaceColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (amplitudes.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height
            val paddingBottom = 40f
            val graphHeight = height - paddingBottom

            // Y-Axis Scaling: Display range 0 to 1.2 * threshold
            // Prevent NaN by ensuring threshold is valid
            val maxY = (threshold * 1.2f).coerceAtLeast(1f)

            val stepX = width / (amplitudes.size - 1).coerceAtLeast(1)

            // Calculate all points
            val points = amplitudes.mapIndexed { index, pair ->
                val amplitude = pair.second
                val x = index * stepX
                // Prevent NaN by validating amplitude before division
                val normalizedAmplitude = if (amplitude.isNaN()) 0f else amplitude
                val y = graphHeight - (normalizedAmplitude / maxY * graphHeight).coerceIn(0f, graphHeight)
                Offset(x, y)
            }

            // Determine color based on last amplitude
            val lastAmplitude = amplitudes.lastOrNull()?.second ?: 0f
            val lineColor = if (lastAmplitude >= threshold) SnoreHigh else GraphLine

            // 1. Draw gradient fill under the curve
            if (points.size > 1) {
                val fillPath = Path().apply {
                    moveTo(points.first().x, graphHeight)
                    points.forEach { point ->
                        lineTo(point.x, point.y)
                    }
                    lineTo(points.last().x, graphHeight)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.3f),
                            lineColor.copy(alpha = 0.05f)
                        ),
                        startY = 0f,
                        endY = graphHeight
                    )
                )
            }

            // 2. Draw smooth bezier curve
            if (points.size > 1) {
                val smoothPath = createSmoothPath(points)
                drawPath(
                    path = smoothPath,
                    color = lineColor,
                    style = Stroke(width = 4f)
                )
            }

            // 3. Draw threshold line with pulse animation
            // Prevent NaN by validating threshold
            val safeThreshold = if (threshold.isNaN()) 800f else threshold
            val thresholdY = graphHeight - (safeThreshold / maxY * graphHeight).coerceIn(0f, graphHeight)
            drawLine(
                color = Color.Red.copy(alpha = thresholdAlpha),
                start = Offset(0f, thresholdY),
                end = Offset(width, thresholdY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )

            // 4. Draw peak indicators (for high amplitude points)
            points.forEachIndexed { index, point ->
                val amplitude = amplitudes[index].second
                if (amplitude >= threshold * 0.8f) {
                    // Draw glow effect for peaks
                    drawCircle(
                        color = lineColor.copy(alpha = 0.3f),
                        radius = 12f,
                        center = point
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 6f,
                        center = point
                    )
                }
            }

            // 5. Draw labels
            drawContext.canvas.nativeCanvas.apply {
                val textPaint = Paint().apply {
                    color = textColor.toArgb()
                    textSize = 24f
                    isAntiAlias = true
                }

                // Threshold label
                drawText(
                    "Threshold: ${threshold.toInt()}",
                    10f,
                    thresholdY - 10f,
                    textPaint
                )

                // Time labels
                val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                if (amplitudes.isNotEmpty()) {
                    val startTime = dateFormat.format(Date(amplitudes.first().first))
                    drawText(startTime, 10f, height - 10f, textPaint)

                    val endTime = dateFormat.format(Date(amplitudes.last().first))
                    val endTextWidth = textPaint.measureText(endTime)
                    drawText(endTime, width - endTextWidth - 10f, height - 10f, textPaint)
                }
            }
        }
    }
}

/**
 * Create a smooth bezier curve path from a list of points
 */
private fun createSmoothPath(points: List<Offset>): Path {
    if (points.size < 2) return Path()

    val path = Path()
    path.moveTo(points.first().x, points.first().y)

    for (i in 0 until points.size - 1) {
        val current = points[i]
        val next = points[i + 1]

        // Use cubic bezier for smooth curves
        // Control points are at 1/3 and 2/3 between points
        val controlX1 = current.x + (next.x - current.x) / 3
        val controlY1 = current.y
        val controlX2 = current.x + 2 * (next.x - current.x) / 3
        val controlY2 = next.y

        path.cubicTo(
            controlX1, controlY1,
            controlX2, controlY2,
            next.x, next.y
        )
    }

    return path
}
