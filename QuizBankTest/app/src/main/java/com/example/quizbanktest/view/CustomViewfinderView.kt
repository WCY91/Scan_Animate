package com.example.quizbanktest.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.journeyapps.barcodescanner.ViewfinderView


class CustomViewfinderView(context: android.content.Context, attrs: android.util.AttributeSet) : ViewfinderView(context, attrs) {
    private var scannerMiddle: Int = 0
    private var scannerDirection = 1
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        refreshSizes()

        if (framingRect == null || previewSize == null) {
            return
        }

        val frame = framingRect
        val previewSize = previewSize

        val width = width
        val height = height

        // Draw the exterior (i.e. outside the framing rect) darkened

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.color = if (resultBitmap != null) resultColor else maskColor
        canvas.drawRect(0f, 0f, width.toFloat(), frame.top.toFloat(), paint)
        canvas.drawRect(
            0f,
            frame.top.toFloat(),
            frame.left.toFloat(),
            (frame.bottom + 1).toFloat(),
            paint
        )
        canvas.drawRect(
            (frame.right + 1).toFloat(),
            frame.top.toFloat(),
            width.toFloat(),
            (frame.bottom + 1).toFloat(),
            paint
        )
        canvas.drawRect(0f, (frame.bottom + 1).toFloat(), width.toFloat(), height.toFloat(), paint)

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.alpha = CURRENT_POINT_OPACITY
            canvas.drawBitmap(resultBitmap, null, frame, paint)
        } else {
            // If wanted, draw a red "laser scanner" line through the middle to show decoding is active
//            if (laserVisibility) {
//                paint.color = laserColor
//                paint.alpha = SCANNER_ALPHA[scannerAlpha]
//                scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.size
//                val middle = frame.top + scannerMiddle
//                canvas.drawRect(
//                    (frame.left + 2).toFloat(),
//                    (middle - 1).toFloat(),
//                    (frame.right - 1).toFloat(),
//                    (middle + 2).toFloat(),
//                    paint
//                )
//                scannerMiddle += scannerDirection * 5
//                if (scannerMiddle > frame.height() || scannerMiddle < 0) {
//                    scannerDirection *= -1
//                }
//            }
            if (true) {
                paint.color = laserColor
//                paint.alpha = SCANNER_ALPHA[scannerAlpha]
//                scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.size
                paint.alpha = 255

                val middle = frame.top + scannerMiddle
                val thickness = 6f  // 粗細
                canvas.drawRect(frame.left + 2f, middle - thickness, frame.right - 1f, middle + thickness, paint)

                scannerMiddle += scannerDirection * 5
                if (scannerMiddle > frame.height() || scannerMiddle < 0) {
                    scannerDirection *= -1
                }
            }

            val scaleX = width / previewSize.width.toFloat()
            val scaleY = height / previewSize.height.toFloat()

            // draw the last possible result points
            if (!lastPossibleResultPoints.isEmpty()) {
                paint.alpha = CURRENT_POINT_OPACITY / 2
                paint.color = resultPointColor
                val radius = POINT_SIZE / 2.0f
                for (point in lastPossibleResultPoints) {
                    canvas.drawCircle(
                        (point.x * scaleX).toInt().toFloat(),
                        (point.y * scaleY).toInt().toFloat(),
                        radius, paint
                    )
                }
                lastPossibleResultPoints.clear()
            }

            // draw current possible result points
            if (!possibleResultPoints.isEmpty()) {
                paint.alpha = CURRENT_POINT_OPACITY
                paint.color = resultPointColor
                for (point in possibleResultPoints) {
                    canvas.drawCircle(
                        (point.x * scaleX).toInt().toFloat(),
                        (point.y * scaleY).toInt().toFloat(),
                        POINT_SIZE.toFloat(), paint
                    )
                }

                // swap and clear buffers
                val temp = possibleResultPoints
                possibleResultPoints = lastPossibleResultPoints
                lastPossibleResultPoints = temp
                possibleResultPoints.clear()
            }

            // Request another update at the animation interval, but only repaint the laser line,
            // not the entire viewfinder mask.
            postInvalidateDelayed(
                ANIMATION_DELAY,
                frame.left - POINT_SIZE,
                frame.top - POINT_SIZE,
                frame.right + POINT_SIZE,
                frame.bottom + POINT_SIZE
            )
        }
    }

}
