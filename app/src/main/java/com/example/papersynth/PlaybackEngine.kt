package com.example.papersynth

import android.content.Context
import android.media.AudioManager
import android.os.Build
import com.example.papersynth.dataclasses.AlphaArray

object PlaybackEngine {
    private var mEngineHandle: Long = 0
    fun create(context: Context): Boolean {
        if (mEngineHandle == 0L) {
            setDefaultStreamValues(context)
            mEngineHandle = nativeCreateEngine()
        }
        return mEngineHandle != 0L
    }

    private fun setDefaultStreamValues(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val myAudioMgr = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val sampleRateStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
            val defaultSampleRate = sampleRateStr.toInt()
            val framesPerBurstStr =
                myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
            val defaultFramesPerBurst = framesPerBurstStr.toInt()
            nativeSetDefaultStreamValues(defaultSampleRate, defaultFramesPerBurst)
        }
    }

    fun start(): Int {
        return if (mEngineHandle != 0L) {
            nativeStartEngine(mEngineHandle)
        } else {
            -1
        }
    }

    fun stop(): Int {
        return if (mEngineHandle != 0L) {
            nativeStopEngine(mEngineHandle)
        } else {
            -1
        }
    }

    fun delete() {
        if (mEngineHandle != 0L) {
            nativeDeleteEngine(mEngineHandle)
        }
        mEngineHandle = 0
    }

    fun setToneOn(isToneOn: Boolean) {
        if (mEngineHandle != 0L) nativeSetToneOn(mEngineHandle, isToneOn)
    }

    fun setAlphaArray(alphaArray: AlphaArray) {
        if (mEngineHandle != 0L) nativeSetAlphaArray(
            mEngineHandle,
            alphaArray.alphaArray,
            alphaArray.width,
            alphaArray.height
        )
    }

    fun processAlphaArray() {
        if (mEngineHandle != 0L) nativeProcessAlphaArray(mEngineHandle)
    }

    fun setAudioApi(audioApi: Int) {
        if (mEngineHandle != 0L) nativeSetAudioApi(mEngineHandle, audioApi)
    }

    fun setAudioDeviceId(deviceId: Int) {
        if (mEngineHandle != 0L) nativeSetAudioDeviceId(mEngineHandle, deviceId)
    }

    fun setChannelCount(channelCount: Int) {
        if (mEngineHandle != 0L) nativeSetChannelCount(mEngineHandle, channelCount)
    }

    fun setBufferSizeInBursts(bufferSizeInBursts: Int) {
        if (mEngineHandle != 0L) nativeSetBufferSizeInBursts(mEngineHandle, bufferSizeInBursts)
    }

    val currentOutputLatencyMillis: Double
        get() = if (mEngineHandle == 0L) 0.0 else nativeGetCurrentOutputLatencyMillis(mEngineHandle)
    val isLatencyDetectionSupported: Boolean
        get() = mEngineHandle != 0L && nativeIsLatencyDetectionSupported(mEngineHandle)

    // Native methods
    private external fun nativeCreateEngine(): Long
    private external fun nativeStartEngine(engineHandle: Long): Int
    private external fun nativeStopEngine(engineHandle: Long): Int
    private external fun nativeDeleteEngine(engineHandle: Long)
    private external fun nativeProcessAlphaArray(engineHandle: Long)

    private external fun nativeSetToneOn(engineHandle: Long, isToneOn: Boolean)
    private external fun nativeSetAudioApi(engineHandle: Long, audioApi: Int)
    private external fun nativeSetAudioDeviceId(engineHandle: Long, deviceId: Int)
    private external fun nativeSetChannelCount(mEngineHandle: Long, channelCount: Int)
    private external fun nativeSetBufferSizeInBursts(engineHandle: Long, bufferSizeInBursts: Int)
    private external fun nativeSetDefaultStreamValues(sampleRate: Int, framesPerBurst: Int)
    private external fun nativeSetAlphaArray(engineHandle: Long, alphaArray: IntArray, width: Int, height: Int)

    private external fun nativeGetCurrentOutputLatencyMillis(engineHandle: Long): Double
    private external fun nativeIsLatencyDetectionSupported(engineHandle: Long): Boolean

    // Load native library
    init {
        System.loadLibrary("papersynth")
    }
}