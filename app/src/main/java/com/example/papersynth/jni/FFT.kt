package com.example.papersynth.jni

object FFT {

    fun oversample(samples: FloatArray, oversampleFactor: Int): FloatArray {
        return nativeOverSample(samples, samples.size, oversampleFactor)
    }

    private external fun nativeOverSample(samples: FloatArray, numSamples: Int, oversampleFactor: Int): FloatArray
}