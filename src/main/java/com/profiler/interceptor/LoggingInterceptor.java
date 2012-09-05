package com.profiler.interceptor;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingInterceptor implements StaticAroundInterceptor {

	private final Logger logger;

	public LoggingInterceptor(String loggerName) {
		this.logger = Logger.getLogger(loggerName);
	}

	@Override
	public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("before " + target.toString() + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
		}
	}

	@Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("after " + target.toString() + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
		}
	}
}
