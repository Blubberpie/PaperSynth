package com.example.papersynth.utils

import kotlin.math.sin

object MathUtil {

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
     fun calculateSineSample(
        x: Int,
        b: Float,
        a: Float=1f, h: Float=0f, k: Float=0f
    ): Float {
        return a * sin((x.toFloat() - h) / b) + k
    }
}