package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.*;
import com.nhn.pinpoint.profiler.interceptor.util.JDBCScope;
import com.nhn.pinpoint.profiler.logging.PLogger;
import com.nhn.pinpoint.profiler.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.util.DepthScope;

/**
 *
 */
public class JDBCScopeDelegateStaticInterceptor implements StaticAroundInterceptor, TraceContextSupport {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final StaticAroundInterceptor delegate;


    public JDBCScopeDelegateStaticInterceptor(StaticAroundInterceptor delegate) {
        if (delegate == null) {
            throw new NullPointerException("delegate must not be null");
        }
        this.delegate = delegate;
    }

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        final int push = JDBCScope.push();
        if (push != DepthScope.ZERO) {
            if (isDebug) {
                logger.debug("push bindValue scope. skip trace. {}", delegate.getClass());
            }
            return;
        }
        this.delegate.before(target, className, methodName, parameterDescription, args);
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        final int pop = JDBCScope.pop();
        if (pop != DepthScope.ZERO) {
            if (isDebug) {
                logger.debug("pop bindValue scope. skip trace. {}", delegate.getClass());
            }
            return;
        }
        this.delegate.after(target, className, methodName, parameterDescription, args, result);
    }


    @Override
    public void setTraceContext(TraceContext traceContext) {
        if (this.delegate instanceof TraceContextSupport) {
            ((TraceContextSupport)this.delegate).setTraceContext(traceContext);
        }
    }
}
