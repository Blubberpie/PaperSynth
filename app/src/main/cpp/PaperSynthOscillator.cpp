//
// Created by Zwel Pai on 30-May-21.
//

#include "PaperSynthOscillator.h"

#include <utility>

PaperSynthOscillator::PaperSynthOscillator(Eigen::Array<float, 1, Eigen::Dynamic> fourierWave) {
    setFourierWave(std::move(fourierWave));
}

void PaperSynthOscillator::renderAudio(float *audioData, int32_t numFrames) {
    if (isWaveOn_) {
        for (int i = 0; i < numFrames; ++i) {

            int pos = static_cast<int>(round(1024 * (phase_ / TWO_PI)));
            if (pos < 0) pos = 0;
            else if (pos >= 1024) pos = 1023;
            audioData[i] = fourierWave_(pos) * amplitude_;

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

void PaperSynthOscillator::setFourierWave(Eigen::Array<float, 1, Eigen::Dynamic> fourierWave) {
    fourierWave_ = std::move(fourierWave);
}

/// PRIVATE ///

void PaperSynthOscillator::updatePhaseIncrement() {
    phaseIncrement_.store((TWO_PI * frequency_) / static_cast<double>(sampleRate_));
}
