package com.profiler.modifier.db.mysql.interceptors;

import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;

public class CreateConnectionInterceptor implements StaticAfterInterceptor {

	private final Logger logger = Logger.getLogger(CreateConnectionInterceptor.class.getName());
    private final MetaObject setUrl = new MetaObject("__setUrl", String.class);
	@Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("after " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
		}
		if (!InterceptorUtils.isSuccess(result)) {
			return;
		}

		if (result instanceof Connection) {
			Object url = args[4];
			if (url instanceof String) {
                this.setUrl.invoke(result, url);
			}
		}
	}
}
