#include <jni.h>
#include <oboe/Oboe.h>
#include <android/input.h>

#include "PaperSynthEngine.h"
#include "FFTUtil.h"
#include <logging_macros.h>

std::vector<int> convertJavaIntArrayToVector(JNIEnv *env, jintArray intArray) {
    std::vector<int> v;
    jsize length = env->GetArrayLength(intArray);
    if (length > 0) {
        jint *elements = env->GetIntArrayElements(intArray, nullptr);
        v.insert(v.end(), &elements[0], &elements[length]);
        // Unpin the memory for the array, or free the copy.
        env->ReleaseIntArrayElements(intArray, elements, 0);
    }
    return v;
}

std::vector<float> convertJavaFloatArrayToVector(JNIEnv *env, jfloatArray floatArray) {
    std::vector<float> v;
    jsize length = env->GetArrayLength(floatArray);
    if (length > 0) {
        jfloat *elements = env->GetFloatArrayElements(floatArray, nullptr);
        v.insert(v.end(), &elements[0], &elements[length]);
        // Unpin the memory for the array, or free the copy.
        env->ReleaseFloatArrayElements(floatArray, elements, 0);
    }
    return v;
}

extern "C" {

/**
 * Creates the audio engine
 *
 * @return a pointer to the audio engine. This should be passed to other methods
 */
JNIEXPORT jlong JNICALL
Java_com_example_papersynth_jni_PlaybackEngine_nativeCreateEngine(
        JNIEnv *env,
        jobject thiz,
        jfloatArray jWave1,
        jfloatArray jWave2,
        jfloatArray jWave3,
        jint scaleOrdinal,
        jint canvasHeight
) {

    float *wave1 = env->GetFloatArrayElements(jWave1, nullptr);
    float *wave2 = env->GetFloatArrayElements(jWave2, nullptr);
    float *wave3 = env->GetFloatArrayElements(jWave3, nullptr);

    std::vector<float*> waveForms;

    waveForms.emplace_back(wave1);
    waveForms.emplace_back(wave2);
    waveForms.emplace_back(wave3);

    // We use std::nothrow so `new` returns a nullptr if the engine creation fails
    auto *engine = new(std::nothrow) PaperSynthEngine(waveForms, scaleOrdinal, canvasHeight);
    if (engine == nullptr) {
        LOGE("Could not instantiate PaperSynthEngine");
        return 0;
    }
    return reinterpret_cast<jlong>(engine);
}

JNIEXPORT jint JNICALL
Java_com_example_papersynth_jni_PlaybackEngine_nativeStartEngine(
        JNIEnv *env,
        jobject,
        jlong engineHandle) {

    auto *engine = reinterpret_cast<PaperSynthEngine *>(engineHandle);
    return static_cast<jint>(engine->start());
}

JNIEXPORT jint JNICALL
Java_com_example_papersynth_jni_PlaybackEngine_nativeStopEngine(
        JNIEnv *env,
        jobject,
        jlong engineHandle) {

    auto *engine = reinterpret_cast<PaperSynthEngine *>(engineHandle);
    return static_cast<jint>(engine->stop());
}

JNIEXPORT void JNICALL
Java_com_example_papersynth_jni_PlaybackEngine_nativeDeleteEngine(
        JNIEnv *env,
        jobject,
        jlong engineHandle) {

    auto *engine = reinterpret_cast<PaperSynthEngine *>(engineHandle);
    engine->stop();
    delete engine;
}

JNIEXPORT void JNICALL
Java_com_example_papersynth_jni_PlaybackEngine_nativeSetToneOn(
        JNIEnv *env,
        jobject,
        jlong engineHandle,
        jboolean isToneOn) {

    auto *engine = reinterpret_cast<PaperSynthEngine *>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    engine->tap(isToneOn);
}

JNIEXPORT void JNICALL
Java_com_example_papersynth_jni_PlaybackEngine_nativeSetAudioApi(
        JNIEnv *env,
        jobject type,
        jlong engineHandle,
        jint audioApi) {

    auto *engine = reinterpret_cast<PaperSynthEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }

    auto api = static_cast<oboe::AudioApi>(audioApi);
    engine->setAudioApi(api);
}

