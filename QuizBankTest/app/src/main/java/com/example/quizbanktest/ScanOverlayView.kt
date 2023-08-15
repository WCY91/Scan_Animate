package com.example.quizbanktest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class ScanOverlayView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint()
    private val scanRect: Rect

    init {
        // 設置畫筆的顏色和樣式
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f

        // 設置掃描框的位置和大小，這裡將它置於視圖的中心，大小為200x200
        val rectSize = 200
        val left = (width - rectSize) / 2
        val top = (height - rectSize) / 2
        val right = left + rectSize
        val bottom = top + rectSize
        scanRect = Rect(left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 繪製掃描框
        canvas.drawRect(scanRect, paint)
    }

    fun getScanRect(): Rect {
        return scanRect
    }
}
