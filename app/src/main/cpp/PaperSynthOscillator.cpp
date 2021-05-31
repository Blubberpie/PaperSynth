//
// Created by Zwel Pai on 30-May-21.
//

#include "PaperSynthOscillator.h"

void PaperSynthOscillator::renderAudio(float *audioData, int32_t numFrames) {
    if (isWaveOn_) {
        for (int i = 0; i < numFrames; ++i) {

            // Square wave
//            if (phase_ <= PI) {
//                audioData[i] = -amplitude_;
//            } else {
//                audioData[i] = amplitude_;
//            }

            // Sine wave
            audioData[i] = sinf(phase_) * amplitude_;

            // Triangle wave
//            float triangle = 2.0f * ((phase_ < 0.0f) ? (0.5f + phase_) : (0.5f - phase_));
//            audioData[i] = triangle * amplitude_;

            // Sawtooth
//            if (phase_ <= PI) {
//                audioData[i] += amplitude_;
//            } else {
//                audioData[i] += -amplitude_;
//            }

            phase_ += phaseIncrement_;
            if (phase_ > TWO_PI) phase_ -= TWO_PI;
        }
    } else {
        memset(audioData, 0, sizeof(float) * numFrames);
    }
}

void PaperSynthOscillator::updatePhaseIncrement() {
    phaseIncrement_.store((TWO_PI * frequency_) / static_cast<double>(sampleRate_));
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