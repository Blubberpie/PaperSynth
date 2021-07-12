//
// Created by Zwel Pai on 30-May-21.
//

#include "PaperSynthOscillator.h"

#include <utility>

PaperSynthOscillator::PaperSynthOscillator(float *wave1,
                                           float *wave2,
                                           float *wave3) {
    setOversampledWaves(wave1, wave2, wave3);
}

void PaperSynthOscillator::renderAudio(float *audioData, int32_t numFrames) {
    if (isWaveOn_) {
        for (int i = 0; i < numFrames; ++i) {

            int pos = static_cast<int>(round(1024 * (phase_ / TWO_PI)));
            if (pos < 0) pos = 0;
            else if (pos >= 1024) pos = 1023;
            audioData[i] =
                    (wave1_[pos] * amplitude1_)
                    + (wave2_[pos] * amplitude2_)
                    + (wave3_[pos] * amplitude3_);

            phase_ += phaseIncrement_;
            if (phase_ > TWO_PI) phase_ -= TWO_PI;
        }
    } else {
        memset(audioData, 0, sizeof(float) * numFrames);
    }
}

void PaperSynthOscillator::setWaveOn(bool isWaveOn) {
    isWaveOn_.store(isWaveOn);
}

void PaperSynthOscillator::setSampleRate(int32_t sampleRate) {
    sampleRate_ = sampleRate;
    updatePhaseIncrement();
}

void PaperSynthOscillator::setFrequency(double frequency) {
    frequency_ = frequency;
    updatePhaseIncrement();
}

void PaperSynthOscillator::setOversampledWaves(float *wave1,
                                               float *wave2,
                                               float *wave3) {
    wave1_ = wave1;
    wave2_ = wave2;
    wave3_ = wave3;
}

/// PRIVATE ///

void PaperSynthOscillator::updatePhaseIncrement() {
    phaseIncrement_.store((TWO_PI * frequency_) / static_cast<double>(sampleRate_));
}
