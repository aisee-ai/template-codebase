package com.example.aisee_template_codebase.ml

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View

class OverlayView(context: Context) : View(context) {

    private val boxPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    
    private val textPaint = Paint().apply {
        color = Color.GREEN
        textSize = 40f
        style = Paint.Style.FILL
    }

    private var boxes: List<Rect> = emptyList()
    private var labels: List<String> = emptyList()

    fun updateBoxes(newBoxes: List<Rect>, newLabels: List<String>) {
        boxes = newBoxes
        labels = newLabels
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        for (i in boxes.indices) {
            val box = boxes[i]
            canvas.drawRect(box, boxPaint)
            if (i < labels.size) {
                canvas.drawText(labels[i], box.left.toFloat(), box.top.toFloat() - 10f, textPaint)
            }
        }
    }
}
