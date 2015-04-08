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

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Cached;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.nbasearc.NbaseArcConstants;
import com.nhncorp.redis.cluster.gateway.GatewayServer;

/**
 * Trace destinationId & endPoint
 * 
 * @author jaehong.kim
 *
 */
public abstract class GatewayServerMetadataReadInterceptor implements SimpleAroundInterceptor, NbaseArcConstants {

    protected final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    protected MetadataAccessor destinationIdAccessor;
    protected MetadataAccessor endPointAccessor;
    protected MethodDescriptor methodDescriptor;

    public GatewayServerMetadataReadInterceptor(TraceContext traceContext, @Cached MethodDescriptor methodDescriptor, @Name(METADATA_DESTINATION_ID) MetadataAccessor destinationIdAccessor, @Name(METADATA_END_POINT) MetadataAccessor endPointAccessor) {
        this.methodDescriptor = methodDescriptor;
        this.destinationIdAccessor = destinationIdAccessor;
        this.endPointAccessor = endPointAccessor;
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
            endPointAccessor.set(target, endPoint);

            final String destinationId = destinationIdAccessor.get(args[0]);
            if (destinationId != null) {
                destinationIdAccessor.set(target, destinationId);
            }
        } catch (Throwable t) {
            logger.warn("Failed to before process. {}", t.getMessage(), t);
        }
    }

    private boolean validate(final Object target, final Object[] args) {
        if (!destinationIdAccessor.isApplicable(target)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need metadata accessor({}).", METADATA_DESTINATION_ID);
            }
            return false;
        }

        if (!endPointAccessor.isApplicable(target)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need metadata accessor({}).", METADATA_END_POINT);
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

        if (!destinationIdAccessor.isApplicable(args[0])) {
            if (isDebug) {
                logger.debug("Invalid args[0]({}) object. Need metadata accessor({})", args[0], METADATA_DESTINATION_ID);
            }
            return false;
        }

        return true;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
