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
import com.navercorp.pinpoint.collector.receiver.grpc.security.AuthorizationInterceptor;
import com.navercorp.pinpoint.collector.receiver.grpc.security.service.AuthTokenService;
import com.navercorp.pinpoint.grpc.auth.AuthGrpc;
import com.navercorp.pinpoint.grpc.auth.PAuthCode;
import com.navercorp.pinpoint.grpc.auth.PCmdGetTokenRequest;
import com.navercorp.pinpoint.grpc.auth.PCmdGetTokenResponse;
import com.navercorp.pinpoint.grpc.auth.PSecurityResult;
import com.navercorp.pinpoint.grpc.auth.PTokenType;
import com.navercorp.pinpoint.grpc.security.client.AuthenticationKeyInterceptor;
import com.navercorp.pinpoint.grpc.security.client.AuthorizationTokenInterceptor;
import com.navercorp.pinpoint.grpc.security.server.SecurityServerTransportFilter;
import com.navercorp.pinpoint.grpc.server.MetadataServerTransportFilter;
import com.navercorp.pinpoint.grpc.server.TransportMetadataFactory;
import com.navercorp.pinpoint.grpc.server.TransportMetadataServerInterceptor;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;

import io.grpc.BindableService;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.netty.InternalNettyChannelBuilder;
import io.grpc.netty.InternalNettyServerBuilder;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Taejin Koo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-tokenauth-test.xml")
@ActiveProfiles("tokenAuthentication")
public class TokenServiceTest {

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(100, 1000);

    // refer file /resources/metadata_dummy.txt
    private static final String AUTH_KEY = "test";

    private Server authenticationServer;
    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;
    @Autowired
    private AuthTokenService authTokenService;


    private Server authorizationServer;
    @Autowired
    private AuthorizationInterceptor authorizationInterceptor;

    @Before
    public void start() throws IOException {
        authenticationServer = createAndStartServer("authenticationServer", authenticationInterceptor, authTokenService);

        authorizationServer = createAndStartServer("authorizationServer", authorizationInterceptor, new TestGeeterService());
    }

    private Server createAndStartServer(String serverName, ServerInterceptor serverInterceptor, BindableService bindableService) throws IOException {
        com.navercorp.pinpoint.common.util.Assert.requireNonNull(serverName, "serverName must not be null");
        com.navercorp.pinpoint.common.util.Assert.requireNonNull(serverInterceptor, "serverInterceptor must not be null");
        com.navercorp.pinpoint.common.util.Assert.requireNonNull(bindableService, "bindableService must not be null");

        int bindPort = SocketUtils.findAvailableTcpPort(27675);

        NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(bindPort);
        InternalNettyServerBuilder.setTracingEnabled(serverBuilder, false);
        InternalNettyServerBuilder.setStatsRecordStartedRpcs(serverBuilder, false);
        InternalNettyServerBuilder.setStatsEnabled(serverBuilder, false);

        serverBuilder.addTransportFilter(new SecurityServerTransportFilter());

        TransportMetadataFactory transportMetadataFactory = new TransportMetadataFactory(serverName);
        serverBuilder.addTransportFilter(new MetadataServerTransportFilter(transportMetadataFactory));

        serverBuilder.intercept(new TransportMetadataServerInterceptor());

        serverBuilder.intercept(serverInterceptor);

        serverBuilder.addService(bindableService);

        Server server = serverBuilder.build();
        return server.start();
    }

    @After
    public void stop() {
        if (authorizationServer != null) {
            authorizationServer.shutdownNow();
        }

        if (authenticationServer != null) {
            authenticationServer.shutdown();
        }
    }

