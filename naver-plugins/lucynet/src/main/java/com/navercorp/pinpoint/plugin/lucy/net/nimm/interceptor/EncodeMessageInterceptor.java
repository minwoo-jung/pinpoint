/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.lucy.net.nimm.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetHeader;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetUtils;
import com.navercorp.pinpoint.plugin.lucy.net.nimm.NimmAddressAccessor;
import com.nhncorp.lucy.net.call.Call;
import com.nhncorp.lucy.npc.UserOptionIndex;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class EncodeMessageInterceptor implements AroundInterceptor {

    private static final int DEFAULT_MAX_USER_OPTIONS_SET_INDEX = 3;

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final InterceptorScope scope;

    public EncodeMessageInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.scope = scope;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if (args == null || args.length != 2) {
            return;
        }

        if (!(args[0] instanceof Map)) {
            return;
        }

        if (!(args[1] instanceof Call)) {
            return;
        }

        Map optionMap = (Map) args[0];

        String nimmAddress = LucyNetConstants.UNKOWN_ADDRESS;
        if ((target instanceof NimmAddressAccessor)) {
            nimmAddress = ((NimmAddressAccessor) target)._$PINPOINT$_getNimmAddress();
        }

        if (optionMap.size() == 0) {
            addOption(optionMap, trace, nimmAddress, Collections.<String>emptySet());
        } else {
            Collection optionDataSet = optionMap.values();

            Set<String> notIncludeOptionSet = new HashSet<String>(optionDataSet.size());
            for (Object optionData : optionDataSet) {
                if (optionData instanceof byte[]) {
                    notIncludeOptionSet.add(getPinpointOptionKey((byte[]) optionData));
                }
            }

            addOption(optionMap, trace, nimmAddress, notIncludeOptionSet);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
    }

    // 핀포인트 옵션을 생성 및 포함합니다.
    private void addOption(Map optionMap, Trace trace, String nimmAddress, Set<String> notIncludeOptionSet) {
        Object attachment = scope.getCurrentInvocation().getAttachment();
        if (trace.canSampled() && attachment instanceof TraceId) {
            List<byte[]> pinpointOptions = LucyNetUtils.createOptions((TraceId) attachment, traceContext.getApplicationName(), traceContext.getServerTypeCode(), nimmAddress, notIncludeOptionSet);
            addOption0(optionMap, pinpointOptions);
        } else {
            List<byte[]> unsampledOptions = LucyNetUtils.createUnsampledOptions();
            addOption0(optionMap, unsampledOptions);
        }
    }

    private void addOption0(Map userOptions, List<byte[]> options) {
        if (userOptions.size() == 0) {
            for (int i = 0; i < options.size(); i++) {
                userOptions.put(new UserOptionIndex(1, i), options.get(i));
            }
        } else {
            Set<UserOptionIndex> userOptionIndexSet = userOptions.keySet();

            UserOptionIndex userOptionIndex = new UserOptionIndex(1, 0);
            for (byte[] option : options) {
                UserOptionIndex avaiableOptionIndex = findAvaiableOptionIndex(userOptionIndexSet, userOptionIndex);
                if (avaiableOptionIndex == null) {
                    break;
                }
                userOptions.put(avaiableOptionIndex, option);
                userOptionIndex = LucyNetUtils.increaseUserOptionIndex(avaiableOptionIndex);
            }
        }
    }

    private UserOptionIndex findAvaiableOptionIndex(Set<UserOptionIndex> userOptionIndexSet, UserOptionIndex newOptionIndex) {
        return findAvaiableOptionIndex(userOptionIndexSet, newOptionIndex, DEFAULT_MAX_USER_OPTIONS_SET_INDEX);
    }

    private UserOptionIndex findAvaiableOptionIndex(Set<UserOptionIndex> userOptionIndexSet, UserOptionIndex newOptionIndex, int maxUserOptionSetIndex) {
        int optionSetIndex = newOptionIndex.getOptionSetIndex();
        if (optionSetIndex == maxUserOptionSetIndex) {
            return null;
        }

        if (isAvaiableUserOptionIndex(newOptionIndex, userOptionIndexSet)) {
            return newOptionIndex;
        } else {
            return findAvaiableOptionIndex(userOptionIndexSet, LucyNetUtils.increaseUserOptionIndex(newOptionIndex), maxUserOptionSetIndex);
        }
    }

    private boolean isAvaiableUserOptionIndex(UserOptionIndex newUserOptionIndex, Set<UserOptionIndex> userOptionIndexSet) {
        for (UserOptionIndex userOptionIndex : userOptionIndexSet) {
            if (LucyNetUtils.equalsUserOptionIndex(newUserOptionIndex, userOptionIndex)) {
                return false;
            }
        }
        return true;
    }

    private String getPinpointOptionKey(byte[] optionData) {
        if (optionData == null) {
            return null;
        }

        String option = new String(optionData, LucyNetConstants.UTF_8_CHARSET);
        String[] keyValuePair = option.split("=");

        if (keyValuePair.length == 2) {
            if (LucyNetHeader.hasHeader(keyValuePair[0])) {
               return keyValuePair[0];
            }
        }

        return null;
    }

}
