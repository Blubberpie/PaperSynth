//
// Created by Zwel Pai on 30-May-21.
//

#include "PaperSynthSoundGenerator.h"

PaperSynthSoundGenerator::PaperSynthSoundGenerator(int32_t sampleRate, int32_t channelCount)
        : TappableAudioSource(sampleRate, channelCount)
        , oscillators_(std::make_unique<PaperSynthOscillator[]>(channelCount)) {

    lastPitchChangeTime_ = high_resolution_clock::now();
    constexpr float AMPLITUDE = 1.0;

    // Set up the oscillators
    for (int i = 0; i < mChannelCount; ++i) {
        oscillators_[i].setFrequency(cur_frequency_);
        oscillators_[i].setSampleRate(mSampleRate);
        oscillators_[i].setAmplitude(AMPLITUDE);
    }
}

void PaperSynthSoundGenerator::renderAudio(float *audioData, int32_t numFrames) {
    // Render each oscillator into its own channel
    std::fill_n(buffer_.get(), SHARED_BUFFER_SIZE, 0);
    high_resolution_clock::time_point curTime = high_resolution_clock::now();
    duration<double, std::milli> timeSincePitchChange = curTime - lastPitchChangeTime_;
    bool changePitch = timeSincePitchChange.count() >= pitchChangeDelay_;
    if (changePitch && waveIsOn_) addSemitoneAbove();
    for (int i = 0; i < mChannelCount; ++i) {
        if (changePitch && waveIsOn_) oscillators_[i].setFrequency(cur_frequency_);
        oscillators_[i].renderAudio(buffer_.get(), numFrames);
        for (int j = 0; j < numFrames; ++j) {
            audioData[(j * mChannelCount) + i] = buffer_[j];
        }
    }
    if (changePitch) {
        lastPitchChangeTime_ = curTime;
        changePitch = false;
    }
}

void PaperSynthSoundGenerator::tap(bool isOn) {
    for (int i = 0; i < mChannelCount; ++i) {
        oscillators_[i].setWaveOn(isOn);
    }
}

void PaperSynthSoundGenerator::addSemitoneAbove() {
    cur_frequency_ = (cur_frequency_ >= 20000) ? FREQUENCY_DEFAULT : cur_frequency_ * TWELFTH_ROOT_TWO;
}
