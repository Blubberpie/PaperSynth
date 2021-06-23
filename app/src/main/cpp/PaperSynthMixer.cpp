//
// Created by lozbe on 6/23/2021.
//

#include "PaperSynthMixer.h"

void PaperSynthMixer::renderAudio(float *audioData, int32_t numFrames) {

    // Zero out the incoming container array
    memset(audioData, 0, sizeof(float) * numFrames * mChannelCount);

    for (int i = 0; i < mNextFreeTrackIndex; ++i) {
        mTracks[i]->renderAudio(mixingBuffer, numFrames);

        for (int j = 0; j < numFrames * mChannelCount; ++j) {
            audioData[j] += mixingBuffer[j];
        }
    }
}

void PaperSynthMixer::removeAllTracks() {
    for (int i = 0; i < mNextFreeTrackIndex; i++){
        mTracks[i] = nullptr;
    }
    mNextFreeTrackIndex = 0;
}
