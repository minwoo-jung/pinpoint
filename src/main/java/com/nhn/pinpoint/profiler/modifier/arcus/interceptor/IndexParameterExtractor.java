package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import com.nhn.pinpoint.profiler.interceptor.ParameterExtractor;

/**
 *
 */
public class IndexParameterExtractor implements ParameterExtractor {

    private final int index;

    public IndexParameterExtractor(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public Object extractObject(Object[] parameterList) {
        if (parameterList == null) {
            return NULL;
        }
        return parameterList[index];
    }


}
