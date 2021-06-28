package com.example.papersynth.utils

import android.util.Log
import org.jetbrains.kotlinx.multik.api.arange
import org.jetbrains.kotlinx.multik.api.empty
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D1
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

    fun test() {
        val sampleOscArr = floatArrayOf(0.8446298f,0.82586026f,0.83315957f,0.83315957f,0.83315957f,0.835245f,0.8373306f,0.8383733f,0.8383733f,0.8393712f,0.84059167f,
            0.84450144f,0.84775805f,0.8505964f,0.85297185f,0.85297185f,0.85297185f,0.85297185f,0.85297185f,0.85192907f,0.8498436f,0.8498436f,0.8498436f,
            0.8498436f,0.84880084f,0.8465626f,0.84358704f,0.8425443f,0.84290564f,0.8415016f,0.8415016f,0.8415016f,0.8415016f,0.84002584f,0.84057117f,0.8456726f,0.8491285f,0.85592747f,0.8532553f,0.8731213f,
            0.8738269f,0.8043876f,0.73866355f,0.650124f,0.4832849f,0.3952847f,0.29129577f,0.09724891f,0.09280503f,0.08898723f,0.08759123f,0.086591005f,0.08654851f,0.085505724f,0.08413154f,0.08342022f,0.08237749f,0.08237749f,0.08237749f,
            0.08237749f,0.08237749f,0.08133471f,0.08133471f,0.08133471f,0.08029199f,0.08029199f,0.08029199f,0.08029199f,0.08029199f,0.08029199f,0.08029199f,0.08029199f,
            0.08029199f,0.08029199f,0.08029199f,0.08029199f,0.08029199f,0.08029199f,0.08029199f,0.08029199f,0.08029199f,0.08125222f,0.08342022f,0.084463f,0.084463f,
            0.085505724f,0.81447136f,0.8105458f,0.8091762f,0.8079175f,0.8081335f,0.8081335f,0.8081335f,0.8081335f,0.8081335f,0.8081335f,0.8081335f,0.8081335f,0.8081335f,0.8081335f,0.8081335f,
            0.8081335f,0.8081335f,0.8070907f,0.8070907f,0.8070907f,0.8070907f,0.8070907f,0.8081335f,0.8081335f,0.8091762f,0.810219f,0.810219f,0.810219f,0.810219f,0.8121941f,0.8133685f,
            0.8133472f,0.81439f,0.81439f,0.81439f,0.81439f,0.81439f,0.81439f,0.81439f,0.81439f,0.7366673f,0.6748734f,0.5761578f,0.3934489f,0.07181907f,0.067778945f,0.070907176f,
            0.070907176f,0.07194996f,0.07255149f,0.074196994f,0.07542217f,0.07600731f,0.07612097f,0.07777995f,0.08029199f,0.08170593f,0.082898855f,0.08237749f,0.08408725f,
            0.084463f,0.084463f,0.085505724f,0.08654851f,0.08654851f,0.08654851f,0.08654851f,0.096774876f,0.08654851f,0.08654851f,0.09697604f,0.08759123f,0.09697604f,0.08759123f,0.08829516f,0.09697604f,
            0.0901981f,0.08967674f,0.09697604f,0.09071952f,0.09071952f,0.09071952f,0.09071952f,0.09071952f,0.08811259f,0.09071952f,0.7789364f,0.77789366f,0.77789366f,0.7768509f,0.7768509f,0.7768509f,0.7768509f,0.7768509f,0.7768509f,
            0.77789366f,0.77789366f,0.77789366f,0.77789366f,0.77789366f,0.7789631f,0.77956426f,0.78073925f,0.78246427f,0.7831074f,0.78423494f,0.7853769f,0.7851929f,0.7851929f,0.7862357f,0.7873786f,0.7248429f,0.6318113f,
            0.49497318f,0.07194996f,0.07194996f,0.074035466f,0.07507819f,0.07507819f,0.07691944f,0.07820648f,0.080316424f,0.08153421f,0.08271086f,0.08237749f,0.08237749f,0.08237749f,0.08097452f,0.08133471f,0.08133471f,
            0.08133471f,0.08133471f,0.08133471f,0.08133471f,0.08133471f,0.080583215f,0.08029199f,0.07872784f,0.0792492f,0.0792492f,0.0792492f,0.0792492f,.0792492f,0.07859111f,0.07820648f,0.07820648f,
            0.07820648f,0.0792492f,0.0792492f,0.0792492f,0.08072603f,0.08133471f,0.0825358f,0.08375412f,0.3717274f,0.73351324f,0.7309698f,0.7272506f,0.7247132f,0.7224149f,0.7205422f,0.7205422f,0.7205422f,
            0.721585f,0.7236705f,0.7236705f,0.725756f,0.7275636f,0.729927f,0.7247132f)
        fit(sampleOscArr, 256)
    }

    fun fit(data: FloatArray, n: Int) {
        val step: Float = 2f / n
        // Initialize array of size n from -PI to PI
        val arrX = mk.arange<Float>(-1, 1, step.toDouble())
            .map { x: Float -> (x + step) * PI.toFloat() }
        val f = mk.ndarray(data)

        // Compute Fourier series

        val arrA0 = f.sum() * step
        var fFourierSeries = mk.empty<Float, D1>(n).map { arrA0 / 2 }

        val arrA = mk.empty<Float, D1>(NUM_TERMS)
        val arrB = mk.empty<Float, D1>(NUM_TERMS)

        for (k in 0 until NUM_TERMS) {
            arrA[k] = arrX
                .map { x -> x * (k + 1) }
                .map { x -> cos(x) }
                .times(f)
                .sum() * step

            arrB[k] = arrX
                .map { x -> x * (k + 1) }
                .map { x -> sin(x) }
                .times(f)
                .sum() * step

            fFourierSeries = fFourierSeries.plus(
                arrX
                    .map { x -> x * (k + 1) }
                    .map { x -> cos(x) }
                    .map { x -> x * arrA[k]}
                + arrX
                    .map { x -> x * (k + 1) }
                    .map { x -> sin(x) }
                    .map { x -> x * arrB[k]}
            )
        }

        for (x in fFourierSeries) {
            Log.d("test", "$x")
        }
    }
}