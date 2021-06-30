#include <jni.h>
#include <oboe/Oboe.h>
#include <android/input.h>

#include "PaperSynthEngine.h"
#include "FourierSeries.h"
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
Java_com_example_papersynth_PlaybackEngine_nativeCreateEngine(
        JNIEnv *env,
        jobject thiz,
        jfloatArray jCoefficientsA,
        jfloatArray jCoefficientsB,
        jint jNumTerms,
        jfloat jA0
) {

    std::vector<float> coefficientsA = convertJavaFloatArrayToVector(env, jCoefficientsA);
    std::vector<float> coefficientsB = convertJavaFloatArrayToVector(env, jCoefficientsB);

    // We use std::nothrow so `new` returns a nullptr if the engine creation fails
    auto *engine = new(std::nothrow) PaperSynthEngine(
            FourierSeries(coefficientsA, coefficientsB, jNumTerms, jA0)
            );
    if (engine == nullptr) {
        LOGE("Could not instantiate PaperSynthEngine");
        return 0;
    }
    return reinterpret_cast<jlong>(engine);
}

JNIEXPORT jint JNICALL
Java_com_example_papersynth_PlaybackEngine_nativeStartEngine(
        JNIEnv *env,
        jobject,
        jlong engineHandle) {

    auto *engine = reinterpret_cast<PaperSynthEngine *>(engineHandle);
    return static_cast<jint>(engine->start());
}

JNIEXPORT jint JNICALL
Java_com_example_papersynth_PlaybackEngine_nativeStopEngine(
        JNIEnv *env,
        jobject,
        jlong engineHandle) {

    auto *engine = reinterpret_cast<PaperSynthEngine *>(engineHandle);
    return static_cast<jint>(engine->stop());
}

JNIEXPORT void JNICALL
Java_com_example_papersynth_PlaybackEngine_nativeDeleteEngine(
        JNIEnv *env,
        jobject,
        jlong engineHandle) {

    auto *engine = reinterpret_cast<PaperSynthEngine *>(engineHandle);
    engine->stop();
    delete engine;
}

JNIEXPORT void JNICALL
Java_com_example_papersynth_PlaybackEngine_nativeSetToneOn(
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
Java_com_example_papersynth_PlaybackEngine_nativeSetAudioApi(
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
Java_com_example_papersynth_PlaybackEngine_nativeSetAudioDeviceId(
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
Java_com_example_papersynth_PlaybackEngine_nativeSetChannelCount(
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
Java_com_example_papersynth_PlaybackEngine_nativeSetBufferSizeInBursts(
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
Java_com_example_papersynth_PlaybackEngine_nativeGetCurrentOutputLatencyMillis(
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
Java_com_example_papersynth_PlaybackEngine_nativeIsLatencyDetectionSupported(
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
Java_com_example_papersynth_PlaybackEngine_nativeSetDefaultStreamValues(
        JNIEnv *env,
        jobject type,
        jint sampleRate,
        jint framesPerBurst) {
    oboe::DefaultStreamValues::SampleRate = (int32_t) sampleRate;
    oboe::DefaultStreamValues::FramesPerBurst = (int32_t) framesPerBurst;
}

JNIEXPORT void JNICALL
Java_com_example_papersynth_PlaybackEngine_nativeSetAlphaArray(
        JNIEnv *env,
        jobject type,
        jlong engineHandle,
        jintArray jAlphaArray,
        jint jWidth,
        jint jHeight) {

    std::vector<int> alphaArray = convertJavaIntArrayToVector(env, jAlphaArray);

    auto *engine = reinterpret_cast<PaperSynthEngine*>(engineHandle);
    if (engine) {
        engine->setAudioSourceAlphaArray(alphaArray, jWidth, jHeight);
    } else {
        LOGE("Engine handle is invalid, call createEngine() to create a new one");
    }
}

}