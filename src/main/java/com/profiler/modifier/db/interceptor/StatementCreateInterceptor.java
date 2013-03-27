package com.profiler.modifier.db.interceptor;

import com.profiler.context.DefaultTraceContext;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.logging.LoggingUtils;
import com.profiler.modifier.db.util.DatabaseInfo;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;

import java.sql.Connection;
import java.util.logging.Logger;

public class StatementCreateInterceptor implements StaticAfterInterceptor {

    private final Logger logger = Logger.getLogger(StatementCreateInterceptor.class.getName());
    private final boolean isDebug = LoggingUtils.isDebug(logger);

    // connection 용.
    private final MetaObject<Object> getUrl = new MetaObject<Object>("__getUrl");

    private final MetaObject setUrl = new MetaObject("__setUrl", Object.class);

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        if (isDebug) {
            LoggingUtils.logAfter(logger, target, className, methodName, parameterDescription, args, result);
        }
        if (JDBCScope.isInternal()) {
            logger.fine("internal jdbc scope. skip trace");
            return;
        }
        if (!InterceptorUtils.isSuccess(result)) {
            return;
        }
        TraceContext traceContext = DefaultTraceContext.getTraceContext();
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        if (target instanceof Connection) {
            DatabaseInfo databaseInfo = (DatabaseInfo) getUrl.invoke(target);
            setUrl.invoke(result, databaseInfo);
        }
    }

}
