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

    std::vector<float> fourierWave = calculateFourierWave(fourierSeries);

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

std::vector<float> PaperSynthSoundGenerator::calculateFourierWave(FourierSeries fourierSeries) {
    return std::vector<float>();
}
