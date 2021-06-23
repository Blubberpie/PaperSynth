//
// Created by Zwel Pai on 30-May-21.
//

#include "PaperSynthSoundGenerator.h"

PaperSynthSoundGenerator::PaperSynthSoundGenerator(int32_t sampleRate, int32_t channelCount)
        : TappableAudioSource(sampleRate, channelCount) {

//    lastPitchChangeTime_ = high_resolution_clock::now();
    float amplitude = 1.0f / (float)numOscs_; // TODO: make dynamic

    for (int i = 0; i < numOscs_; ++i) {
        auto osc = new PaperSynthOscillator(); // TODO: handle delete?? somehow?
        osc->setSampleRate(SAMPLE_RATE_DEFAULT);
        osc->setFrequency(curFrequency);
        osc->setAmplitude(amplitude);
        oscillators_.push_back(osc);
        mixer_.addTrack(osc);
        curFrequency = curFrequency / INTERVAL;
        if (curFrequency <= 55.0) {
            curFrequency = 16744.036179238312619382;
        }
    }

    if (mChannelCount == oboe::ChannelCount::Stereo) {
        outputStage_ =  &converter_;
    } else {
        outputStage_ = &mixer_;
    }
}

void PaperSynthSoundGenerator::renderAudio(float *audioData, int32_t numFrames) {
    // Render each oscillator into its own channel
//    std::fill_n(buffer_.get(), SHARED_BUFFER_SIZE, 0);
//    high_resolution_clock::time_point curTime = high_resolution_clock::now();
//    duration<double, std::milli> timeSincePitchChange = curTime - lastPitchChangeTime_;
//    bool changePitch = timeSincePitchChange.count() >= pitchChangeDelay_;
//    if (changePitch && waveIsOn_) addSemitoneAbove();
//    for (int i = 0; i < mChannelCount; ++i) {
//        if (changePitch && waveIsOn_) oscillators_[i].setFrequency(curFrequency);
//        oscillators_[i].renderAudio(buffer_.get(), numFrames);
//        for (int j = 0; j < numFrames; ++j) {
//            audioData[(j * mChannelCount) + i] = buffer_[j];
//        }
//    }
//    if (changePitch) {
//        lastPitchChangeTime_ = curTime;
//        changePitch = false;
//    }

    high_resolution_clock::time_point curTime = high_resolution_clock::now();
    duration<double, std::milli> timeSinceLastSweep = curTime - lastSweepTime_;
    bool shouldSweep = timeSinceLastSweep.count() >= sweepDelay_;

    if (waveIsOn_ && shouldSweep) {
        processAlphaArray();
        curSweepPosition_ = (curSweepPosition_ == alphaArrayWidth_ - 1) ? 0 : curSweepPosition_ + 1;
        lastSweepTime_ = curTime;
    }

    outputStage_->renderAudio(audioData, numFrames);
}

void PaperSynthSoundGenerator::tap(bool isOn) {
    waveIsOn_ = isOn;
    if (!isOn) curSweepPosition_ = 0;
}

void PaperSynthSoundGenerator::processAlphaArray() {
    for (int row = 0; row < alphaArrayHeight_; ++row) {
        int pos = row * alphaArrayWidth_ + curSweepPosition_;
        if (alphaArray_[pos] > 0) {
            oscillators_[row]->setWaveOn(waveIsOn_);
        } else {
            oscillators_[row]->setWaveOn(false);
        }
    }
}
