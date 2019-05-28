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

package com.navercorp.pinpoint.collector.handler.grpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.navercorp.pinpoint.collector.dao.MetadataDao;
import com.navercorp.pinpoint.collector.dao.memory.MemoryMetadataDao;
import com.navercorp.pinpoint.collector.receiver.grpc.service.AuthTokenService;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationKey;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.grpc.auth.PCmdGetTokenRequest;
import com.navercorp.pinpoint.grpc.auth.PCmdGetTokenResponse;
import com.navercorp.pinpoint.grpc.auth.PTokenResponseCode;
import com.navercorp.pinpoint.grpc.auth.PTokenType;
import com.navercorp.pinpoint.grpc.server.DefaultTransportMetadata;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
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

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * @author Taejin Koo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-tokenauth-test.xml")
@ActiveProfiles("tokenAuthentication")
public class AuthTokenServiceTest {

    private static final String LICENSE_KEY = UUID.randomUUID().toString();
    private static final String ORGANIZATION = "org";
    private static final String NAMESPACE = "namespace";
    private static final String REMOTE_ADDRESS = "127.0.0.1";

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private MetadataDao metadataDao;

    @Before
    public void setUp() throws Exception {
        if (metadataDao instanceof MemoryMetadataDao) {
            PaaSOrganizationKey paaSOrganizationKey = new PaaSOrganizationKey(LICENSE_KEY, ORGANIZATION);
            ((MemoryMetadataDao) metadataDao).createPaaSOrganizationkey(LICENSE_KEY, paaSOrganizationKey);

            PaaSOrganizationInfo paaSOrganizationInfo = new PaaSOrganizationInfo(ORGANIZATION, NAMESPACE, NAMESPACE);
            ((MemoryMetadataDao) metadataDao).createPaaSOrganizationInfo(ORGANIZATION, paaSOrganizationInfo);
        }
    }

    @Test
    public void responseSuccessTest() {
        TransportMetadata transportMetaData = createTransportMetaData(new InetSocketAddress(REMOTE_ADDRESS, 41413), 10);
        attachContext(transportMetaData);

        PCmdGetTokenRequest.Builder builder = PCmdGetTokenRequest.newBuilder();

        builder.setLicenseKey(BytesValue.of(ByteString.copyFromUtf8(LICENSE_KEY)));
        builder.setTokenType(PTokenType.SPAN);

        RecordedStreamObserver<PCmdGetTokenResponse> recordedStreamObserver = new RecordedStreamObserver<>();
        authTokenService.getToken(builder.build(), recordedStreamObserver);

        PCmdGetTokenResponse latestResponse = recordedStreamObserver.getLatestResponse();
        Assert.assertEquals(PTokenResponseCode.OK, latestResponse.getCode());
        Assert.assertNotNull(latestResponse.getToken().getValue().toStringUtf8());
    }


    @Test
    public void responseFailTest1() {
        TransportMetadata transportMetaData = createTransportMetaData(new InetSocketAddress(REMOTE_ADDRESS, 41413), 10);
        attachContext(transportMetaData);

        PCmdGetTokenRequest.Builder builder = PCmdGetTokenRequest.newBuilder();

        RecordedStreamObserver<PCmdGetTokenResponse> recordedStreamObserver = new RecordedStreamObserver<>();
        authTokenService.getToken(builder.build(), recordedStreamObserver);

        PCmdGetTokenResponse latestResponse = recordedStreamObserver.getLatestResponse();
        Assert.assertEquals(PTokenResponseCode.BAD_REQUEST, latestResponse.getCode());
        Assert.assertTrue(StringUtils.isEmpty(latestResponse.getToken().getValue().toStringUtf8()));
    }

    @Test
    public void responseFailTest2() {
        TransportMetadata transportMetaData = createTransportMetaData(new InetSocketAddress(REMOTE_ADDRESS, 41413), 10);
        attachContext(transportMetaData);

        PCmdGetTokenRequest.Builder builder = PCmdGetTokenRequest.newBuilder();
        builder.setLicenseKey(BytesValue.of(ByteString.copyFromUtf8(LICENSE_KEY + "fail")));
        builder.setTokenType(PTokenType.SPAN);

        RecordedStreamObserver<PCmdGetTokenResponse> recordedStreamObserver = new RecordedStreamObserver<>();
        authTokenService.getToken(builder.build(), recordedStreamObserver);

        PCmdGetTokenResponse latestResponse = recordedStreamObserver.getLatestResponse();
        Assert.assertEquals(PTokenResponseCode.UNAUTHORIZED, latestResponse.getCode());
        Assert.assertTrue(StringUtils.isEmpty(latestResponse.getToken().getValue().toStringUtf8()));
    }

    private TransportMetadata createTransportMetaData(InetSocketAddress remoteAddress, long transportId) {
        return new DefaultTransportMetadata(remoteAddress, transportId, System.currentTimeMillis());
    }

    private void attachContext(TransportMetadata transportMetadata) {
        final Context currentContext = Context.current();
        Context newContext = currentContext.withValue(ServerContext.getTransportMetadataKey(), transportMetadata);
        newContext.attach();
    }


    private static class RecordedStreamObserver<T> implements StreamObserver<T> {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private int requestCount;
        private T latestResponse;
        private Throwable latestThrowable;
        private boolean isCompleted = false;

        @Override
        public void onNext(T response) {
            requestCount++;
            logger.info("onNext:{}", response);
            this.latestResponse = response;
        }

        @Override
        public void onError(Throwable t) {
            logger.info("onError. message:{}", t.getMessage(), t);
            this.latestThrowable = t;
        }

        @Override
        public void onCompleted() {
            logger.info("onCompleted");
            this.isCompleted = true;
        }

        public T getLatestResponse() {
            return latestResponse;
        }

        public int getRequestCount() {
            return requestCount;
        }

        public Throwable getLatestThrowable() {
            return latestThrowable;
        }

        public boolean isCompleted() {
            return isCompleted;
        }
    }

}
