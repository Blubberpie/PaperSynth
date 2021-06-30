//
// Created by lozbe on 6/30/2021.
//

#include "FourierSeries.h"

#include <utility>

FourierSeries::FourierSeries(
        std::vector<float> coefficientsA,
        std::vector<float> coefficientsB,
        int numTerms,
        float a0)
{
    this->coefficientsA = std::move(coefficientsA);
    this->coefficientsB = std::move(coefficientsB);
    this->numTerms = numTerms;
    this->a0 = a0;
}
