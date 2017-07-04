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

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitorRegistry;
import com.navercorp.pinpoint.plugin.cp.nbasetcp.NbasetCpMonitorAccessor;
import com.navercorp.pinpoint.plugin.cp.nbasetcp.NbasetCpMonitor;

/**
 * @author Taejin Koo
 */
public class CloseInterceptor implements AroundInterceptor {

    private final DataSourceMonitorRegistry dataSourceMonitorRegistry;

    public CloseInterceptor(DataSourceMonitorRegistry dataSourceMonitorRegistry) {
        this.dataSourceMonitorRegistry = dataSourceMonitorRegistry;
    }

    @Override
    public void before(Object target, Object[] args) {
        if ((target instanceof NbasetCpMonitorAccessor)) {
            NbasetCpMonitor cpMonitor = ((NbasetCpMonitorAccessor) target)._$PINPOINT$_getConnectionPoolMonitor();
            if (cpMonitor != null) {
                ((NbasetCpMonitorAccessor) target)._$PINPOINT$_setConnectionPoolMonitor(null);
                cpMonitor.close();
                dataSourceMonitorRegistry.unregister(cpMonitor);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

    }

}

