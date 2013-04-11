package com.profiler.modifier.arcus.interceptors;

import com.profiler.logging.Logger;

import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.logging.LoggerFactory;
import com.profiler.logging.LoggingUtils;
import com.profiler.util.MetaObject;

/**
 * 
 * @author netspider
 * 
 */
public class CacheManagerConstructInterceptor implements StaticAfterInterceptor {

	private final Logger logger = LoggerFactory.getLogger(CacheManagerConstructInterceptor.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

	private MetaObject<Object> setServiceCode = new MetaObject<Object>("__setServiceCode", String.class);

	@Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		if (isDebug) {
			LoggingUtils.logAfter(logger, target, className, methodName, parameterDescription, args, result);
		}

		setServiceCode.invoke(target, (String) args[1]);
	}
}
