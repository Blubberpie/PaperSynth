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
        curFrequency = curFrequency * INTERVAL;
        if (curFrequency >= 16744.036179238312619382) {
            curFrequency = 55.0;
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

    outputStage_->renderAudio(audioData, numFrames);
}

void PaperSynthSoundGenerator::tap(bool isOn) {
    for (auto &osc : oscillators_) osc->setWaveOn(isOn);
}

//void PaperSynthSoundGenerator::addSemitoneAbove() {
//    curFrequency = (curFrequency >= 20000) ? FREQUENCY_DEFAULT : curFrequency * TWELFTH_ROOT_TWO;
//}
