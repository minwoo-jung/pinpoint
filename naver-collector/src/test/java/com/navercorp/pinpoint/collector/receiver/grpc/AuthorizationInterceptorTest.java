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

import com.navercorp.pinpoint.collector.dao.TokenDao;
import com.navercorp.pinpoint.collector.receiver.grpc.security.AuthorizationInterceptor;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.grpc.security.server.AuthContext;
import com.navercorp.pinpoint.grpc.security.server.AuthKeyHolder;
import com.navercorp.pinpoint.grpc.security.server.AuthState;
import com.navercorp.pinpoint.grpc.security.server.AuthTokenHolder;
import com.navercorp.pinpoint.grpc.security.server.DefaultAuthStateContext;
import com.navercorp.pinpoint.grpc.security.server.GrpcSecurityAttribute;
import com.navercorp.pinpoint.grpc.security.server.GrpcSecurityContext;
import com.navercorp.pinpoint.grpc.security.GrpcSecurityMetadata;

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

import java.util.UUID;

/**
 * @author Taejin Koo
 */
@ContextConfiguration("classpath:applicationContext-tokenauth-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Profile("tokenAuthentication")
public class AuthorizationInterceptorTest {

    @Autowired
    @Qualifier("authorizationInterceptor")
    private AuthorizationInterceptor authorizationInterceptor;

    @Autowired
    private TokenDao tokenDao;

    private Context prevContext;

    private String token;

    @Before
    public void setUp() throws Exception {
        Context root = Context.ROOT;
        prevContext = root.attach();

        this.token = UUID.randomUUID().toString();

        Token mockToken = Mockito.mock(Token.class);
        Mockito.when(mockToken.getKey()).thenReturn(token);

        tokenDao.create(mockToken);
    }

    @After
    public void tearDown() throws Exception {
        tokenDao.getAndRemove(token);

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
        GrpcSecurityMetadata.setAuthToken(metadata, token);

        ServerCallHandler mockServerCallHandler = createMockServerCallHandler();

        authorizationInterceptor.interceptCall(serverCall, metadata, mockServerCallHandler);

        AuthTokenHolder authTokenHolder = GrpcSecurityContext.getAuthTokenHolder();

        Assert.assertNotNull(authTokenHolder);
        Assert.assertTrue(token.equals(authTokenHolder.getToken()));

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
        authorizationInterceptor.interceptCall(serverCall, metadata, mockServerCallHandler);
    }

    // authenticate -> fail -> throws exception
    @Test
    public void authFailTest() {
        ServerCall serverCall = createMockServerCall();

        Metadata metadata = new Metadata();
        GrpcSecurityMetadata.setAuthToken(metadata, token + " fail");

        ServerCallHandler mockServerCallHandler = createMockServerCallHandler();

        try {
            authorizationInterceptor.interceptCall(serverCall, metadata, mockServerCallHandler);
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
        GrpcSecurityMetadata.setAuthKey(metadata, token + " fail");

        ServerCallHandler mockServerCallHandler = createMockServerCallHandler();

        // do not check key
        try {
            authorizationInterceptor.interceptCall(serverCall, metadata, mockServerCallHandler);
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
