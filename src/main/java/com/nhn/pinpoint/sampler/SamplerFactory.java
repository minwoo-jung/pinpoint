package com.nhn.pinpoint.sampler;

import com.nhn.pinpoint.profiler.sampler.Sampler;

/**
 *
 */
public class SamplerFactory {
    public Sampler createSampler(boolean sampling, int samplingRate) {
        if (!sampling) {
            return new FalseSampler();
        }
        if (samplingRate == 1) {
            return new TrueSampler();
        }
        return new SamplingRateSampler(samplingRate);
    }
}
