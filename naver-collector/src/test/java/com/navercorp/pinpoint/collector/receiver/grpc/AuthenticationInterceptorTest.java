/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.collector.receiver.grpc.security.AuthenticationInterceptor;
import com.navercorp.pinpoint.grpc.security.server.AuthContext;
import com.navercorp.pinpoint.grpc.security.server.AuthKeyHolder;
import com.navercorp.pinpoint.grpc.security.server.AuthState;
import com.navercorp.pinpoint.grpc.security.server.DefaultAuthStateContext;
import com.navercorp.pinpoint.grpc.security.server.GrpcSecurityAttribute;
import com.navercorp.pinpoint.grpc.security.server.GrpcSecurityContext;
import com.navercorp.pinpoint.grpc.security.server.GrpcSecurityMetadata;

import io.grpc.Attributes;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.StatusRuntimeException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Taejin Koo
 */
@ContextConfiguration("classpath:applicationContext-tokenauth-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Profile("tokenAuthentication")
public class AuthenticationInterceptorTest {

    private static final String SUCCESS_KEY = "authTest!@#123";

    private static final String FAIL_KEY = "authTest!@#123" + " fail";

    @Autowired
    @Qualifier("authenticationInterceptor")
    private AuthenticationInterceptor authenticationInterceptor;

    private Context prevContext;

    @Before
    public void setUp() throws Exception {
        Context root = Context.ROOT;
        prevContext = root.attach();
    }

    @After
    public void tearDown() throws Exception {
        Context root = Context.ROOT;
        if (prevContext != null) {
            root.detach(prevContext);
        }
    }

    // authenticate -> success -> communication
    @Test
    public void authSuccessTest() {
        ServerCall serverCall = createMockServerCall();

        Metadata metadata = new Metadata();
        GrpcSecurityMetadata.setAuthKey(metadata, SUCCESS_KEY);

        ServerCallHandler mockServerCallHandler = createMockServerCallHandler();

        authenticationInterceptor.interceptCall(serverCall, metadata, mockServerCallHandler);

        AuthKeyHolder authKeyHolder = GrpcSecurityContext.getAuthKeyHolder();

        Assert.assertNotNull(authKeyHolder);
        Assert.assertTrue(SUCCESS_KEY.equals(authKeyHolder.getKey()));

        AuthContext authContext = GrpcSecurityAttribute.getAuthContext(serverCall.getAttributes());
        Assert.assertTrue(authContext.getState() == AuthState.SUCCESS);
    }

    // success -> communication (skip authentication)
    @Test
    public void authSuccessTest2() {
        ServerCall serverCall = createMockServerCall(AuthState.SUCCESS);

        Metadata metadata = new Metadata();

        ServerCallHandler mockServerCallHandler = createMockServerCallHandler();

        // skip authenticate
        authenticationInterceptor.interceptCall(serverCall, metadata, mockServerCallHandler);
    }

    // authenticate -> fail -> throws exception
    @Test
    public void authFailTest() {
        ServerCall serverCall = createMockServerCall();

        Metadata metadata = new Metadata();
        GrpcSecurityMetadata.setAuthKey(metadata, FAIL_KEY);

        ServerCallHandler mockServerCallHandler = createMockServerCallHandler();

        try {
            authenticationInterceptor.interceptCall(serverCall, metadata, mockServerCallHandler);
            Assert.fail();
        } catch (StatusRuntimeException e) {
        }

        AuthKeyHolder authKeyHolder = GrpcSecurityContext.getAuthKeyHolder();
        Assert.assertNull(authKeyHolder);

        AuthContext authContext = GrpcSecurityAttribute.getAuthContext(serverCall.getAttributes());
        Assert.assertTrue(authContext.getState() == AuthState.FAIL);
    }

    // fail -> throws exception (skip authentication)
    @Test
    public void authFailTest2() {
        ServerCall serverCall = createMockServerCall(AuthState.FAIL);

        Metadata metadata = new Metadata();
        GrpcSecurityMetadata.setAuthKey(metadata, SUCCESS_KEY);

        ServerCallHandler mockServerCallHandler = createMockServerCallHandler();

        // do not check key
        try {
            authenticationInterceptor.interceptCall(serverCall, metadata, mockServerCallHandler);
            Assert.fail();
        } catch (StatusRuntimeException e) {
        }
    }

    private ServerCall createMockServerCall() {
        return createMockServerCall(AuthState.NONE);
    }

    private ServerCall createMockServerCall(AuthState updateWantedState) {
        Attributes attributes = GrpcSecurityAttribute.setAuthContext(Attributes.newBuilder().build());

        if (updateWantedState != AuthState.NONE) {
            AuthContext authContext = GrpcSecurityAttribute.getAuthContext(attributes);
            if (authContext instanceof DefaultAuthStateContext) {
                ((DefaultAuthStateContext) authContext).changeState(updateWantedState);
            }
        }

        ServerCall serverCall = Mockito.mock(ServerCall.class);
        Mockito.when(serverCall.getAttributes()).thenReturn(attributes);
        return serverCall;
    }

    private ServerCallHandler createMockServerCallHandler() {
        return Mockito.mock(ServerCallHandler.class);
    }

}
