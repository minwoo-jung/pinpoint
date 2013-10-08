package com.nhn.pinpoint.profiler.interceptor;

/**
 * 객체 생성을 줄이기 위해서 객체를 리턴하지 않고 c 스타일 api로 디자인함.
 */
public interface ParameterExtractor {
    public static final Object NULL = new Object();

    public static final int NOT_FOUND = -1;

    int extractIndex(Object[] parameterList);

    Object extractObject(Object[] parameterList, int index);
}
