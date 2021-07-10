//
// Created by Zwel Pai on 30-May-21.
//

#include "PaperSynthSoundGenerator.h"

PaperSynthSoundGenerator::PaperSynthSoundGenerator(
        int32_t sampleRate,
        int32_t channelCount,
        const std::vector<FourierSeries>& fourierSeries,
        int scaleOrdinal,
        int canvasHeight
)
        : TappableAudioSource(sampleRate, channelCount) {

    numOscs_ = canvasHeight;
    scaleOrdinal_ = scaleOrdinal;
    float amplitude = 1.0f / (float)numOscs_; // TODO: make dynamic
    float ampSplit = amplitude / 3.0f;

    for (const FourierSeries& fs : fourierSeries) {
        fourierWaves_.push_back(calculateFourierWave(fs, 1024));
    }

    std::vector<int> intervals = scaleIntervals_[scaleOrdinal];
    int intervalsSize = static_cast<int>(intervals.size());
    int curInterval = intervalsSize - 1;

    bool rise = false;

    for (int i = 0; i < numOscs_; ++i) {
        auto osc = new PaperSynthOscillator(&fourierWaves_); // TODO: handle delete?? somehow?
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

double PaperSynthSoundGenerator::getIntervalFreq(int interval) {
    return pow(2, interval/12.0);
}
