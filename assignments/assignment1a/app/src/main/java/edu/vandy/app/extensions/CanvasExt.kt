package edu.vandy.app.extensions

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import edu.vandy.app.ui.adapters.dpToPx

private val rect = Rect()
private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

fun Canvas.drawRulers(width: Int, height: Int, step: Int = 50) {
    val textSize = paint.textSize

    paint.textSize = 8.dpToPx
    drawHorizontalRuler(width, step)
    drawVerticalRuler(height, step)

    paint.textSize = textSize
}

fun Canvas.drawVerticalRuler(size: Int, step: Int = 50) {
    val color = paint.color

    for (i in 0..(size) step step) {
        paint.color = Color.RED
        drawLine(0f, i.toFloat(),
                 20f, i.toFloat(),
                 paint)
        paint.color = Color.BLACK
        val text = i.toString()
        when (i) {
            0 -> {
                paint.getTextBounds(text, 0, text.length, rect)
                drawText(text,
                         0f, i.toFloat() - rect.top,
                         paint)
            }
            height -> drawText(text,
                               0f, i.toFloat(),
                               paint)
            else -> drawText(text,
                             0f, i.toFloat(),
                             paint)
        }
    }

    if (height % step != 0) {
        paint.color = Color.RED
        drawLine(0f, height.toFloat(),
                 20f, height.toFloat(),
                 paint)
        paint.color = Color.BLACK
        drawText(height.toString(),
                 0f, height.toFloat(),
                 paint)
    }

    paint.color = color
}

fun Canvas.drawHorizontalRuler(size: Int, step: Int = 50) {
    val color = paint.color

    for (i in 0..(size) step step) {
        paint.color = Color.RED
        drawLine(i.toFloat(), 0f,
                 i.toFloat(), 20f,
                 paint)
        paint.color = Color.BLACK
        val text = i.toString()
        paint.getTextBounds(text, 0, text.length, rect)
        if (i == size) {
            drawText(size.toString(),
                     size.toFloat() - rect.width(),
                     0f - rect.top,
                     paint)
        } else if (i < size &&
                   size - i > step &&
                   rect.width() < step) {
            // draw marker text unless the 2nd to last
            // one doesn't leave room to draw the more
            // important end marker text.
            drawText(text,
                     i.toFloat(), 0f - rect.top,
                     paint)
        }
    }

    val text = size.toString()
    paint.getTextBounds(text, 0, text.length, rect)
    paint.color = Color.RED
    drawLine(size.toFloat(), 0f,
             size.toFloat(), 20f,
             paint)
    paint.color = Color.BLACK
    drawText(size.toString(),
             size.toFloat() - rect.width(),
             0f - rect.top,
             paint)

    paint.color = color
}

fun Canvas.drawWireFrame(rect: Rect,
                         color: Int = Color.RED,
                         drawSizes: Boolean = false) {
    val oldColor = paint.color
    val oldTextSize = paint.textSize
    val oldStyle = paint.style

    paint.textSize = 8.dpToPx
    paint.color = color
    paint.style = Paint.Style.STROKE

    drawRect(rect, paint)

    if (drawSizes) {
        paint.color = Color.BLACK

        var x = rect.right.toFloat()
        var y = rect.top + rect.height() / 2f
        drawText(rect.height().toString(), x, y, paint)

        x = rect.left + rect.width() / 2f
        y = rect.bottom.toFloat()

        drawText(rect.width().toString(), x, y, paint)
    }

    paint.color = oldColor
    paint.textSize = oldTextSize
    paint.style = oldStyle
}

