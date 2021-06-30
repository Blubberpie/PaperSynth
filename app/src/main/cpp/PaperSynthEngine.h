//
// Created by Zwel Pai on 23-May-21.
//

#ifndef PAPERSYNTH_PAPERSYNTHENGINE_H
#define PAPERSYNTH_PAPERSYNTHENGINE_H

#include <oboe/Oboe.h>

#include <IRestartable.h>
#include <DefaultErrorCallback.h>
#include <logging_macros.h>

#include "FourierSeries.h"
#include "PaperSynthOscillator.h"
#include "PaperSynthLatencyTuningCallback.h"
#include "PaperSynthSoundGenerator.h"

using namespace oboe;

constexpr int32_t BUFFER_SIZE_AUTOMATIC = 0;

class PaperSynthEngine : public IRestartable {
public:
    PaperSynthEngine(FourierSeries fourierSeries);
    virtual  ~PaperSynthEngine() = default;

    void tap(bool isDown);
    oboe::Result start();
    oboe::Result stop();

    void restart() override;

    // These methods reset the underlying stream with new properties

    /**
     * Set the audio device which should be used for playback. Can be set to oboe::kUnspecified if
     * you want to use the default playback device (which is usually the built-in speaker if
     * no other audio devices, such as headphones, are attached).
     *
     * @param deviceId the audio device id, can be obtained through an {@link AudioDeviceInfo} object
     * using Java/JNI.
    */
    void setDeviceId(int32_t deviceId);
    void setChannelCount(int channelCount);
    void setAudioApi(oboe::AudioApi audioApi);
    void setBufferSizeInBursts(int32_t numBursts);

    /**
     * Calculate the current latency between writing a frame to the output stream and
     * the same frame being presented to the audio hardware.
     *
     * Here's how the calculation works:
     *
     * 1) Get the time a particular frame was presented to the audio hardware
     * @see AudioStream::getTimestamp
     * 2) From this extrapolate the time which the *next* audio frame written to the stream
     * will be presented
     * 3) Assume that the next audio frame is written at the current time
     * 4) currentLatency = nextFramePresentationTime - nextFrameWriteTime
     *
     * @return  Output Latency in Milliseconds
     */
    double getCurrentOutputLatencyMillis();
    bool isLatencyDetectionSupported() const;

    void setAudioSourceAlphaArray(std::vector<int> alphaArray, int width, int height);

private:
    oboe::Result reopenStream();
    oboe::Result openPlaybackStream();

    FourierSeries fourierSeries_;
    std::shared_ptr<oboe::AudioStream> stream_;
    std::unique_ptr<PaperSynthLatencyTuningCallback> latencyCallback_;
    std::unique_ptr<DefaultErrorCallback> errorCallback_;
    std::shared_ptr<PaperSynthSoundGenerator> audioSource_;
    bool isLatencyDetectionSupported_ = false;

    int32_t        deviceId_ = oboe::Unspecified;
    int32_t        channelCount_ = oboe::Unspecified;
    oboe::AudioApi audioApi_ = oboe::AudioApi::Unspecified;
    std::mutex     lock_;
};


#endif //PAPERSYNTH_PAPERSYNTHENGINE_H
