//
// Created by Zwel Pai on 30-May-21.
//

#ifndef PAPERSYNTH_PAPERSYNTHOSCILLATOR_H
#define PAPERSYNTH_PAPERSYNTHOSCILLATOR_H

#include <cstdint>
#include <atomic>
#include <cmath>
#include <memory>

#include <IRenderableAudio.h>
#include <logging_macros.h>

#include "FourierSeries.h"
#include "Eigen/Dense"

using std::chrono::high_resolution_clock; // TODO REMOVE
using std::chrono::duration;

constexpr double FREQUENCY_DEFAULT = 440.0;
constexpr int32_t SAMPLE_RATE_DEFAULT = 48000;
constexpr double PI = M_PI;
constexpr double TWO_PI = PI * 2;

class PaperSynthOscillator : public IRenderableAudio {
public:
    PaperSynthOscillator(Eigen::Array<float, 1, Eigen::Dynamic> fourierWave);
    ~PaperSynthOscillator() = default;

    void setFourierWave(Eigen::Array<float, 1, Eigen::Dynamic> fourierWave);
    void setWaveOn(bool isWaveOn);
    void setSampleRate(int32_t sampleRate);
    void setFrequency(double frequency);
    inline void setAmplitude(float amplitude) {
        amplitude_ = amplitude;
    }

    // From IRenderableAudio
    void renderAudio(float *audioData, int32_t numFrames) override;

private:
    Eigen::Array<float, 1, Eigen::Dynamic> fourierWave_;
    std::atomic<bool> isWaveOn_ { false };
    std::atomic<float> amplitude_ { 0 };
    std::atomic<double> phaseIncrement_ { 0.0 };
    float phase_ = 0.0;
    double frequency_ = FREQUENCY_DEFAULT;
    int32_t sampleRate_ = SAMPLE_RATE_DEFAULT;

    void updatePhaseIncrement();
};


#endif //PAPERSYNTH_PAPERSYNTHOSCILLATOR_H
