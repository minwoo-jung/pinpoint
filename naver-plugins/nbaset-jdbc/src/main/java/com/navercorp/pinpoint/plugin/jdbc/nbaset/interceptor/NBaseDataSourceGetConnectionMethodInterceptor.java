/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.jdbc.nbaset.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;


/**
 * @author jaehong.kim
 */
public class NBaseDataSourceGetConnectionMethodInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(NBaseDataSourceGetConnectionMethodInterceptor.class);
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;

    public NBaseDataSourceGetConnectionMethodInterceptor(TraceContext context, MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        this.traceContext = context;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        final boolean success = InterceptorUtils.isSuccess(throwable);
        if (success && target instanceof DatabaseInfoAccessor && result instanceof DatabaseInfoAccessor) {
            DatabaseInfo databaseInfo = ((DatabaseInfoAccessor) target)._$PINPOINT$_getDatabaseInfo();
            ((DatabaseInfoAccessor) result)._$PINPOINT$_setDatabaseInfo(databaseInfo);
        }
    }
}
