package com.nhn.pinpoint.interceptor;

/**
 * precompile level의 methodDescriptor를 setting 받을수 있게 한다.
 */
public interface ByteCodeMethodDescriptorSupport {
    void setMethodDescriptor(MethodDescriptor descriptor);
}
