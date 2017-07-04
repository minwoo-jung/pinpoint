/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.cp.nbasetcp.interceptor;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitorRegistry;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.plugin.cp.nbasetcp.NbasetCpMonitorAccessor;
import com.navercorp.pinpoint.plugin.cp.nbasetcp.NbasetCpConstants;
import com.navercorp.pinpoint.plugin.cp.nbasetcp.NbasetCpMonitor;

/**
 * @author Taejin Koo
 */
public class ConstructorInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final TraceContext traceContext;
    private final DataSourceMonitorRegistry dataSourceMonitorRegistry;

    public ConstructorInterceptor(TraceContext traceContext, DataSourceMonitorRegistry dataSourceMonitorRegistry) {
        this.traceContext = traceContext;
        this.dataSourceMonitorRegistry = dataSourceMonitorRegistry;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (!InterceptorUtils.isSuccess(throwable)) {
            return;
        }

        traceContext.getJdbcContext().parseJdbcUrl(NbasetCpConstants.SERVICE_TYPE, NbasetCpConstants.CP_URL);

        try {
            NbasetCpMonitor cpMonitor = new NbasetCpMonitor(target);
            dataSourceMonitorRegistry.register(cpMonitor);
            if (target instanceof NbasetCpMonitorAccessor) {
                ((NbasetCpMonitorAccessor) target)._$PINPOINT$_setConnectionPoolMonitor(cpMonitor);
            }
        } catch (Exception e) {
            logger.info("failed while creating NbasetCpMonitor. message:{}", e.getMessage(), e);
        }
    }

}

