/**
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetUtils;
import com.navercorp.pinpoint.plugin.lucy.net.npc.NpcServerAddressAccessor;
import com.nhncorp.lucy.npc.DefaultNpcMessage;
import com.nhncorp.lucy.npc.UserOptionIndex;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class MakeMessageInterceptor implements AroundInterceptor {

    private static final int DEFAULT_MAX_USER_OPTIONS_SET_INDEX = 3;

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    public MakeMessageInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
    }
    
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if (trace.canSampled()) {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            TraceId id = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(id.getSpanId());
            if (result instanceof com.nhncorp.lucy.npc.DefaultNpcMessage) {
                recorder.recordServiceType(LucyNetConstants.NPC_CLIENT);
                String endPoint = LucyNetConstants.UNKOWN_ADDRESS;
                if (target instanceof NpcServerAddressAccessor) {
                    InetSocketAddress serverAddress = ((NpcServerAddressAccessor) target)._$PINPOINT$_getNpcServerAddress();
                    if (serverAddress != null) {
                        int port = serverAddress.getPort();
                        endPoint = getHostAddress(serverAddress) + ((port > 0) ? ":" + port : "");
                    }
                }
                recorder.recordDestinationId(endPoint);

                List<byte[]> options = LucyNetUtils.createOptions(id, traceContext.getApplicationName(), traceContext.getServerTypeCode(), endPoint);
                putOption((DefaultNpcMessage) result, options);
            } else {
                recorder.recordDestinationId(LucyNetConstants.UNKOWN_ADDRESS);
            }
        } else {
            if (result instanceof com.nhncorp.lucy.npc.DefaultNpcMessage) {
                List<byte[]> options = LucyNetUtils.createUnsampledOptions();
                putOption((DefaultNpcMessage) result, options);
            }
        }
    }

    private String getHostAddress(InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) {
            return null;
        }
        // TODO JDK 1.7 InetSocketAddress.getHostString();
        // Warning : Avoid unnecessary DNS lookup  (warning:InetSocketAddress.getHostName())
        final InetAddress inetAddress = inetSocketAddress.getAddress();
        if (inetAddress == null) {
            return null;
        }
        return inetAddress.getHostAddress();
    }

    private boolean putOption(DefaultNpcMessage npcMessage, List<byte[]> options) {
        for (byte[] option : options) {
            UserOptionIndex optionIndex = findAvaiableOptionIndex(npcMessage);
            if (optionIndex == null) {
                return false;
            }

            npcMessage.setUserOption(optionIndex, option);
        }
        return true;
    }
    
    private UserOptionIndex findAvaiableOptionIndex(DefaultNpcMessage npcMessage) {
        return findAvaiableOptionIndex(npcMessage, new UserOptionIndex(1, 0), DEFAULT_MAX_USER_OPTIONS_SET_INDEX);
    }

    private UserOptionIndex findAvaiableOptionIndex(DefaultNpcMessage npcMessage, UserOptionIndex optionIndex, int maxUserOptionSetIndex) {
        int optionSetIndex = optionIndex.getOptionSetIndex();

        if (optionSetIndex == maxUserOptionSetIndex) {
            return null;
        }
        
        byte[] data = npcMessage.getUserOption(optionIndex);
        if (data == null) {
            return optionIndex;
        }

        int flagIndex = optionIndex.getFlagIndex() + 1;
        if (flagIndex == 32) {
            return findAvaiableOptionIndex(npcMessage, new UserOptionIndex(optionSetIndex + 1, 0), maxUserOptionSetIndex);
        } else {
            return findAvaiableOptionIndex(npcMessage, new UserOptionIndex(optionSetIndex, flagIndex), maxUserOptionSetIndex);
        }
    }

}
