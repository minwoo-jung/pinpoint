package com.nhn.pinpoint.profiler.modifier.tomcat.interceptor;

import com.nhn.pinpoint.profiler.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.profiler.logging.PLogger;
import com.nhn.pinpoint.profiler.logging.PLoggerFactory;

import com.nhn.pinpoint.profiler.LifeCycleEventListener;

/**
 *
 */
public class StandardServiceStopInterceptor implements SimpleAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private LifeCycleEventListener lifeCycleEventListener;

    public StandardServiceStopInterceptor(LifeCycleEventListener lifeCycleEventListener) {
        this.lifeCycleEventListener = lifeCycleEventListener;
    }

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result);
        }
		// TODO 시작이 실패했을때 stop이 불러 지는가?
		// if (!InterceptorUtils.isSuccess(result)) {
		// return;
		// }
        lifeCycleEventListener.stop();
    }
}
