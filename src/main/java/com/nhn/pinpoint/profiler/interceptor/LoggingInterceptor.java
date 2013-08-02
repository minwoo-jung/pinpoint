package com.nhn.pinpoint.profiler.interceptor;

import com.nhn.pinpoint.profiler.util.StringUtils;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingInterceptor implements StaticAroundInterceptor, SimpleAroundInterceptor {

	private final Logger logger;

	public LoggingInterceptor(String loggerName) {
		this.logger = Logger.getLogger(loggerName);
	}

	@Override
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
		}
	}

	@Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
		}
	}

    @Override
    public void before(Object target, Object[] args) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("before " + StringUtils.toString(target) + " args:" + Arrays.toString(args) );
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("after " + StringUtils.toString(target) + " args:" + Arrays.toString(args) + " result:" + result);
        }
    }

}
