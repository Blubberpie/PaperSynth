package com.example.papersynth.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import com.example.papersynth.R
import com.example.papersynth.dataclasses.FourierSeries
import com.example.papersynth.dataclasses.TextToDraw
import com.example.papersynth.utils.CurveFittingUtil
import com.example.papersynth.utils.CurveFittingUtil.calculateSineSample
import com.example.papersynth.utils.FileUtil
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.operations.forEachIndexed
import java.io.FileNotFoundException
import kotlin.math.PI
import kotlin.math.roundToInt

private const val NUM_SAMPLES = 256
private const val HALF_WAVE_CYCLE = NUM_SAMPLES / (2 * PI.toFloat())
private const val CANVAS_PADDING = 15f
private const val GRID_STROKE_WIDTH = 1f
private const val GRID_STROKE_ACCENT_WIDTH = 3f

class OscillatorCanvasView : View {

    enum class WaveEnum {
        WAVE_1, WAVE_2, WAVE_3, NUM_WAVES
    }

    // Late init //

    private lateinit var mainCanvas: Canvas
    private lateinit var mainBitmap: Bitmap
    private lateinit var currentFourierSeries: FourierSeries
    private lateinit var calculatedFourierYs: NDArray<Float, D1>

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
    private var canvasHalfHeight = 0f
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

    private val numStepsFourier = NUM_SAMPLES * 2
    private val stepSizeFourier = 1f / numStepsFourier
    private val arrXsFourier: NDArray<Float, D1> = CurveFittingUtil.generateXs(stepSizeFourier)
    private var fourierSampleSpreadAmount = 0f
    private var firstTime = true

    private val fourierPath = Path()
    private val gridLines = Path()
    private val gridLinesThick = Path()
    private val textsToDraw: ArrayList<TextToDraw> = ArrayList()

    private var waveList: ArrayList<FloatArray> = ArrayList(WaveEnum.NUM_WAVES.ordinal)
    private var currentWave = WaveEnum.WAVE_1

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    // TODO: onsizechanged is this a good thing?
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (::mainBitmap.isInitialized) mainBitmap.recycle()

        for (i in 0 until WaveEnum.NUM_WAVES.ordinal) {
            waveList.add(FloatArray(NUM_SAMPLES))
        }

        try {
            val activity: FragmentActivity = context as FragmentActivity
            val oscs = FileUtil.readOscillatorFromFile(activity, "my_oscillators.json")
            if (oscs == null) {
                resetOscillator()
            } else {
                oscs.forEachIndexed { i, osc ->
                    (osc.oscillator_data)?.let {
                        waveList[i] = it
                    }
                }
            }

        } catch (e: FileNotFoundException) {
            resetOscillator()
        }

        canvasWidth = width.toFloat() - (2 * CANVAS_PADDING)
        canvasHeight = height.toFloat() - (2 * CANVAS_PADDING)
        canvasHalfHeight = canvasHeight / 2
        dotSpreadAmount = canvasWidth / 256
        gridSpreadAmount = canvasWidth / numGridSpaces
        fourierSampleSpreadAmount = canvasWidth / numStepsFourier
        generateGridLines()

        mainBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mainCanvas = Canvas(mainBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (firstTime) { // workaround
            handleFourierComputations()
            firstTime = false
        }
        drawCanvasGrid(canvas)
        drawDotSamples(canvas)
        drawFourierSeries(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchAndMove()
            MotionEvent.ACTION_MOVE -> touchAndMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    //// PRIVATE ////

    private fun touchAndMove() {
        setNewSampleListValue()
        invalidate()
    }

    private fun touchUp() {
        handleFourierComputations()
    }

    // CALCULATIONS //

    private fun handleFourierComputations() {
        setFourierSeries(computeCurve(currentWave))
        generateYs()
        generateFourierPath()
    }

    private fun initializeSineWave() {
        for (w in waveList) {
            for (i in 0 until NUM_SAMPLES) {
                w[i] = calculateSineSample(i, b=HALF_WAVE_CYCLE)
            }
        }
    }

    private fun generateYs() {
        calculatedFourierYs = CurveFittingUtil.initializeFourierYs(numStepsFourier, currentFourierSeries.a0)
        calculatedFourierYs = CurveFittingUtil.calculateValuesByCoefficients(
            currentFourierSeries,
            arrXsFourier,
            calculatedFourierYs
        )
    }

    private fun generateFourierPath() {
        var lastXPos = CANVAS_PADDING
        var lastYPos = CANVAS_PADDING

        fourierPath.reset()

        calculatedFourierYs.forEachIndexed { i: Int, sample: Float ->
            val xPos = (i * fourierSampleSpreadAmount) + CANVAS_PADDING
            val yPos = (canvasHalfHeight * (1 - sample)) + CANVAS_PADDING

            fourierPath.quadTo(
                lastXPos,
                lastYPos,
                (xPos + lastXPos) / 2,
                (yPos + lastYPos) / 2,
            )
            lastXPos = xPos
            lastYPos = yPos
        }
    }

    private fun computeCurve(wave: WaveEnum): FourierSeries {
        return CurveFittingUtil.fit(waveList[wave.ordinal], NUM_SAMPLES)
    }

    // Drawing //

    private fun drawFourierSeries(canvas: Canvas) {
        canvas.drawPath(fourierPath, gridBrush)
    }

    private fun drawDotSamples(canvas: Canvas) {
        waveList[currentWave.ordinal].forEachIndexed { i, sample ->
            val xPos = (i * dotSpreadAmount) + CANVAS_PADDING
            val yPos = (canvasHalfHeight * (1 - sample)) + CANVAS_PADDING

            canvas.drawCircle(xPos, yPos, dotRadius, circleBrush)
        }
    }

    private fun drawCanvasGrid(canvas: Canvas) {
        setAccentedGridLine(false)
        canvas.drawPath(gridLines, gridBrush)
        setAccentedGridLine(true)
        canvas.drawPath(gridLinesThick, gridBrush)
        for (ttd in textsToDraw) {
            canvas.drawText(ttd.text, ttd.x, ttd.y, gridTextBrush)
        }
    }

    private fun generateGridLines() {
        val yCenter = (canvasHeight + (2 * CANVAS_PADDING)) / 2

        // Horizontal center line
        val horizontalLinePath = Path()
        horizontalLinePath.setLastPoint(CANVAS_PADDING, yCenter)
        horizontalLinePath.lineTo(canvasWidth + CANVAS_PADDING, yCenter)

        // Cache it
        gridLines.addPath(horizontalLinePath)

        // Vertical lines
        val verticalLinePath = Path()
        for (i in 0 until numGridSpaces + 1) {
            verticalLinePath.reset()
            val curSampleCount = i * gridSpaceLength
            val sectionLength = NUM_SAMPLES / numSections
            val xPos = (i * gridSpreadAmount) + CANVAS_PADDING

            if (curSampleCount % sectionLength == 0 || i == 0) {
                if (i != numGridSpaces + 1) {
                    textsToDraw.add(
                        TextToDraw(curSampleCount.toString(),
                        xPos + CANVAS_PADDING,
                        CANVAS_PADDING + gridTextBrush.textSize)
                    )
                }
                verticalLinePath.setLastPoint(xPos, CANVAS_PADDING)
                verticalLinePath.lineTo(xPos, canvasHeight + CANVAS_PADDING)
                gridLinesThick.addPath(verticalLinePath) // cache
            } else {
                verticalLinePath.setLastPoint(xPos, CANVAS_PADDING)
                verticalLinePath.lineTo(xPos, canvasHeight + CANVAS_PADDING)
                gridLines.addPath(verticalLinePath) // cache
            }
        }
    }

    // Setters //

    private fun setAccentedGridLine(accented: Boolean) {
        if (accented) {
            gridBrush.strokeWidth = GRID_STROKE_ACCENT_WIDTH
        } else {
            gridBrush.strokeWidth = GRID_STROKE_WIDTH
        }
    }

    private fun setNewSampleListValue() {
        var samplePos: Int = ((motionTouchEventX - CANVAS_PADDING) / dotSpreadAmount).roundToInt()
        val sampleVal: Float = -(motionTouchEventY - CANVAS_PADDING - canvasHalfHeight) / canvasHalfHeight

        // Protect against out of bounds
        if (samplePos < 0) samplePos = 0
        else if (samplePos >= NUM_SAMPLES) samplePos = NUM_SAMPLES - 1

        waveList[currentWave.ordinal][samplePos] = sampleVal
    }

    private fun setFourierSeries(fourierSeries: FourierSeries) {
        currentFourierSeries = fourierSeries
    }

    //// Public ////

    fun getWaves(): ArrayList<FloatArray> {
        return waveList
    }

    fun resetOscillator() {
        initializeSineWave()
        handleFourierComputations()
        invalidate()
    }

    fun setCurrentWave(wave: WaveEnum) {
        currentWave = wave
        handleFourierComputations()
        invalidate()
    }
}