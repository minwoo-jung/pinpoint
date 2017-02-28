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

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.plugin.jdbc.nbaset.NbasetConstants;


/**
 * @author jaehong.kim
 */
public class NBaseDataSourceCreateMethodInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(NBaseDataSourceCreateMethodInterceptor.class);
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;

    public NBaseDataSourceCreateMethodInterceptor(TraceContext context, MethodDescriptor descriptor) {
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
        if (success && args != null && args.length >= 1 && args[0] instanceof String && result instanceof DatabaseInfoAccessor) {
            final String driverUrl = (String) args[0];
            DatabaseInfo databaseInfo = createDatabaseInfo(driverUrl);
            ((DatabaseInfoAccessor) result)._$PINPOINT$_setDatabaseInfo(databaseInfo);
        }
    }

    private DatabaseInfo createDatabaseInfo(String url) {
        if (url == null) {
            return UnKnownDatabaseInfo.INSTANCE;
        }

        final DatabaseInfo databaseInfo = traceContext.getJdbcContext().parseJdbcUrl(NbasetConstants.NBASET, url);
        if (isDebug) {
            logger.debug("parse DatabaseInfo:{}", databaseInfo);
        }

        return databaseInfo;
    }
}