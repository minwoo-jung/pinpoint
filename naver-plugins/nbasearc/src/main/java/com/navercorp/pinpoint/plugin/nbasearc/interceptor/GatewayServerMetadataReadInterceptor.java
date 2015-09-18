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
package com.navercorp.pinpoint.plugin.nbasearc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.nbasearc.DestinationIdAccessor;
import com.navercorp.pinpoint.plugin.nbasearc.EndPointAccessor;
import com.navercorp.pinpoint.plugin.nbasearc.NbaseArcConstants;
import com.nhncorp.redis.cluster.gateway.GatewayServer;

/**
 * Trace destinationId & endPoint
 * 
 * @author jaehong.kim
 *
 */
public abstract class GatewayServerMetadataReadInterceptor implements AroundInterceptor, NbaseArcConstants {

    protected final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    protected MethodDescriptor methodDescriptor;

    public GatewayServerMetadataReadInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, target.getClass().getName(), methodDescriptor.getMethodName(), methodDescriptor.getParameterDescriptor(), args);
        }

        try {
            if (!validate(target, args)) {
                return;
            }

            // trace destinationId & endPoint
            // first argument is GatewayServer
            final GatewayServer server = (GatewayServer) args[0];
            final String endPoint = server.getAddress().getHost() + ":" + server.getAddress().getPort();
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
                logger.debug("Invalid target object. Need field accessor({}).", METADATA_DESTINATION_ID);
            }
            return false;
        }

        if (!(target instanceof EndPointAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", METADATA_END_POINT);
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
                logger.debug("Invalid args[0]({}) object. Need field accessor({})", args[0], METADATA_DESTINATION_ID);
            }
            return false;
        }

        return true;
    }

    @Override
    public void after(Object target, Object result, Throwable throwable, Object[] args) {
    }
}