    // authentication success -> get token -> authorization success -> authorization communication success
    @Test
    public void getTokenTest() {
        ManagedChannel authenticationChannel = createChannel(authenticationServer.getPort(), new AuthenticationKeyInterceptor(AUTH_KEY));

        ManagedChannel authorizationChannel = null;
        try {
            AuthGrpc.AuthStub authStub = AuthGrpc.newStub(authenticationChannel);

            RecordStreamObserver responseObserver = new RecordStreamObserver();

            // for checking operation when collector receive message during authentication operation
            authStub.getToken(createRequest(), responseObserver);
            authStub.getToken(createRequest(), responseObserver);

            boolean await = awaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    return 2 == responseObserver.getResponseCount();
                }
            });
            Assert.assertTrue(await);

            PCmdGetTokenResponse latestResponse = responseObserver.latestResponse;

            PSecurityResult result = latestResponse.getResult();
            Assert.assertEquals(PAuthCode.OK, result.getCode());

            AuthGrpc.AuthBlockingStub authBlockingStub = AuthGrpc.newBlockingStub(authenticationChannel);

            authorizationChannel = createChannel(authorizationServer.getPort(), new AuthorizationTokenInterceptor(new TestTokenProvider(authBlockingStub)));

            GreeterGrpc.GreeterBlockingStub greeterBlockingStub = GreeterGrpc.newBlockingStub(authorizationChannel);

            String helloMessage = "hello";
            HelloReply helloReply = greeterBlockingStub.sayHello(HelloRequest.newBuilder().setName(helloMessage).build());

            Assert.assertEquals(helloMessage.toUpperCase(), helloReply.getMessage());
        } finally {
            closeChannel(authorizationChannel, authenticationChannel);
        }
    }

    private ManagedChannel createChannel(int bindPort, ClientInterceptor interceptor) {
        final NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress("127.0.0.1", bindPort);
        channelBuilder.usePlaintext();

        channelBuilder.intercept(interceptor);

        InternalNettyChannelBuilder.setStatsEnabled(channelBuilder, false);
        InternalNettyChannelBuilder.setTracingEnabled(channelBuilder, false);
        InternalNettyChannelBuilder.setStatsRecordStartedRpcs(channelBuilder, false);

        return channelBuilder.build();
    }

    private PCmdGetTokenRequest createRequest() {
        PCmdGetTokenRequest.Builder builder = PCmdGetTokenRequest.newBuilder();
        builder.setTokenType(PTokenType.SPAN);
        return builder.build();
    }

    private void closeChannel(ManagedChannel... managedChannels) {
        for (ManagedChannel managedChannel : managedChannels) {
            if (managedChannel != null) {
                managedChannel.shutdownNow();
            }
        }
    }

    // authentication fail -> get token fail -> authorization communication fail
    @Test
    public void getTokenFailTest() {
        ManagedChannel authenticationChannel = createChannel(authenticationServer.getPort(), new AuthenticationKeyInterceptor(AUTH_KEY + "fail"));

        ManagedChannel authorizationChannel = null;
        try {
            AuthGrpc.AuthBlockingStub authBlockingStub = AuthGrpc.newBlockingStub(authenticationChannel);
            try {
                authBlockingStub.getToken(createRequest());
                Assert.fail();
            } catch (StatusRuntimeException e) {
                Assert.assertEquals(Status.Code.UNAUTHENTICATED, e.getStatus().getCode());
            }

            authorizationChannel = createChannel(authorizationServer.getPort(), new AuthorizationTokenInterceptor(new TestTokenProvider(authBlockingStub)));

            GreeterGrpc.GreeterBlockingStub greeterBlockingStub = GreeterGrpc.newBlockingStub(authorizationChannel);

            try {
                String helloMessage = "hello";
                greeterBlockingStub.sayHello(HelloRequest.newBuilder().setName(helloMessage).build());
                Assert.fail();
            } catch (SecurityException e) {
            }

        } finally {
            closeChannel(authorizationChannel, authenticationChannel);
        }
    }

    private static class RecordStreamObserver implements StreamObserver<PCmdGetTokenResponse> {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final AtomicInteger responseCount = new AtomicInteger(0);
        private volatile PCmdGetTokenResponse latestResponse;
        private volatile Throwable latestThrowable;


        private RecordStreamObserver() {
        }

        @Override
        public void onNext(PCmdGetTokenResponse value) {
            logger.info("onNext:{}", value);
            this.latestResponse = value;
            responseCount.incrementAndGet();
        }

        @Override
        public void onError(Throwable t) {
            logger.info("onError:{}", t.getMessage(), t);
            this.latestThrowable = t;
        }

        @Override
        public void onCompleted() {
            logger.info("onCompleted");
        }

        public int getResponseCount() {
            return responseCount.get();
        }

        public PCmdGetTokenResponse getLatestResponse() {
            return latestResponse;
        }

        public Throwable getLatestThrowable() {
            return latestThrowable;
        }

    }

}
