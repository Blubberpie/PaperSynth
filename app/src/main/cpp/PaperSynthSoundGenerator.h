//
// Created by Zwel Pai on 30-May-21.
//

#ifndef PAPERSYNTH_PAPERSYNTHSOUNDGENERATOR_H
#define PAPERSYNTH_PAPERSYNTHSOUNDGENERATOR_H

#include "PaperSynthOscillator.h"
#include <TappableAudioSource.h>
#include <chrono>

using std::chrono::high_resolution_clock;
using std::chrono::duration;

const double TWELFTH_ROOT_TWO = pow(2, 1.0/12); // semitone

class PaperSynthSoundGenerator : public TappableAudioSource {
    static constexpr size_t SHARED_BUFFER_SIZE = 1024;

public:
    /**
     * Create a new SoundGenerator object.
     *
     * @param sampleRate - The output sample rate.
     * @param maxFrames - The maximum number of audio frames which will be rendered, this is used to
     * calculate this object's internal buffer size.
     * @param channelCount - The number of channels in the output, one tone will be created for each
     * channel, the output will be interlaced.
     *
     */
    PaperSynthSoundGenerator(int32_t sampleRate, int32_t channelCount);
    ~PaperSynthSoundGenerator() = default;

    PaperSynthSoundGenerator(PaperSynthSoundGenerator&& other) = default;
    PaperSynthSoundGenerator& operator = (PaperSynthSoundGenerator&& other) = default;

    // Switch the tones on
    void tap(bool isOn) override;

    void renderAudio(float *audioData, int32_t numFrames) override;

private:
    std::unique_ptr<PaperSynthOscillator[]> oscillators_;
    std::unique_ptr<float[]> buffer_ = std::make_unique<float[]>(SHARED_BUFFER_SIZE);
    double cur_frequency_ = FREQUENCY_DEFAULT;
    int pitchChangeDelay_ = 1000; // ms
    high_resolution_clock::time_point lastPitchChangeTime_;
    bool waveIsOn_ = false;

    void addSemitoneAbove();
};


#endif //PAPERSYNTH_PAPERSYNTHSOUNDGENERATOR_H
