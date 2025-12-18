package com.example.timelimit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dailyUsages: List<DailyUsage> = emptyList()
    private val barPaint: Paint
    private val textPaint: Paint
    private val highlightedTextPaint: Paint
    private val highlightedBarPaint: Paint
    private val backgroundBarPaint: Paint

    init {
        barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.primary_dark)
        }
        highlightedBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.primary)
            setShadowLayer(15f, 0f, 0f, ContextCompat.getColor(context, R.color.primary))
        }
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.gray_700)
            textAlign = Paint.Align.CENTER
            textSize = 28f
        }
        highlightedTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.primary)
            textAlign = Paint.Align.CENTER
            textSize = 28f
            isFakeBoldText = true
        }
        backgroundBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.gray_100)
        }
    }

    fun setData(data: List<DailyUsage>) {
        this.dailyUsages = data
        invalidate() // Redraw the view with new data
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dailyUsages.isEmpty()) return

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat() - 40f // Leave space for labels at the bottom
        val barWidth = viewWidth / (dailyUsages.size * 2) // Give some spacing

        val maxUsage = dailyUsages.maxOfOrNull { it.usageMillis } ?: 1L // Avoid division by zero

        dailyUsages.forEachIndexed { index, usage ->
            val barHeightRatio = usage.usageMillis.toFloat() / maxUsage
            val barHeight = barHeightRatio * viewHeight

            val startX = (index * 2 + 0.5f) * barWidth
            val startY = viewHeight - barHeight

            // Background bar
            val backgroundRect = RectF(startX, 0f, startX + barWidth, viewHeight)
            canvas.drawRoundRect(backgroundRect, 20f, 20f, backgroundBarPaint)

            if (barHeight > 0) {
                // Foreground bar
                val barRect = RectF(startX, startY, startX + barWidth, viewHeight)
                val isMaxUsage = usage.usageMillis == dailyUsages.maxOf { it.usageMillis }
                val currentPaint = if (isMaxUsage) highlightedBarPaint else barPaint
                canvas.drawRoundRect(barRect, 20f, 20f, currentPaint)

                // Draw usage time on top
                val usageMinutes = TimeUnit.MILLISECONDS.toMinutes(usage.usageMillis)
                if (usageMinutes > 0) {
                    val timeTextPaint = if(isMaxUsage) highlightedTextPaint else textPaint
                    canvas.drawText("${usageMinutes}m", startX + barWidth / 2, startY - 20, timeTextPaint)
                }
            }
            
            // Draw day label
             val dayTextPaint = if (usage.usageMillis == dailyUsages.maxOf { it.usageMillis }) highlightedTextPaint else textPaint
            canvas.drawText(usage.day, startX + barWidth / 2, viewHeight + 35, dayTextPaint)
        }
    }
}
