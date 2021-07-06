//
// Created by Zwel Pai on 30-May-21.
//

#include "PaperSynthSoundGenerator.h"

PaperSynthSoundGenerator::PaperSynthSoundGenerator(
        int32_t sampleRate,
        int32_t channelCount,
        const FourierSeries& fourierSeries
)
        : TappableAudioSource(sampleRate, channelCount) {

//    lastPitchChangeTime_ = high_resolution_clock::now();
    float amplitude = 1.0f / (float)numOscs_; // TODO: make dynamic

    Eigen::Array<float, 1, Eigen::Dynamic> fourierWave = calculateFourierWave(fourierSeries, 1024);

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
        processPixelsArray();
        curSweepPosition_ = (curSweepPosition_ == pixelsArrayWidth_ - 1) ? 0 : curSweepPosition_ + 1;
        lastSweepTime_ = curTime;
    } else if (!waveIsOn_) {
        processPixelsArray(true);
    }

    outputStage_->renderAudio(audioData, numFrames);
}

void PaperSynthSoundGenerator::tap(bool isOn) {
    waveIsOn_ = isOn;
    if (!isOn) curSweepPosition_ = 0;
}

void PaperSynthSoundGenerator::processPixelsArray(bool disableAll) {
    for (int row = 0; row < pixelsArrayHeight_; ++row) {
        int pos = row * pixelsArrayWidth_ + curSweepPosition_;
        if (((pixelsArray_[pos] >> 24u) & 0xff) > 0) {
            oscillators_[row]->setWaveOn(waveIsOn_ && !disableAll);
        } else {
            oscillators_[row]->setWaveOn(false);
        }
    }
}

Eigen::Array<float, 1, Eigen::Dynamic> PaperSynthSoundGenerator::calculateFourierWave(const FourierSeries& fourierSeries, int n) {
    float stepSize = 1.0f / static_cast<float>(n);
    float period = M_PI * 2;

    // Initialize array of size n with values from range:
    // 0 to period with stepSize
    auto xs = Eigen::Array<float, 1, Eigen::Dynamic>(n);
    float curX = 0 + stepSize;
    for (int i = 0; i < n; i++) {
        xs(i) = curX * period;
        curX += stepSize;
    }

    // Initialize array of size n with a constant (A0 / 2)
    auto ys = Eigen::Array<float, 1, Eigen::Dynamic>(n).operator=(fourierSeries.a0 / 2);

    for (int k = 0; k < fourierSeries.numTerms; k++) {
        auto calculatedFreq = calculateFrequency(k, xs, period);
        auto calculatedA = calculatedFreq.cos().operator*(fourierSeries.coefficientsA[k]);
        auto calculatedB = calculatedFreq.sin().operator*(fourierSeries.coefficientsB[k]);

        ys.operator+=(calculatedA.operator+(calculatedB));
    }

    return ys;
}

Eigen::Array<float, 1, Eigen::Dynamic>
PaperSynthSoundGenerator::calculateFrequency(int k, const Eigen::Array<float, 1, Eigen::Dynamic>& xs, float period) {
    float timesVal = M_PI * (k + 1) / period;
    return xs.operator*(timesVal);
}
