package com.example.papersynth.utils

import android.util.Log
import com.example.papersynth.dataclasses.FourierSeries
import org.jetbrains.kotlinx.multik.api.arange
import org.jetbrains.kotlinx.multik.api.empty
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.operations.map
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.sum
import org.jetbrains.kotlinx.multik.ndarray.operations.times
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object CurveFittingUtil {

    private const val NUM_TERMS = 40

    fun fit(data: FloatArray, n: Int): FourierSeries {
        val step: Float = 2f / n
        val period: Float = PI.toFloat()

        // Initialize array of size n from -PI to PI
        val arrX = generateXs(step, period)
        val f = mk.ndarray(data)

        // Compute Fourier series

        // Lists of coefficients a and b
        val arrA = mk.empty<Float, D1>(NUM_TERMS)
        val arrB = mk.empty<Float, D1>(NUM_TERMS)

        for (k in 0 until NUM_TERMS) {
            arrA[k] = calculateFrequency(k, arrX, period)
                .map { x -> cos(x) }
                .times(f)
                .sum() * step // normalize

            arrB[k] = calculateFrequency(k, arrX, period)
                .map { x -> sin(x) }
                .times(f)
                .sum() * step
        }

        return FourierSeries(arrA, arrB, 40, f.sum() * step)
    }

    fun calculateValuesByCoefficients(
        fourierSeries: FourierSeries,
        xs: NDArray<Float, D1>,
        ys: NDArray<Float, D1>
    ): NDArray<Float, D1> {
        var calculatedYs = ys
        for (k in 0 until fourierSeries.numTerms) {
            calculatedYs = calculatedYs.plus(
                calculateFrequency(k, xs)
                    .map { x -> cos(x) }
                    .map { x -> x * fourierSeries.coefficientsA[k]}
                        + calculateFrequency(k, xs)
                    .map { x -> sin(x) }
                    .map { x -> x * fourierSeries.coefficientsB[k]}
            )
        }

        return calculatedYs
    }

    fun initializeFourierYs(n: Int, firstCoefficient: Float): NDArray<Float, D1> {
        // Center the function at A0 / 2
        return mk.empty<Float, D1>(n).map { firstCoefficient / 2 }
    }

    fun generateXs(stepSize: Float, period: Float=1f): NDArray<Float, D1> {
        return mk
            .arange<Float>(-1, 1, stepSize.toDouble())
            .map { x: Float -> (x + stepSize) * period }
    }

    // PRIVATE //

    private fun calculateFrequency(k: Int, xs: NDArray<Float, D1>, period: Float=1f): NDArray<Float, D1> {
        return xs.map { x -> PI.toFloat() * (k + 1) * x / period}
    }
}