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

package com.navercorp.pinpoint.plugin.lucy.net.nimm.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.common.Charsets;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetHeader;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.nhncorp.lucy.net.call.DefaultCall;
import com.nhncorp.lucy.npc.UserOptionIndex;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Author Taejin Koo
 */
public class EncodeMessageInterceptorTest {

    private static final Charset UTF_8_CHARSET = Charsets.UTF_8;

    private static MethodDescriptor methodDescriptor;
    private static InterceptorScope scope;
    private static int transactionId = 0;
    private static DefaultCall call;

    Set<String> checkDuplicateUserOptionIndex = new HashSet<String>();
    Set<String> checkDuplicateUserOptionKey = new HashSet<String>();

    @BeforeClass
    public static void setUpBeforeClass() {
        InterceptorScopeInvocation mockInterceptorScopeInvocation = mock(InterceptorScopeInvocation.class);
        when(mockInterceptorScopeInvocation.getAttachment()).thenReturn(new DefaultTraceId("agentId", System.currentTimeMillis(), transactionId++));

        methodDescriptor = mock(MethodDescriptor.class);
        scope = mock(InterceptorScope.class);
        when(scope.getCurrentInvocation()).thenReturn(mockInterceptorScopeInvocation);

        call = new DefaultCall();
        call.setObjectName("objectName");
        call.setMethodName("methodName");
        call.setParameters("hi");
    }

    @Before
    public void setUpBefore() {
        checkDuplicateUserOptionIndex.clear();
        checkDuplicateUserOptionKey.clear();
    }

    @Test
    public void encodeMessageInterceptorTest1() throws Exception {
        TraceContext traceContext = createMockTraceContext(true);

        Map<UserOptionIndex, byte[]> option = new HashMap<UserOptionIndex, byte[]>();

        EncodeMesssageInterceptor encodeMessageInteceptor = new EncodeMesssageInterceptor(traceContext, methodDescriptor, scope);
        encodeMessageInteceptor.before(null, new Object[]{option, call});

        checkOptions(option);

        Assert.assertEquals(LucyNetHeader.values().length, checkDuplicateUserOptionIndex.size());
        Assert.assertEquals(LucyNetHeader.values().length, checkDuplicateUserOptionKey.size());
    }

    @Test
    public void encodeMessageInterceptorTest2() throws Exception {
        TraceContext traceContext = createMockTraceContext(false);

        Map<UserOptionIndex, byte[]> option = new HashMap<UserOptionIndex, byte[]>();

        EncodeMesssageInterceptor encodeMessageInteceptor = new EncodeMesssageInterceptor(traceContext, methodDescriptor, scope);
        encodeMessageInteceptor.before(null, new Object[]{option, call});

        checkOptions(option);

        Assert.assertEquals(1, checkDuplicateUserOptionIndex.size());
        Assert.assertEquals(1, checkDuplicateUserOptionKey.size());
    }

    @Test
    public void encodeMessageInterceptorTest3() throws Exception {
        TraceContext traceContext = createMockTraceContext(true);

        Map<UserOptionIndex, byte[]> option = new HashMap<UserOptionIndex, byte[]>();
        option.put(new UserOptionIndex(1, 3), new String(LucyNetHeader.PINPOINT_HOST.toString() + "=test").getBytes(UTF_8_CHARSET));
        option.put(new UserOptionIndex(1, 1), new String(LucyNetHeader.PINPOINT_PARENT_APPLICATION_TYPE.toString() + "=21").getBytes(UTF_8_CHARSET));
        option.put(new UserOptionIndex(1, 5), new String(LucyNetHeader.PINPOINT_SAMPLED.toString() + "=" + SamplingFlagUtils.SAMPLING_RATE_TRUE).getBytes(UTF_8_CHARSET));

        EncodeMesssageInterceptor encodeMessageInteceptor = new EncodeMesssageInterceptor(traceContext, methodDescriptor, scope);
        encodeMessageInteceptor.before(null, new Object[]{option, call});

        checkOptions(option);

//        for (UserOptionIndex index : option.keySet()) {
//            byte[] bytes = option.get(index);
//
//            System.out.println(index.getOptionSetIndex() + ":" + index.getFlagIndex() + " " + new String(bytes));
//
//        }

        Assert.assertEquals(LucyNetHeader.values().length, checkDuplicateUserOptionIndex.size());
        Assert.assertEquals(LucyNetHeader.values().length, checkDuplicateUserOptionKey.size());
    }

    @Test
    public void encodeMessageInterceptorTest4() throws Exception {
        TraceContext traceContext = createMockTraceContext(true);

        Map<UserOptionIndex, byte[]> option = new HashMap<UserOptionIndex, byte[]>();
        for (int i = 0; i < 30; i++) {
            option.put(new UserOptionIndex(1, i), "".getBytes());
        }


        EncodeMesssageInterceptor encodeMessageInteceptor = new EncodeMesssageInterceptor(traceContext, methodDescriptor, scope);
        encodeMessageInteceptor.before(null, new Object[]{option, call});

        checkOptions(option);

        Assert.assertEquals(LucyNetHeader.values().length, checkDuplicateUserOptionIndex.size());
        Assert.assertEquals(LucyNetHeader.values().length, checkDuplicateUserOptionKey.size());
    }

    private TraceContext createMockTraceContext(boolean sampled) {
        DefaultTraceId traceId = new DefaultTraceId("agentId", System.currentTimeMillis(), transactionId++);

        Trace mockTrace = mock(Trace.class);
        when(mockTrace.canSampled()).thenReturn(sampled);
        when(mockTrace.getTraceId()).thenReturn(traceId);

        TraceContext traceContext = mock(TraceContext.class);
        when(traceContext.getApplicationName()).thenReturn("Application");
        when(traceContext.getServerTypeCode()).thenReturn(ServiceType.TEST.getCode());
        when(traceContext.currentTraceObject()).thenReturn(mockTrace);

        return traceContext;
    }

    private void checkOptions(Map<UserOptionIndex, byte[]> option) {
        for (Map.Entry<UserOptionIndex, byte[]> entry : option.entrySet()) {
            UserOptionIndex userOptionIndex = entry.getKey();
            String pinpointOptionKey = getPinpointOptionKey(entry.getValue());

            String userOptionIndexAsString = userOptionIndex.getOptionSetIndex() + ":" + userOptionIndex.getFlagIndex();
            if (LucyNetHeader.hasHeader(pinpointOptionKey)) {
                checkDuplicateUserOptionIndex.add(userOptionIndexAsString);
                checkDuplicateUserOptionKey.add(pinpointOptionKey);
            }
        }
    }

    private String getPinpointOptionKey(byte[] optionData) {
        if (optionData == null) {
            return null;
        }

        String option = new String(optionData, UTF_8_CHARSET);
        String[] keyValuePair = option.split("=");

        if (keyValuePair.length == 2) {
            if (LucyNetHeader.hasHeader(keyValuePair[0])) {
                return keyValuePair[0];
            }
        }

        return null;
    }


}
