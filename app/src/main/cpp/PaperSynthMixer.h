//
// Created by lozbe on 6/23/2021.
//

#ifndef PAPERSYNTH_PAPERSYNTHMIXER_H
#define PAPERSYNTH_PAPERSYNTHMIXER_H

#include <array>
#include "IRenderableAudio.h"

constexpr int32_t kBufferSize = 192*10;  // Temporary buffer is used for mixing
constexpr uint8_t kMaxTracks = 115;

/**
 * A Mixer object which sums the output from multiple tracks into a single output. The number of
 * input channels on each track must match the number of output channels (default 1=mono). This can
 * be changed by calling `setChannelCount`.
 * The inputs to the mixer are not owned by the mixer, they should not be deleted while rendering.
 */
class PaperSynthMixer : public IRenderableAudio {

public:
    void renderAudio(float *audioData, int32_t numFrames) override;

    void addTrack(IRenderableAudio *renderer){
        mTracks[mNextFreeTrackIndex++] = renderer;
    }

    void removeAllTracks();

    void setChannelCount(int32_t channelCount){ mChannelCount = channelCount; }

private:
    float mixingBuffer[kBufferSize];
    std::array<IRenderableAudio*, kMaxTracks> mTracks;
    uint8_t mNextFreeTrackIndex = 0;
    int32_t mChannelCount = 1; // Default to mono
};


#endif //PAPERSYNTH_PAPERSYNTHMIXER_H
