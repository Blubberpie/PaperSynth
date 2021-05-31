//
// Created by Zwel Pai on 30-May-21.
//

#include "PaperSynthLatencyTuningCallback.h"

oboe::DataCallbackResult
PaperSynthLatencyTuningCallback::onAudioReady(
        oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) {

    if (oboeStream != stream_) {
        stream_ = oboeStream;
        latencyTuner_ = std::make_unique<oboe::LatencyTuner>(*oboeStream);
    }

    if (bufferTuneEnabled_
        && latencyTuner_
        && oboeStream->getAudioApi() == oboe::AudioApi::AAudio) {
        latencyTuner_->tune();
    }

    auto underrunCountResult = oboeStream->getXRunCount();
    int bufferSize = oboeStream->getBufferSizeInFrames();

    /**
     * The following output can be seen by running a systrace. Tracing is preferable to logging
    * inside the callback since tracing does not block.
    *
    * See https://developer.android.com/studio/profile/systrace-commandline.html
    */
    if (Trace::isEnabled()) Trace::beginSection("numFrames %d, Underruns %d, buffer size %d",
                                                numFrames, underrunCountResult.value(), bufferSize);
    auto result = DefaultDataCallback::onAudioReady(oboeStream, audioData, numFrames);
    if (Trace::isEnabled()) Trace::endSection();
    return result;
}
