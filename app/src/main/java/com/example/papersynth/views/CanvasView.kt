package com.example.papersynth.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.PathInterpolator
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.alpha
import com.example.papersynth.R
import com.example.papersynth.dataclasses.AlphaArray
import kotlin.math.abs

private const val STROKE_WIDTH = 12f

class CanvasView : View {
    private lateinit var mainCanvas: Canvas
    private lateinit var mainBitmap: Bitmap

    private val resizedWidth = 300 // todo: this is really bad coding lol
    private val resizedHeight = 88

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f
    private var currentX = 0f
    private var currentY = 0f

    private var drawGrid = false
    private var sweepDelay = 30L // ms
    private var currentSweepPos = 0f
    private var lastSweepTickTime = 0L
    private var isSweeping = false

    /* 28 pixels on Galaxy Note 5 */
//    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop
    private val touchTolerance = 8f // make it smoother

    private val brushColor = ResourcesCompat.getColor(resources, R.color.brushL, null)
    private val brush = Paint().apply {
        color = brushColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH
    }

    private val gridColor = ResourcesCompat.getColor(resources, R.color.elementD, null)
    private val gridBrush = Paint().apply {
        color = gridColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val drawing = Path() // the drawing so far
    private val curPath = Path() // current drawing
    private val gridLines = Path()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::mainBitmap.isInitialized) mainBitmap.recycle()

        generateGridLines(width.toFloat() / resizedWidth, height.toFloat() / resizedHeight)

        mainBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mainCanvas = Canvas(mainBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (drawGrid) {
            canvas.drawPath(gridLines, gridBrush)
        }
        canvas.drawPath(drawing, brush)
        canvas.drawPath(curPath, brush)
        drawSweeper(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    // PRIVATE

    private fun touchStart() {
        curPath.reset()
        curPath.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    private fun touchMove() {
        val dx = abs(motionTouchEventX - currentX)
        val dy = abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            curPath.quadTo( // quadratic bezier from pt1 to pt2
                currentX,
                currentY,
                (motionTouchEventX + currentX) / 2,
                (motionTouchEventY + currentY) / 2
            )
            currentX = motionTouchEventX
            currentY = motionTouchEventY
            mainCanvas.drawPath(curPath, brush) // cache it
        }
        invalidate() // force redraw screen with updated path
    }

    private fun touchUp() {
        drawing.addPath(curPath)
        curPath.reset()
    }

    private fun drawSweeper(canvas: Canvas) {
        if (isSweeping) {
            val curTime = System.currentTimeMillis()
            if (curTime - lastSweepTickTime >= sweepDelay) {
                currentSweepPos += width.toFloat() / resizedWidth
                if (currentSweepPos >= width) currentSweepPos = 0f
                lastSweepTickTime = curTime
            }
            canvas.drawLine(
                currentSweepPos,
                0f,
                currentSweepPos,
                height.toFloat(),
                gridBrush
            )
        } else {
            currentSweepPos = 0f
            lastSweepTickTime = 0L
        }
        invalidate()
    }

    private fun generateGridLines(spacingW: Float, spacingH: Float) {
        val gridLine = Path()

        // Horizontal lines
        for (y in 0 until resizedHeight) {
            gridLine.reset()
            val yPos = y * spacingH
            gridLine.setLastPoint(0f, yPos)
            gridLine.lineTo(width.toFloat(), yPos)
            gridLines.addPath(gridLine)
        }

        // Vertical lines
//        for (x in 0 until resizedWidth) {
//            gridLine.reset()
//            val xPos = x * spacingW
//            gridLine.setLastPoint(xPos, 0f)
//            gridLine.lineTo(xPos, height.toFloat())
//            gridLines.addPath(gridLine)
//        }
    }

    private fun getResizedBitmap(bm: Bitmap): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = resizedWidth.toFloat() / width
        val scaleHeight = resizedHeight.toFloat() / height

        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        return Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
    }

    /**
     * TODO: description
     */
    private fun convertToAlphaArray(bm: Bitmap): IntArray {
        val pixels = IntArray(resizedWidth * resizedHeight)
        bm.getPixels(pixels, 0, resizedWidth, 0, 0, resizedWidth, resizedHeight)
        for (row in 0 until resizedHeight) {
            for (col in 0 until resizedWidth) {
                val pos = row * resizedWidth + col
                if (Color.alpha(pixels[pos]) > 0) {
                    pixels[pos] = pixels[pos].alpha
                } else {
                    pixels[pos] = 0
                }
            }
        }

        return pixels
    }

    // PUBLIC

    fun getBitmap(): Bitmap {
        this.setBackgroundColor(Color.TRANSPARENT)
        draw(mainCanvas)
        return mainBitmap
    }

    // TODO: some mechanism to detect if bitmap has changed so that PSE doesn't have to process again
    fun getAlphaArray(): AlphaArray {
        this.setBackgroundColor(Color.TRANSPARENT)
        draw(mainCanvas)
        val resizedBitmap = getResizedBitmap(mainBitmap)
        return AlphaArray(
            convertToAlphaArray(resizedBitmap),
            resizedWidth,
            resizedHeight)
    }

    fun clearCanvas() {
        drawing.reset()
        curPath.reset()
        mainCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    fun sweep(willSweep: Boolean) {
        isSweeping = willSweep
        lastSweepTickTime = System.currentTimeMillis()
    }
}