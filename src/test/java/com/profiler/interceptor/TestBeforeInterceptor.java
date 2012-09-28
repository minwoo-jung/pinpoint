package com.profiler.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class TestBeforeInterceptor implements StaticBeforeInterceptor {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

        public int call = 0;
        public Object target;
        public String className;
        public String methodName;
        public Object[] args;

        @Override
        public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
            logger.info("before target:" + target  + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
            this.target = target;
            this.className = className;
            this.methodName = methodName;
            this.args = args;
            call++;
        }
}
