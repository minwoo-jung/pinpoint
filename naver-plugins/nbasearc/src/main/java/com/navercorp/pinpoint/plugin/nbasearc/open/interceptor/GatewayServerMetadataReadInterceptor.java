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
package com.navercorp.pinpoint.plugin.nbasearc.open.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.nbasearc.DestinationIdAccessor;
import com.navercorp.pinpoint.plugin.nbasearc.EndPointAccessor;
import com.navercorp.redis.cluster.gateway.GatewayServer;


/**
 * Trace destinationId & endPoint
 * 
 * @author jaehong.kim
 *
 */
public abstract class GatewayServerMetadataReadInterceptor implements AroundInterceptor {

    protected final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    protected final boolean isDebug = logger.isDebugEnabled();


    public GatewayServerMetadataReadInterceptor() {
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            if (!validate(target, args)) {
                return;
            }

            // trace destinationId & endPoint
            // first argument is GatewayServer
            final GatewayServer server = (GatewayServer) args[0];
            final String endPoint = HostAndPort.toHostAndPortString(server.getAddress().getHost(), server.getAddress().getPort());
            ((EndPointAccessor)target)._$PINPOINT$_setEndPoint(endPoint);

            final String destinationId = ((DestinationIdAccessor)args[0])._$PINPOINT$_getDestinationId();
            if (destinationId != null) {
                ((DestinationIdAccessor)target)._$PINPOINT$_setDestinationId(destinationId);
            }
        } catch (Throwable t) {
            logger.warn("Failed to BEFORE process. {}", t.getMessage(), t);
        }
    }

    private boolean validate(final Object target, final Object[] args) {
        if (!(target instanceof DestinationIdAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", DestinationIdAccessor.class.getName());
            }
            return false;
        }

        if (!(target instanceof EndPointAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", DestinationIdAccessor.class.getName());
            }
            return false;
        }

        if (args == null || args.length == 0 || args[0] == null) {
            if (isDebug) {
                logger.debug("Invalid arguments. Null or not found args({}).", args);
            }
            return false;
        }

        if (!(args[0] instanceof GatewayServer)) {
            if (isDebug) {
                logger.debug("Invalid arguments. Expect GatewayConfig but args[0]({}).", args[0]);
            }
            return false;
        }

        if (!(args[0] instanceof DestinationIdAccessor)) {
            if (isDebug) {
                logger.debug("Invalid args[0]({}) object. Need field accessor({})", args[0], DestinationIdAccessor.class.getName());
            }
            return false;
        }

        return true;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
