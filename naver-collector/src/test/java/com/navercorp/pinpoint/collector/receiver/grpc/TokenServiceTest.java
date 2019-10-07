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
import com.navercorp.pinpoint.collector.receiver.grpc.security.service.AuthTokenService;
import com.navercorp.pinpoint.grpc.auth.AuthGrpc;
import com.navercorp.pinpoint.grpc.auth.PAuthCode;
import com.navercorp.pinpoint.grpc.auth.PAuthorizationToken;
import com.navercorp.pinpoint.grpc.auth.PCmdGetTokenRequest;
import com.navercorp.pinpoint.grpc.auth.PCmdGetTokenResponse;
import com.navercorp.pinpoint.grpc.auth.PSecurityResult;
import com.navercorp.pinpoint.grpc.auth.PTokenType;
import com.navercorp.pinpoint.grpc.security.TokenType;
import com.navercorp.pinpoint.grpc.security.client.AuthenticationKeyInterceptor;
import com.navercorp.pinpoint.grpc.security.server.SecurityServerTransportFilter;
import com.navercorp.pinpoint.grpc.server.MetadataServerTransportFilter;
import com.navercorp.pinpoint.grpc.server.TransportMetadataFactory;
import com.navercorp.pinpoint.grpc.server.TransportMetadataServerInterceptor;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;

import io.grpc.ManagedChannel;
import io.grpc.Server;
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
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Taejin Koo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-tokenauth-test.xml")
@ActiveProfiles("tokenAuthentication")
public class TokenServiceTest {

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(100, 1000);


    private Server server;

    private int bindPort;

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Autowired
    private AuthTokenService authTokenService;

    @Before
    public void start() throws IOException {
        bindPort = SocketUtils.findAvailableTcpPort(27675);

        NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(bindPort);
        InternalNettyServerBuilder.setTracingEnabled(serverBuilder, false);
        InternalNettyServerBuilder.setStatsRecordStartedRpcs(serverBuilder, false);
        InternalNettyServerBuilder.setStatsEnabled(serverBuilder, false);

        serverBuilder.addTransportFilter(new SecurityServerTransportFilter());

        TransportMetadataFactory transportMetadataFactory = new TransportMetadataFactory("test");
        serverBuilder.addTransportFilter(new MetadataServerTransportFilter(transportMetadataFactory));

        serverBuilder.intercept(new TransportMetadataServerInterceptor());
        serverBuilder.intercept(authenticationInterceptor);

        serverBuilder.addService(authTokenService);

        server = serverBuilder.build();

        server.start();
    }

    @After
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    // authentication success -> get token -> authrization success
    @Test
    public void getTokenTest() {
        final NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress("127.0.0.1", bindPort);
        channelBuilder.usePlaintext();

        channelBuilder.intercept(new AuthenticationKeyInterceptor("test"));

        InternalNettyChannelBuilder.setStatsEnabled(channelBuilder, false);
        InternalNettyChannelBuilder.setTracingEnabled(channelBuilder, false);
        InternalNettyChannelBuilder.setStatsRecordStartedRpcs(channelBuilder, false);

        ManagedChannel managedChannel = channelBuilder.build();

        try {
            AuthGrpc.AuthStub authStub = AuthGrpc.newStub(managedChannel);

            PCmdGetTokenRequest.Builder builder = PCmdGetTokenRequest.newBuilder();
            builder.setTokenType(PTokenType.SPAN);

            CountDownLatch latch = new CountDownLatch(2);

            RecordStreamObserver responseObserver = new RecordStreamObserver();

            // for checking operation when collector receive message during authentication operation
            authStub.getToken(builder.build(), responseObserver);
            authStub.getToken(builder.build(), responseObserver);

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

            PAuthorizationToken authorizationToken = latestResponse.getAuthorizationToken();
            String token = authorizationToken.getToken();
            Assert.assertTrue(StringUtils.hasLength(token));

            boolean authorization = authTokenService.authorization(token, TokenType.SPAN);
            Assert.assertTrue(authorization);

            // for checking one-time token
            authorization = authTokenService.authorization(token, TokenType.SPAN);
            Assert.assertFalse(authorization);

            authorization = authTokenService.authorization(token + " fail", TokenType.SPAN);
            Assert.assertFalse(authorization);
        } finally {
            managedChannel.shutdownNow();
        }

    }

    private static class RecordStreamObserver implements StreamObserver<PCmdGetTokenResponse> {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final AtomicInteger responseCount = new AtomicInteger(0);
        private volatile PCmdGetTokenResponse latestResponse;

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

    }

}
