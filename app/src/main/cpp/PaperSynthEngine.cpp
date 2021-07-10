//
// Created by Zwel Pai on 23-May-21.
//

#include <android/log.h>
#include <cinttypes>
#include <memory>
#include <utility>

#include "PaperSynthEngine.h"
#include "PaperSynthOscillator.h"

PaperSynthEngine::PaperSynthEngine(std::vector<FourierSeries> fourierSeries, int scaleOrdinal, int canvasHeight)
        : latencyCallback_(std::make_unique<PaperSynthLatencyTuningCallback>())
        , errorCallback_(std::make_unique<DefaultErrorCallback>(*this)){

        fourierSeries_ = std::move(fourierSeries);
        scaleOrdinal_ = scaleOrdinal;
        canvasHeight_ = canvasHeight;
}

void PaperSynthEngine::tap(bool isDown) {
    if (audioSource_) {
        audioSource_->tap(isDown);
    }
}

oboe::Result PaperSynthEngine::start() {
    std::lock_guard<std::mutex> lock(lock_);
    isLatencyDetectionSupported_ = false;
    auto result = openPlaybackStream();
    if (result == oboe::Result::OK){
        audioSource_ =  std::make_shared<PaperSynthSoundGenerator>(
                stream_->getSampleRate(),
                stream_->getChannelCount(),
                fourierSeries_,
                scaleOrdinal_,
                canvasHeight_);
        latencyCallback_->setSource(std::dynamic_pointer_cast<IRenderableAudio>(audioSource_));

        LOGD("Stream opened: AudioAPI = %d, channelCount = %d, deviceID = %d",
             stream_->getAudioApi(),
             stream_->getChannelCount(),
             stream_->getDeviceId());

        result = stream_->start();
        if (result != oboe::Result::OK) {
            LOGE("Error starting playback stream. Error: %s", oboe::convertToText(result));
            stream_->close();
            stream_.reset();
        } else {
            isLatencyDetectionSupported_ = (stream_->getTimestamp((CLOCK_MONOTONIC)) !=
                                            oboe::Result::ErrorUnimplemented);
        }
    } else {
        LOGE("Error creating playback stream. Error: %s", oboe::convertToText(result));
    }
    return result;
}

oboe::Result PaperSynthEngine::stop() {
    oboe::Result result = oboe::Result::OK;
    // Stop, close and delete in case not already closed.
    std::lock_guard<std::mutex> lock(lock_);
    if (stream_) {
        result = stream_->stop();
        stream_->close();
        stream_.reset();
    }
    return result;
}

void PaperSynthEngine::restart() {
    // The stream will have already been closed by the error callback.
    latencyCallback_->reset();
    start();
}

void PaperSynthEngine::setDeviceId(int32_t deviceId) {
    if (deviceId_ != deviceId) {
        deviceId_ = deviceId;
        if (reopenStream() != oboe::Result::OK) {
            LOGW("Open stream failed, forcing deviceId to Unspecified");
            deviceId_ = oboe::Unspecified;
        }
    }
}

void PaperSynthEngine::setChannelCount(int channelCount) {
    if (channelCount_ != channelCount) {
        channelCount_ = channelCount;
        reopenStream();
    }
}

void PaperSynthEngine::setAudioApi(oboe::AudioApi audioApi) {
    if (audioApi_ != audioApi) {
        audioApi_ = audioApi;
        reopenStream();
    }
}

void PaperSynthEngine::setBufferSizeInBursts(int32_t numBursts) {
    std::lock_guard<std::mutex> lock(lock_);
    if (!stream_) return;

    latencyCallback_->setBufferTuneEnabled(numBursts == BUFFER_SIZE_AUTOMATIC);
    auto result = stream_->setBufferSizeInFrames(
            numBursts * stream_->getFramesPerBurst());
    if (result) {
        LOGD("Buffer size successfully changed to %d", result.value());
    } else {
        LOGW("Buffer size could not be changed, %d", result.error());
    }
}

double PaperSynthEngine::getCurrentOutputLatencyMillis() {
    if (!isLatencyDetectionSupported_) return -1.0;

    std::lock_guard<std::mutex> lock(lock_);
    if (!stream_) return -1.0;

    // Get the time that a known audio frame was presented for playing
    auto result = stream_->getTimestamp(CLOCK_MONOTONIC);
    double outputLatencyMillis = -1;
    const int64_t NANOS_PER_MILLISECOND = 1000000;
    if (result == oboe::Result::OK) {
        oboe::FrameTimestamp playedFrame = result.value();
        // Get the write index for the next audio frame
        int64_t writeIndex = stream_->getFramesWritten();
        // Calculate the number of frames between our known frame and the write index
        int64_t frameIndexDelta = writeIndex - playedFrame.position;
        // Calculate the time which the next frame will be presented
        int64_t frameTimeDelta = (frameIndexDelta * oboe::kNanosPerSecond) /  (stream_->getSampleRate());
        int64_t nextFramePresentationTime = playedFrame.timestamp + frameTimeDelta;
        // Assume that the next frame will be written at the current time
        using namespace std::chrono;
        int64_t nextFrameWriteTime =
                duration_cast<nanoseconds>(steady_clock::now().time_since_epoch()).count();
        // Calculate the latency
        outputLatencyMillis = static_cast<double>(nextFramePresentationTime - nextFrameWriteTime)
                              / NANOS_PER_MILLISECOND;
    } else {
        LOGE("Error calculating latency: %s", oboe::convertToText(result.error()));
    }
    return outputLatencyMillis;
}

bool PaperSynthEngine::isLatencyDetectionSupported() const {
    return isLatencyDetectionSupported_;
}

oboe::Result PaperSynthEngine::reopenStream() {
    if (stream_) {
        stop();
        return start();
    } else {
        return oboe::Result::OK;
    }
}

oboe::Result PaperSynthEngine::openPlaybackStream() {
    oboe::AudioStreamBuilder builder;
    oboe::Result result = builder.setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setFormat(oboe::AudioFormat::Float)
            ->setDataCallback(latencyCallback_.get())
            ->setErrorCallback(errorCallback_.get())
            ->setAudioApi(audioApi_)
            ->setChannelCount(channelCount_)
            ->setDeviceId(deviceId_)
            ->openStream(stream_);
    if (result == oboe::Result::OK) {
        channelCount_ = stream_->getChannelCount();
    }
    return result;
}

void PaperSynthEngine::setAudioSourcePixelsArray(std::vector<int> pixelsArray, int width, int height) {
    if (audioSource_) {
        audioSource_->setPixelsArray(std::move(pixelsArray));
        audioSource_->setPixelsArrayDimensions(width, height);
    }
}
