//
// Created by lozbe on 7/12/2021.
//

#ifndef PAPERSYNTH_FFTUTIL_H
#define PAPERSYNTH_FFTUTIL_H

#include "pffft/pffft.h"
#include <cstring>

class FFTUtil {
public:
    static void FFT(const float *in, float *out, int len, bool inverse);
    static void RFFT(const float *in, float *out, int len);
    static void IRFFT(const float *in, float *out, int len);
    static void oversample(const float *in, float *out, int len, int oversample);
};


#endif //PAPERSYNTH_FFTUTIL_H
