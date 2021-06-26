package com.example.papersynth.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.alpha
import com.example.papersynth.R
import com.example.papersynth.dataclasses.AlphaArray
import kotlin.math.abs

private const val STROKE_WIDTH = 12f

class CanvasView : View {
    private lateinit var mainCanvas: Canvas
    private lateinit var mainBitmap: Bitmap

    private val brushColor = ResourcesCompat.getColor(resources, R.color.brushL, null)
    private val resizedWidth = 335 // todo: this is really bad coding lol
    private val resizedHeight = 108
    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f
    private var currentX = 0f
    private var currentY = 0f

    /* 28 pixels on Galaxy Note 5 */
//    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop
    private val touchTolerance = 8f // make it smoother

    private val brush = Paint().apply {
        color = brushColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE // default: FILL
        strokeJoin = Paint.Join.ROUND // default: MITER
        strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = STROKE_WIDTH
    }

    private val drawing = Path() // the drawing so far
    private val curPath = Path() // current drawing

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::mainBitmap.isInitialized) mainBitmap.recycle()
        mainBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mainCanvas = Canvas(mainBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(drawing, brush)
        canvas.drawPath(curPath, brush)
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
        val resizedBitmap = getResizedBitmap(mainBitmap)
        convertToAlphaArray(resizedBitmap)
        return resizedBitmap
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
}