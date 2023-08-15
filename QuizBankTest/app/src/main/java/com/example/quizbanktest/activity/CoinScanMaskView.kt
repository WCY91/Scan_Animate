package com.example.quizbanktest.activity

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.quizbanktest.R

class CoinScanMaskView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var circleRadius = 0f
    private var width = 0f
    private var height = 0f
    private var circleDrawLeft = 0f
    private var circleDrawTop = 0f

    private val mPaint = Paint()

    private val mTextPaint = Paint().apply {
        color = resources.getColor(R.color.white)
        textSize = 14.toFloat()
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL_AND_STROKE
        textAlign = Paint.Align.CENTER
    }
    private val clearMode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val circleBitmap: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.baseline_circle_24)
    private var tipText = "Ready in a moment…"

    // 扫描线
    private val scanBitmap: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.baseline_linear_scale_24)
    private var mScanLineTop = 0f
    private val mScanLineRectF = RectF()
    private val clipScanClearMode =
        PorterDuffXfermode(PorterDuff.Mode.DST_IN)//只在源图像和目标图像相交的地方绘制目标图像
    private var clipBitmap: Bitmap
    private var needCanvasScan = false


    init {
        clipBitmap = createCircularBitmap()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        circleRadius = ((circleBitmap.width / 2)).toFloat()
        width = measuredWidth.toFloat()
        height = measuredHeight.toFloat()
        circleDrawLeft = (width - circleRadius * 2) / 2
        circleDrawTop = (height - circleRadius * 2) / 2

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPaint.reset()
        val layerID = canvas.saveLayer(0f, 0f, width, height, mPaint)
        // 蒙层背景
        mPaint.color = 0x66000000
        canvas.drawRect(0f, 0f, width, height, mPaint)
        // 中间透明区域
        mPaint.xfermode = clearMode
        mPaint.color = 0x00000000
        canvas.drawCircle(
            width / 2, height / 2, circleRadius,
            mPaint
        )
        canvas.restoreToCount(layerID)
        mPaint.reset()

        // 提示文字
        canvas.drawText(
            tipText,
            (width / 2).toFloat(),
            height / 2 + circleRadius + 32,
            mTextPaint
        )

        //圆形边框
        canvas.drawBitmap(
            circleBitmap,
            circleDrawLeft,
            circleDrawTop,
            mPaint
        )

        //放开该行代码可查看自己绘制的 形状，与下面的scanBitmap扫描的相交
//        canvas.drawBitmap(
//            clipBitmap, circleDrawLeft,
//            circleDrawTop - scanBitmap.height, mPaint
//        )
        //再画上结果
        if (needCanvasScan) {
            val layerScanId =
                canvas.saveLayer(0f, 0f, width, height, null, Canvas.ALL_SAVE_FLAG)
            mScanLineRectF.set(
                circleDrawLeft, circleDrawTop + mScanLineTop - scanBitmap.height,
                circleDrawLeft + clipBitmap.width,
                circleDrawTop + mScanLineTop
            )
            //绘制第一层,目标图像，在下层
            canvas.drawBitmap(
                scanBitmap,
                null,
                mScanLineRectF,
                mPaint
            )
            mPaint.xfermode = clipScanClearMode
            //在圆形上面绘制一个画布，防止扫描线bitmap透出显示，源图像，第二层盖在了目标图像上
            canvas.drawBitmap(
                clipBitmap, circleDrawLeft,
                circleDrawTop - scanBitmap.height, mPaint
            )
            mPaint.xfermode = null
            canvas.restoreToCount(layerScanId)
            moveScanLine()
        }
    }


    /**
     * 移动扫描线
     */
    private fun moveScanLine() {
        mScanLineTop += 2
        if (mScanLineTop > circleBitmap.height) {
            mScanLineTop = 0f
        }
        postInvalidateDelayed(16, 0, 0, measuredWidth, measuredHeight)
    }

    private fun createCircularBitmap(): Bitmap {
        // 创建一个长方形的 Bitmap
        val size = 190
        // 设置 Bitmap 大小
        val bitmap = Bitmap.createBitmap(size, size + scanBitmap.height, Bitmap.Config.ARGB_8888)

        // 创建一个 Canvas 对象，将 bitmap 作为绘制目标
        val canvas = Canvas(bitmap)

        // 创建一个 Paint 对象，用于设置绘制属性
        val paint = Paint().apply {
            color = Color.RED // 设置圆形区域的颜色,必须要有颜色不然合成的时候无效
        }

        // 创建一个圆形路径
        val centerX = size / 2f
        val centerY = size / 2f
        val radius = size / 2f
        //绘制一个圆形，因为只想要圆形区域，所以只绘制圆形区域
        val path = Path().apply {
            addCircle(centerX, centerY + scanBitmap.height, radius, Path.Direction.CW)
        }
        //在圆形上面绘制一个长方形，防止扫描线bitmap透出显示
        //放开该行代码可查看自己绘制的 长方形形状，与下面的scanBitmap扫描的相交
//        path.addRect(0f, 0f, size.toFloat(), scanBitmap.height.toFloat(), Path.Direction.CW)

        // 在 Canvas 上绘制圆形路径
        canvas.drawPath(path, paint)
        return bitmap
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        needCanvasScan = visibility == View.VISIBLE
        if (needCanvasScan) {
            mScanLineTop = 0f
        }
    }

}