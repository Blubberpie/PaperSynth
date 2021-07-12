//
// Created by Zwel Pai on 30-May-21.
//

#include "PaperSynthSoundGenerator.h"

PaperSynthSoundGenerator::PaperSynthSoundGenerator(
        int32_t sampleRate,
        int32_t channelCount,
        const std::vector<float*>& waveForms,
        int scaleOrdinal,
        int canvasHeight
)
        : TappableAudioSource(sampleRate, channelCount) {

    numOscs_ = canvasHeight;
    scaleOrdinal_ = scaleOrdinal;
    float amplitude = 1.0f / (float)numOscs_; // TODO: make dynamic
    float ampSplit = amplitude / 3.0f;

    FFTUtil::oversample(waveForms.at(0), wave1_, 256, 4);
    FFTUtil::oversample(waveForms.at(1), wave2_, 256, 4);
    FFTUtil::oversample(waveForms.at(2), wave3_, 256, 4);

    std::vector<int> intervals = scaleIntervals_[scaleOrdinal];
    int intervalsSize = static_cast<int>(intervals.size());
    int curInterval = intervalsSize - 1;

    bool rise = false;

    for (int i = 0; i < numOscs_; ++i) {
        auto osc = new PaperSynthOscillator(
                wave1_,
                wave2_,
                wave3_
                ); // TODO: handle delete?? somehow?
        osc->setSampleRate(SAMPLE_RATE_DEFAULT);
        osc->setFrequency(curFrequency); // TODO: handle this
        osc->setAmplitudes(ampSplit, ampSplit, ampSplit);
        oscillators_.push_back(osc);
        mixer_.addTrack(osc);
        if (rise) {
            curFrequency = curFrequency * getIntervalFreq(intervals[curInterval]);
            curInterval++;
            if (curInterval >= intervalsSize) curInterval = 0;
            if (curFrequency >= MAXIMUM_FREQUENCY) rise = false;
        } else {
            curFrequency = curFrequency / getIntervalFreq(intervals[curInterval]);
            curInterval--;
            if (curInterval < 0) curInterval = intervalsSize - 1;
            if (curFrequency <= MINIMUM_FREQUENCY) rise = true;
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
    float amplitude = 1.0f / (float)numOscs_;
    for (int row = 0; row < pixelsArrayHeight_; ++row) {
        int pos = row * pixelsArrayWidth_ + curSweepPosition_;
        // A = (color >> 24) & 0xff
        // R = (color >> 16) & 0xff
        // G = (color >>  8) & 0xff
        // B = (color      ) & 0xff
        if (((pixelsArray_[pos] >> 24) & 0xff) > 0) {
            float rAmt = ((float)((pixelsArray_[pos] >> 16) & 0xff) / float(0xff));
            float gAmt = ((float)((pixelsArray_[pos] >> 8) & 0xff) / float(0xff));
            float bAmt = ((float)(pixelsArray_[pos] & 0xff) / float(0xff));
            float amtSum = rAmt + gAmt + bAmt;
            oscillators_[row]->setAmplitudes(
                    (rAmt / amtSum) * amplitude,
                    (gAmt / amtSum) * amplitude,
                    (bAmt / amtSum) * amplitude);
            oscillators_[row]->setWaveOn(waveIsOn_ && !disableAll);
        } else {
            oscillators_[row]->setWaveOn(false);
        }
    }
}

double PaperSynthSoundGenerator::getIntervalFreq(int interval) {
    return pow(2, interval/12.0);
}
