//
// Created by lozbe on 7/12/2021.
//
// Code in this file is based on FFT methods from the WaveEdit repository:
// https://github.com/AndrewBelt/WaveEdit/blob/master/src/math.cpp
//

#include "FFTUtil.h"
#include <logging_macros.h>

void FFTUtil::FFT(const float *in, float *out, int len, bool inverse) {
    PFFFT_Setup *setup = pffft_new_setup(len, PFFFT_REAL);
    float *work = nullptr;
    if (len >= 4096)
        work = (float*)pffft_aligned_malloc(sizeof(float) * len);
    pffft_transform_ordered(setup, in, out, work, inverse ? PFFFT_BACKWARD : PFFFT_FORWARD);
    pffft_destroy_setup(setup);
    if (work)
        pffft_aligned_free(work);
}

void FFTUtil::RFFT(const float *in, float *out, int len) {
    FFT(in, out, len, false);

    float a = 1.0f / static_cast<float>(len);
    for (int i = 0; i < len; i++) {
        out[i] *= a;
    }
}

void FFTUtil::IRFFT(const float *in, float *out, int len) {
    FFT(in, out, len, true);
}

void FFTUtil::oversample(const float *in, float *out, int len, int oversample) {
    float x[len * oversample];
    memset(x, 0, sizeof(x));
    // Zero-stuff oversampled buffer
    for (int i = 0; i < len; i++) {
        x[i * oversample] = in[i] * static_cast<float>(oversample);
    }
    float fft[len * oversample];
    RFFT(x, fft, len * oversample);

    // Apply brick wall filter
    // y_{N/2} = 0
    fft[1] = 0.0;
    // y_k = 0 for k >= len
    for (int i = len / 2; i < len * oversample / 2; i++) {
        fft[2*i] = 0.0;
        fft[2*i + 1] = 0.0;
    }

    IRFFT(fft, out, len * oversample);
}