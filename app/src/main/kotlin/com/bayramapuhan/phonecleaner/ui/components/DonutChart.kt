package com.bayramapuhan.phonecleaner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DonutSegment(val value: Float, val color: Color)

@Composable
fun DonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 28.dp,
    centerContent: @Composable BoxScope.() -> Unit = {},
) {
    val total = segments.sumOf { it.value.toDouble() }.toFloat()
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            var startAngle = -90f
            segments.forEach { seg ->
                if (total > 0 && seg.value > 0) {
                    val sweep = (seg.value / total) * 360f
                    drawArc(
                        color = seg.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Butt),
                        topLeft = topLeft,
                        size = arcSize,
                    )
                    startAngle += sweep
                }
            }
        }
        centerContent()
    }
}
