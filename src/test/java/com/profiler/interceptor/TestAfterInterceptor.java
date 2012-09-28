package com.profiler.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class TestAfterInterceptor implements StaticAfterInterceptor {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public int call = 0;
    public Object target;
    public String className;
    public String methodName;
    public Object[] args;
    public Object result;

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        logger.info("after target:" + target  + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        this.target = target;
        this.className = className;
        this.methodName = methodName;
        this.args = args;
        call++;
        this.result = result;
    }
}
