package com.example.papersynth.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.example.papersynth.R
import com.example.papersynth.utils.CurveFittingUtil
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.roundToInt

private const val NUM_SAMPLES = 256
private const val HALF_WAVE_CYCLE = NUM_SAMPLES / (2 * PI.toFloat())
private const val MAX_SAMPLE_VAL = 1f
private const val MIN_SAMPLE_VAL = 0f
private const val CANVAS_PADDING = 15f
private const val GRID_STROKE_WIDTH = 1f
private const val GRID_STROKE_ACCENT_WIDTH = 3f

class OscillatorCanvasView : View {

    // Private //

    private lateinit var mainCanvas: Canvas
    private lateinit var mainBitmap: Bitmap

    // Dot-related

    private val circleColor = ResourcesCompat.getColor(resources, R.color.tealD, null)
    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f
    private var dotSpreadAmount = 0f
    private var dotRadius = 5f
    private val circleBrush = Paint().apply {
        color = circleColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
    }

    // Grid-related

    private val gridColor = ResourcesCompat.getColor(resources, R.color.elementD, null)
    private var canvasWidth = 0f
    private var canvasHeight = 0f
    private var gridSpreadAmount = 0f
    private val gridSpaceLength = 8
    private val numGridSpaces = NUM_SAMPLES / gridSpaceLength
    private val numSections = 4
    private val gridBrush = Paint().apply {
        color = gridColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeWidth = GRID_STROKE_WIDTH
    }
    private val gridTextBrush = Paint().apply {
        color = gridColor
        isAntiAlias = true
        isDither = true
        textSize = 50f
    }

    private var sampleList = FloatArray(NUM_SAMPLES)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    // TODO: onsizechanged is this a good thing?
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        initializeSineWave()
        canvasWidth = width.toFloat() - (2 * CANVAS_PADDING)
        canvasHeight = height.toFloat() - (2 * CANVAS_PADDING)
        dotSpreadAmount = canvasWidth / 256
        gridSpreadAmount = canvasWidth / numGridSpaces

        if (::mainBitmap.isInitialized) mainBitmap.recycle()
        mainBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mainCanvas = Canvas(mainBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCanvasGrid(canvas)
        drawDotSamples(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchAndMove()
            MotionEvent.ACTION_MOVE -> touchAndMove()
        }
        return true
    }

    // PRIVATE

    private fun touchAndMove() {
        setNewSampleListValue()
        invalidate()
    }

    private fun setNewSampleListValue() {
        var samplePos: Int = ((motionTouchEventX / canvasWidth) * NUM_SAMPLES).roundToInt()
        val sampleVal: Float = 1 - (motionTouchEventY / canvasHeight)

        // Protect against out of bounds
        if (samplePos < 0) samplePos = 0
        else if (samplePos >= NUM_SAMPLES) samplePos = NUM_SAMPLES - 1

        sampleList[samplePos] = sampleVal
    }

    /**
     * Computes the sin of a given x with the equation:
     * a * sin((x.toFloat() - h) / b) + k
     *
     * @param x Input variable
     * @param a Amplitude
     * @param h Horizontal shift
     * @param k Vertical shift
     * @param b Periodicity factor
     */
    private fun calculateSineSample( // TODO: move to util file?
        x: Int,
        b: Float=HALF_WAVE_CYCLE,
        a: Float=0.5f, h: Float=0f, k: Float=0.5f
    ): Float {
        return a * sin((x.toFloat() - h) / b) + k
    }

    private fun initializeSineWave() {
        for (i in 0 until NUM_SAMPLES) {
            sampleList[i] = calculateSineSample(i)
        }
    }

    private fun drawCanvasGrid(canvas: Canvas) {
        val yCenter = (canvasHeight + (2 * CANVAS_PADDING)) / 2

        // Horizontal center line
        canvas.drawLine(
            CANVAS_PADDING,
            yCenter,
            canvasWidth + CANVAS_PADDING,
            yCenter,
            gridBrush
        )

        // Vertical lines
        for (i in 0 until numGridSpaces + 1) {
            val curSampleCount = i * gridSpaceLength
            val sectionLength = NUM_SAMPLES / numSections
            val xPos = (i * gridSpreadAmount) + CANVAS_PADDING

            if (curSampleCount % sectionLength == 0 || i == 0) {
                if (i != numGridSpaces + 1) {
                    canvas.drawText(
                        curSampleCount.toString(),
                        xPos + CANVAS_PADDING,
                        CANVAS_PADDING + gridTextBrush.textSize,
                        gridTextBrush
                    )
                }
                setAccentedGridLine(true)
            } else {
                setAccentedGridLine(false)
            }

            canvas.drawLine(
                xPos,
                CANVAS_PADDING,
                xPos,
                canvasHeight + CANVAS_PADDING,
                gridBrush
            )
        }
    }

    private fun drawDotSamples(canvas: Canvas) {
        sampleList.forEachIndexed { i, sample ->
            val xPos = (i * dotSpreadAmount) + CANVAS_PADDING
            val yPos = (canvasHeight * (1 - sample)) + CANVAS_PADDING

            canvas.drawCircle(xPos, yPos, dotRadius, circleBrush)
        }
    }

    private fun setAccentedGridLine(accented: Boolean) {
        if (accented) {
            gridBrush.strokeWidth = GRID_STROKE_ACCENT_WIDTH
        } else {
            gridBrush.strokeWidth = GRID_STROKE_WIDTH
        }
    }

    // Public //

    fun getOscillator(): FloatArray {
        return sampleList
    }

    fun computeCurve() {
        CurveFittingUtil.fit(sampleList, NUM_SAMPLES)
    }
}