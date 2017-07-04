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

package com.navercorp.pinpoint.plugin.cp.nbasetcp;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitor;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.cp.nbasetcp.filed.getter.MaxConnPoolSizeGetter;
import com.navercorp.pinpoint.plugin.cp.nbasetcp.filed.getter.NumUsedSocketsGetter;

/**
 * @author Taejin Koo
 */
public class NbasetCpMonitor implements DataSourceMonitor {

    private final NumUsedSocketsGetter numUsedSocketsGetter;
    private final MaxConnPoolSizeGetter maxConnPoolSizeGetter;

    private volatile boolean closed = false;

    public NbasetCpMonitor(Object object) {
        try {
            this.numUsedSocketsGetter = getNumUsedSocketsGetter(object);
            this.maxConnPoolSizeGetter = getMaxConnPoolSizeGetter(object);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private NumUsedSocketsGetter getNumUsedSocketsGetter(Object object) throws NoSuchMethodException {
        if (object instanceof NumUsedSocketsGetter) {
            return (NumUsedSocketsGetter) object;
        } else {
            throw new NoSuchMethodException("object must instanceof NumUsedSocketsGetter");
        }
    }

    private MaxConnPoolSizeGetter getMaxConnPoolSizeGetter(Object object) throws NoSuchMethodException {
        if (object instanceof MaxConnPoolSizeGetter) {
            return (MaxConnPoolSizeGetter) object;
        } else {
            throw new NoSuchMethodException("object must instanceof MaxConnPoolSizeGetter");
        }
    }

    @Override
    public ServiceType getServiceType() {
        return NbasetCpConstants.SERVICE_TYPE;
    }

    @Override
    public String getUrl() {
        return NbasetCpConstants.CP_URL;
    }

    @Override
    public int getActiveConnectionSize() {
        return numUsedSocketsGetter._$PINPOINT$_getNumUsedSockets().get();
    }

    @Override
    public int getMaxConnectionSize() {
        return maxConnPoolSizeGetter._$PINPOINT$_getMaxConnPoolSize().get();
    }

    @Override
    public boolean isDisabled() {
        return closed;
    }

    public void close() {
        closed = true;
    }

}
