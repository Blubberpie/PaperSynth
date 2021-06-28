package com.example.papersynth.dataclasses

import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.NDArray

data class FourierSeries(
    val coefficientsA: NDArray<Float, D1>,
    val coefficientsB: NDArray<Float, D1>,
    val numTerms: Int,
    val a0: Float
) {

}
