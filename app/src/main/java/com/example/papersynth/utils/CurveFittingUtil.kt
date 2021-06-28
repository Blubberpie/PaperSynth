package com.example.papersynth.utils

import android.util.Log
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

    fun fit(data: FloatArray, n: Int) {
        val step: Float = 2f / n
        val period: Float = PI.toFloat()

        // Initialize array of size n from -PI to PI
        val arrX = mk.arange<Float>(-1, 1, step.toDouble())
            .map { x: Float -> (x + step) * period }
        val f = mk.ndarray(data)

        // Compute Fourier series

        val arrA0 = f.sum() * step
        var fFourierSeries = mk.empty<Float, D1>(n).map { arrA0 / 2 }

        val arrA = mk.empty<Float, D1>(NUM_TERMS)
        val arrB = mk.empty<Float, D1>(NUM_TERMS)

        fun calculateFrequencyFromK(k: Int): NDArray<Float, D1> {
            return arrX.map { x -> PI.toFloat() * (k + 1) * x / period}
        }

        for (k in 0 until NUM_TERMS) {
            arrA[k] = calculateFrequencyFromK(k)
                .map { x -> cos(x) }
                .times(f)
                .sum() * step // normalize

            arrB[k] = calculateFrequencyFromK(k)
                .map { x -> sin(x) }
                .times(f)
                .sum() * step

            fFourierSeries = fFourierSeries.plus(
                calculateFrequencyFromK(k)
                    .map { x -> cos(x) }
                    .map { x -> x * arrA[k] }
                        + calculateFrequencyFromK(k)
                    .map { x -> sin(x) }
                    .map { x -> x * arrB[k] }
            )
        }
    }
}