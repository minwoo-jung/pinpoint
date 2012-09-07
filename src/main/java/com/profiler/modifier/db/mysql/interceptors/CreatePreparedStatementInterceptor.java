package com.profiler.modifier.db.mysql.interceptors;

import com.profiler.context.Trace;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.modifier.db.ConnectionTrace;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;

import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreatePreparedStatementInterceptor implements StaticAfterInterceptor {
    private final Logger logger = Logger.getLogger(CreatePreparedStatementInterceptor.class.getName());

    private final MetaObject setUrl = new MetaObject("__setUrl", String.class);
    private final MetaObject setSql = new MetaObject("__setSql", String.class);

	@Override
	public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("after " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args) + " result:" + result);
		}
        if(!InterceptorUtils.isSuccess(result)) {
            return;
        }
		if (Trace.getCurrentTraceId() == null) {
			return;
		}
		if (target instanceof Connection) {
			ConnectionTrace connectionTrace = ConnectionTrace.getConnectionTrace();
			String connectionUrl = connectionTrace.getConnectionUrl((Connection) target);
            this.setUrl.invoke(result, connectionUrl);
            String sql = (String) args[0];
            this.setSql.invoke(result, sql);
		}
	}


}
