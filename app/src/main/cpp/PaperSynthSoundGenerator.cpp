//
// Created by Zwel Pai on 30-May-21.
//

#include "PaperSynthSoundGenerator.h"

PaperSynthSoundGenerator::PaperSynthSoundGenerator(
        int32_t sampleRate,
        int32_t channelCount,
        int32_t framesPerBurst,
        const FourierSeries& fourierSeries
)
        : TappableAudioSource(sampleRate, channelCount) {

//    lastPitchChangeTime_ = high_resolution_clock::now();
    float amplitude = 1.0f / (float)numOscs_; // TODO: make dynamic

    std::vector<float> fourierWave = calculateFourierWave(fourierSeries, framesPerBurst);

    for (int i = 0; i < numOscs_; ++i) {
        auto osc = new PaperSynthOscillator(fourierWave); // TODO: handle delete?? somehow?
        osc->setSampleRate(SAMPLE_RATE_DEFAULT);
        osc->setFrequency(curFrequency); // TODO: handle this
        osc->setAmplitude(amplitude);
        oscillators_.push_back(osc);
        mixer_.addTrack(osc);
        curFrequency = curFrequency / INTERVAL_SEMITONE;
        if (curFrequency <= MINIMUM_FREQUENCY) {
            curFrequency = MAXIMUM_FREQUENCY;
        }
    }

    if (mChannelCount == oboe::ChannelCount::Stereo) {
        outputStage_ =  &converter_;
    } else {
        outputStage_ = &mixer_;
    }
}

void PaperSynthSoundGenerator::renderAudio(float *audioData, int32_t numFrames) {
    high_resolution_clock::time_point curTime = high_resolution_clock::now();
    duration<double, std::milli> timeSinceLastSweep = curTime - lastSweepTime_;
    bool shouldSweep = timeSinceLastSweep.count() >= sweepDelay_;

    if (waveIsOn_ && shouldSweep) {
        processAlphaArray();
        curSweepPosition_ = (curSweepPosition_ == alphaArrayWidth_ - 1) ? 0 : curSweepPosition_ + 1;
        lastSweepTime_ = curTime;
    } else if (!waveIsOn_) {
        processAlphaArray(true);
    }

    outputStage_->renderAudio(audioData, numFrames);
}

void PaperSynthSoundGenerator::tap(bool isOn) {
    waveIsOn_ = isOn;
    if (!isOn) curSweepPosition_ = 0;
}

void PaperSynthSoundGenerator::processAlphaArray(bool disableAll) {
    for (int row = 0; row < alphaArrayHeight_; ++row) {
        int pos = row * alphaArrayWidth_ + curSweepPosition_;
        if (alphaArray_[pos] > 0) {
            oscillators_[row]->setWaveOn(waveIsOn_ && !disableAll);
        } else {
            oscillators_[row]->setWaveOn(false);
        }
    }
}

std::vector<float> PaperSynthSoundGenerator::calculateFourierWave(const FourierSeries& fourierSeries, int32_t n) {
    float stepSize = 2.0f / n;

    // Initialize array of size n with values from range:
    // -1 to 1 with stepSize
    auto xs = Eigen::Array<float, 1, Eigen::Dynamic>(n);
    float curX = -1 + stepSize;
    for (int i = 0; i < n; i++) {
        xs(i) = curX;
        curX += stepSize;
    }

    // Initialize array of size n with a constant center y-value.
    auto ys = Eigen::Array<float, 1, Eigen::Dynamic>(n).operator=(fourierSeries.a0 / 2);

    for (int k = 0; k < fourierSeries.numTerms; k++) {
        auto calculatedFreq = calculateFrequency(k, xs);
        auto cosFreq = calculatedFreq.cos();
        auto calculatedA = cosFreq.operator*(fourierSeries.coefficientsA[k]);

        auto sinFreq = calculatedFreq.sin();
        auto calculatedB = sinFreq.operator*(fourierSeries.coefficientsB[k]);

        ys = ys.operator+(calculatedA.operator+(calculatedB));
    }

    return std::vector<float>();
}

Eigen::Array<float, 1, Eigen::Dynamic>
PaperSynthSoundGenerator::calculateFrequency(int k, Eigen::Array<float, 1, Eigen::Dynamic> xs) {
    float timesVal = M_PI * (k + 1);
    return xs.operator*(timesVal);
}
