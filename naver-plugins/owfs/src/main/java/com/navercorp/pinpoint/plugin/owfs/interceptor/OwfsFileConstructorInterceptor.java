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

package com.navercorp.pinpoint.plugin.owfs.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.owfs.DestinationIdAccessor;
import com.navercorp.pinpoint.plugin.owfs.EndPointAccessor;

/**
 * @author jaehong.kim
 */
public class OwfsFileConstructorInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public OwfsFileConstructorInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
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
            final String destinationId = ((DestinationIdAccessor) args[0])._$PINPOINT$_getDestinationId();
            if (destinationId != null) {
                // set target
                ((DestinationIdAccessor) target)._$PINPOINT$_setDestinationId(destinationId);
                if (isDebug) {
                    logger.debug("Set destinationId {}", destinationId);
                }
            }
            final String endPoint = ((EndPointAccessor) args[0])._$PINPOINT$_getEndPoint();
            if (endPoint != null) {
                // set target
                ((EndPointAccessor) target)._$PINPOINT$_setEndPoint(endPoint);
                if (isDebug) {
                    logger.debug("Set endPoint {}", endPoint);
                }
            }
        } catch (Throwable t) {
            logger.warn("Failed to BEFORE process. {}", t.getMessage(), t);
        }
    }

    private boolean validate(final Object target, final Object[] args) {
        if (args == null || args.length == 0) {
            if (isDebug) {
                logger.debug("Invalid arguments. Null or not found args({}).", args);
            }
            return false;
        }
        // args[0]
        if (!(args[0] instanceof DestinationIdAccessor)) {
            if (isDebug) {
                logger.debug("Invalid args[0]. Need filed accessor({})", DestinationIdAccessor.class.getName());
            }
            return false;
        }

        if (!(args[0] instanceof EndPointAccessor)) {
            if (isDebug) {
                logger.debug("Invalid args[0]. Need field accessor({}).", EndPointAccessor.class.getName());
            }
            return false;
        }
        // target
        if (!(target instanceof DestinationIdAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target. Need filed accessor({})", DestinationIdAccessor.class.getName());
            }
            return false;
        }

        if (!(target instanceof EndPointAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", EndPointAccessor.class.getName());
            }
            return false;
        }
        return true;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}
