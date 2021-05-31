//
// Created by Zwel Pai on 30-May-21.
//

#ifndef PAPERSYNTH_PAPERSYNTHLATENCYTUNINGCALLBACK_H
#define PAPERSYNTH_PAPERSYNTHLATENCYTUNINGCALLBACK_H

#include <oboe/Oboe.h>
#include <oboe/LatencyTuner.h>

#include <TappableAudioSource.h>
#include <DefaultDataCallback.h>
#include <trace.h>

/**
 * This callback object extends the functionality of `DefaultDataCallback` by automatically
 * tuning the latency of the audio stream. @see onAudioReady for more details on this.
 *
 * It also demonstrates how to use tracing functions for logging inside the audio callback without
 * blocking.
 */
class PaperSynthLatencyTuningCallback: public DefaultDataCallback {
public:
    PaperSynthLatencyTuningCallback() : DefaultDataCallback() {

        // Initialize the trace functions, this enables you to output trace statements without
        // blocking. See https://developer.android.com/studio/profile/systrace-commandline.html
        Trace::initialize();
    }

    /**
     * Every time the playback stream requires data this method will be called.
     *
     * @param audioStream the audio stream which is requesting data, this is the mPlayStream object
     * @param audioData an empty buffer into which we can write our audio data
     * @param numFrames the number of audio frames which are required
     * @return Either oboe::DataCallbackResult::Continue if the stream should continue requesting data
     * or oboe::DataCallbackResult::Stop if the stream should stop.
     */


    void setBufferTuneEnabled(bool enabled) { bufferTuneEnabled_ = enabled; }

    oboe::DataCallbackResult
    onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) override;

    void useStream(std::shared_ptr<oboe::AudioStream> stream);

private:
    bool bufferTuneEnabled_ = true;

    // This will be used to automatically tune the buffer size of the stream, obtaining optimal latency
    std::unique_ptr<oboe::LatencyTuner> latencyTuner_;
    oboe::AudioStream  *stream_ = nullptr;
};


#endif //PAPERSYNTH_PAPERSYNTHLATENCYTUNINGCALLBACK_H
