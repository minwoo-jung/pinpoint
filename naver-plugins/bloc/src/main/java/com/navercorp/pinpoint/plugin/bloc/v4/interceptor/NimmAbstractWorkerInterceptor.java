/*
 *  Copyright 2015 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.plugin.bloc.v4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.bloc.LucyNetUtils;
import com.navercorp.pinpoint.plugin.bloc.v4.NimmServerSocketAddressAccessor;
import com.nhncorp.lucy.nimm.connector.address.NimmAddress;

/**
 * @Author Taejin Koo
 */
public class NimmAbstractWorkerInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;

    public NimmAbstractWorkerInterceptor(MethodDescriptor descriptor, TraceContext traceContext) {
        this.descriptor = descriptor;
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (args != null && args.length == 1 && args[0] instanceof com.nhncorp.lucy.nimm.connector.NimmNetEndPoint) {
            com.nhncorp.lucy.nimm.connector.NimmNetEndPoint nimmNetEndPoint = (com.nhncorp.lucy.nimm.connector.NimmNetEndPoint) args[0];
            NimmAddress nimmAddress = nimmNetEndPoint.getAddress();

            if (target instanceof NimmServerSocketAddressAccessor) {
                ((NimmServerSocketAddressAccessor) target)._$PINPOINT$_setNimmAddress(LucyNetUtils.nimmAddressToString(nimmAddress));
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
    }

}
