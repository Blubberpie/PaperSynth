//
// Created by Zwel Pai on 30-May-21.
//

#ifndef PAPERSYNTH_PAPERSYNTHSOUNDGENERATOR_H
#define PAPERSYNTH_PAPERSYNTHSOUNDGENERATOR_H

#include "PaperSynthOscillator.h"
#include "PaperSynthMixer.h"
#include "FourierSeries.h"
#include "Eigen/Dense"

#include <oboe/Oboe.h>
#include <TappableAudioSource.h>
#include <MonoToStereo.h>
#include <chrono>
#include <vector>
#include <cmath>

using std::chrono::high_resolution_clock;
using std::chrono::duration;

const double MAXIMUM_FREQUENCY = 8372.0180896191563096911; // C9
const double MINIMUM_FREQUENCY = 55.0; // very low A

const double INTERVAL_PERFECT_FIFTH = pow(2, 7.0/12);
const double INTERVAL_SEMITONE = pow(2, 1.0/12);

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
    PaperSynthSoundGenerator(
            int32_t sampleRate,
            int32_t channelCount,
            const FourierSeries& fourierSeries);
    ~PaperSynthSoundGenerator() = default;

    PaperSynthSoundGenerator(PaperSynthSoundGenerator&& other) = default;
    PaperSynthSoundGenerator& operator = (PaperSynthSoundGenerator&& other) = default;

    // Switch the tones on
    void tap(bool isOn) override;

    void renderAudio(float *audioData, int32_t numFrames) override;

    void setAlphaArray(std::vector<int> alphaArray) { alphaArray_ = alphaArray; }
    void setAlphaArrayDimensions(int width, int height) {
        alphaArrayWidth_ = width;
        alphaArrayHeight_ = height;
    }
    static Eigen::Array<float, 1, Eigen::Dynamic> calculateFourierWave(const FourierSeries& fourierSeries, int n);

private:
    std::vector<PaperSynthOscillator*> oscillators_;
    PaperSynthMixer mixer_;
    MonoToStereo converter_ = MonoToStereo(&mixer_);
    IRenderableAudio *outputStage_; // This will point to either the mixer or converter, so it needs to be raw

    int numOscs_ = 88;

    std::vector<int> alphaArray_;
    int alphaArrayWidth_ = 0;
    int alphaArrayHeight_ = 0;

    high_resolution_clock::time_point lastSweepTime_;
    bool waveIsOn_ = false;
    double curFrequency = MAXIMUM_FREQUENCY;
    int sweepDelay_ = 30; // ms
    int curSweepPosition_ = 0;

    void processAlphaArray(bool disableAll=false);
    static Eigen::Array<float, 1, Eigen::Dynamic> calculateFrequency(int k, const Eigen::Array<float, 1, Eigen::Dynamic>& xs, float period);
};


#endif //PAPERSYNTH_PAPERSYNTHSOUNDGENERATOR_H
