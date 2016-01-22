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
package com.navercorp.pinpoint.plugin.redis.interceptor;

import redis.clients.jedis.Client;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger_제거예정;
import com.navercorp.pinpoint.bootstrap.logging.SLF4jLoggerFactory;
import com.navercorp.pinpoint.plugin.redis.EndPointAccessor;
import com.navercorp.pinpoint.plugin.redis.RedisConstants;

/**
 * 
 * @author jaehong.kim
 *
 */
public abstract class JedisClientMetadataAttchInterceptor implements AroundInterceptor {
    private final PLogger_제거예정 logger = SLF4jLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    public JedisClientMetadataAttchInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
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

            final String endPoint = ((EndPointAccessor) args[0])._$PINPOINT$_getEndPoint();
            if (endPoint != null) {
                ((EndPointAccessor) target)._$PINPOINT$_setEndPoint(endPoint);
            }
        } catch (Throwable t) {
            logger.warn("Failed to BEFORE process. {}", t.getMessage(), t);
        }
    }

    private boolean validate(final Object target, final Object[] args) {
        if (args == null || args.length == 0 || args[0] == null) {
            logger.debug("Invalid arguments. Null or not found args({}).", args);
            return false;
        }

        if (!(args[0] instanceof Client)) {
            logger.debug("Invalid arguments. Expect Client but args[0]({}).", args[0]);
            return false;
        }

        if (!(args[0] instanceof EndPointAccessor)) {
            logger.debug("Invalid args[0] object. Need field accessor({}).", EndPointAccessor.class.getName());
            return false;
        }

        if (!(target instanceof EndPointAccessor)) {
            logger.debug("Invalid target object. Need field accessor({}).", EndPointAccessor.class.getName());
            return false;
        }

        return true;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }
}