JNIEXPORT void JNICALL
Java_com_example_papersynth_jni_PlaybackEngine_nativeSetAudioDeviceId(
        JNIEnv *env,
        jobject,
        jlong engineHandle,
        jint deviceId) {

    auto *engine = reinterpret_cast<PaperSynthEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    engine->setDeviceId(deviceId);
}

JNIEXPORT void JNICALL
Java_com_example_papersynth_jni_PlaybackEngine_nativeSetChannelCount(
        JNIEnv *env,
        jobject type,
        jlong engineHandle,
        jint channelCount) {

    auto *engine = reinterpret_cast<PaperSynthEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    engine->setChannelCount(channelCount);
}

JNIEXPORT void JNICALL
Java_com_example_papersynth_jni_PlaybackEngine_nativeSetBufferSizeInBursts(
        JNIEnv *env,
        jobject,
        jlong engineHandle,
        jint bufferSizeInBursts) {

    auto *engine = reinterpret_cast<PaperSynthEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine handle is invalid, call createHandle() to create a new one");
        return;
    }
    engine->setBufferSizeInBursts(bufferSizeInBursts);
}


JNIEXPORT jdouble JNICALL
Java_com_example_papersynth_jni_PlaybackEngine_nativeGetCurrentOutputLatencyMillis(
        JNIEnv *env,
        jobject,
        jlong engineHandle) {

    auto *engine = reinterpret_cast<PaperSynthEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine is null, you must call createEngine before calling this method");
        return static_cast<jdouble>(-1.0);
    }
    return static_cast<jdouble>(engine->getCurrentOutputLatencyMillis());
}

JNIEXPORT jboolean JNICALL
Java_com_example_papersynth_jni_PlaybackEngine_nativeIsLatencyDetectionSupported(
        JNIEnv *env,
        jobject type,
        jlong engineHandle) {

    auto *engine = reinterpret_cast<PaperSynthEngine*>(engineHandle);
    if (engine == nullptr) {
        LOGE("Engine is null, you must call createEngine before calling this method");
        return JNI_FALSE;
    }
    return (engine->isLatencyDetectionSupported() ? JNI_TRUE : JNI_FALSE);
}

JNIEXPORT void JNICALL
Java_com_example_papersynth_jni_PlaybackEngine_nativeSetDefaultStreamValues(
        JNIEnv *env,
        jobject type,
        jint sampleRate,
        jint framesPerBurst) {
    oboe::DefaultStreamValues::SampleRate = (int32_t) sampleRate;
    oboe::DefaultStreamValues::FramesPerBurst = (int32_t) framesPerBurst;
}

JNIEXPORT void JNICALL
Java_com_example_papersynth_jni_PlaybackEngine_nativeSetPixelsArray(
        JNIEnv *env,
        jobject type,
        jlong engineHandle,
        jintArray jPixelsArray,
        jint jWidth,
        jint jHeight) {

    std::vector<int> pixelsArray = convertJavaIntArrayToVector(env, jPixelsArray);

    auto *engine = reinterpret_cast<PaperSynthEngine*>(engineHandle);
    if (engine) {
        engine->setAudioSourcePixelsArray(pixelsArray, jWidth, jHeight);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

JNIEXPORT jfloatArray JNICALL
Java_com_example_papersynth_jni_FFT_nativeOverSample(
        JNIEnv *env,
        jobject type,
        jfloatArray jSamples,
        jint numSamples,
        jint oversampleFactor) {

    int oversampledLen = numSamples * oversampleFactor;

    jfloatArray result;
    result = env->NewFloatArray(oversampledLen);

    float *samples = env->GetFloatArrayElements(jSamples, nullptr);
    env->ReleaseFloatArrayElements(jSamples, samples, 0);
    float waveOversample[oversampledLen];

    FFTUtil::oversample(samples, waveOversample, numSamples, oversampleFactor);

    env->SetFloatArrayRegion(result, 0, oversampledLen, waveOversample);

    return result;
}

}