//
// Created by lozbe on 6/30/2021.
//

#ifndef PAPERSYNTH_FOURIERSERIES_H
#define PAPERSYNTH_FOURIERSERIES_H

#include <vector>

class FourierSeries {
public:
    FourierSeries(
            std::vector<float> coefficientsA,
            std::vector<float> coefficientsB,
            int numTerms,
            float a0);

    std::vector<float> coefficientsA;
    std::vector<float> coefficientsB;
    int numTerms;
    float a0;
};


#endif //PAPERSYNTH_FOURIERSERIES_H
