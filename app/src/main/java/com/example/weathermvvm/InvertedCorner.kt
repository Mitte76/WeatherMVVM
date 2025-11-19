package com.example.weathermvvm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp

/**
 * @param corner The corner of the box where the shape should be drawn.
 * @param cornerRadius The radius of the inverted corner.
 * @param color The color to draw the shape with.
 * @param modifier The modifier to be applied to this composable.
 */
@Composable
fun InvertedCorner(
    corner: Corner,
    cornerRadius: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val radius = cornerRadius.toPx()

        val path = Path().apply {
            when (corner) {
                Corner.TopLeft -> {
                    moveTo(0f, 0f)
                    lineTo(0f, radius)
                    arcTo(
                        rect = Rect(left = 0f, top = 0f, right = radius * 2, bottom = radius * 2),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    close()
                }
                Corner.TopRight -> {
                    moveTo(size.width, 0f)
                    lineTo(size.width - radius, 0f)
                    arcTo(
                        rect = Rect(left = size.width - (radius * 2), top = 0f, right = size.width, bottom = radius * 2),
                        startAngleDegrees = 270f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    close()
                }
                Corner.BottomLeft -> {
                    moveTo(0f, size.height)
                    lineTo(radius, size.height)
                    arcTo(
                        rect = Rect(left = 0f, top = size.height - (radius * 2), right = radius * 2, bottom = size.height),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    close()
                }
                Corner.BottomRight -> {
                    moveTo(size.width, size.height)
                    lineTo(size.width, size.height - radius)
                    arcTo(
                        rect = Rect(left = size.width - (radius * 2), top = size.height - (radius * 2), right = size.width, bottom = size.height),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    close()
                }
            }
        }

        drawPath(
            path = path,
            color = color,
        )
    }
}

enum class Corner {
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight
}