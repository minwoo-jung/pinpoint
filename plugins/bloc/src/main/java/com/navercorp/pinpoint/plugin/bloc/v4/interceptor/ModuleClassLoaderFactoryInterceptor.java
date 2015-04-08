package com.navercorp.pinpoint.plugin.bloc.v4.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.TargetConstructor;
import com.nhncorp.lucy.bloc.core.clazz.ModuleClassLoaderFactory;

@TargetConstructor
public class ModuleClassLoaderFactoryInterceptor implements SimpleAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
        
    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }
        
        ModuleClassLoaderFactory moduleClassLoaderFactory = (ModuleClassLoaderFactory)target;
        moduleClassLoaderFactory.addManagedPackage("com.navercorp.pinpoint.bootstrap");
        moduleClassLoaderFactory.addManagedPackage("com.navercorp.pinpoint.common");
        
    }

}
