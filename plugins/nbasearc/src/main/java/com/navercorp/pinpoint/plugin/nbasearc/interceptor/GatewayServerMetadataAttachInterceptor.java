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

/**
 * Trace destinationId
 * 
 * @author jaehong.kim
 *
 */
public class GatewayServerMetadataAttachInterceptor implements SimpleAroundInterceptor, NbaseArcConstants {
    protected final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    protected final MetadataAccessor destinationIdAccessor;

    public GatewayServerMetadataAttachInterceptor(TraceContext traceContext, @Cached MethodDescriptor methodDescriptor, @Name(METADATA_DESTINATION_ID) MetadataAccessor destinationIdAccessor) {
        this.destinationIdAccessor = destinationIdAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            if (!validate(target, result)) {
                return;
            }

            final String destinationId = destinationIdAccessor.get(target);
            if (destinationId != null) {
                // result is GatewayServer object
                destinationIdAccessor.set(result, destinationId);
            }
        } catch (Throwable t) {
            logger.warn("Failed to after process. {}", t.getMessage(), t);
        }
    }

    private boolean validate(final Object target, final Object result) {
        if (result == null) {
            if (isDebug) {
                logger.debug("Ignored. Result is null.");
            }
            return false;
        }

        if (!destinationIdAccessor.isApplicable(target)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need metadata accessor({}).", METADATA_DESTINATION_ID);
            }
            return false;
        }

        if (!destinationIdAccessor.isApplicable(result)) {
            if (isDebug) {
                logger.debug("Invalid result object. Need metadata accessor({}).", METADATA_DESTINATION_ID);
            }
            return false;
        }

        return true;
    }
}