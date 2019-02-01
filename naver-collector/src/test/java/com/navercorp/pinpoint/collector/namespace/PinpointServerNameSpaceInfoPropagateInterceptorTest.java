/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.namespace;

import com.navercorp.pinpoint.collector.service.MetadataService;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationKey;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.security.SecurityConstants;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
public class PinpointServerNameSpaceInfoPropagateInterceptorTest {

    private final TestMetadataService metadataService = new TestMetadataService();

    private final PinpointServerNameSpaceInfoPropagateInterceptor interceptor = new PinpointServerNameSpaceInfoPropagateInterceptor(metadataService, false);

    @Test
    public void nameSpaceInfoShouldBePropagated() throws Throwable {
        final String licenseKey = "TEST";
        final NameSpaceInfo nameSpaceInfo = new NameSpaceInfo("test", "pinpoint", "default");
        metadataService.register(licenseKey, nameSpaceInfo);
        PinpointServer pinpointServer = mockPinpointServerWithLicenseKey(licenseKey);
        ProceedingJoinPoint proceedingJoinPoint = mockJoinPointForNameSpaceInfo(nameSpaceInfo);

        interceptor.aroundAdvice(proceedingJoinPoint, pinpointServer.getChannelProperties());
    }

    @Test
    public void requestAttributesShouldBeCleanedUpFromThreads() throws Throwable {
        final int numThreads = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        final int numOrganizations = 10;
        final int numTestsPerOrganization = 30;
        List<Runnable> testRunnables = new ArrayList<>(numOrganizations * numTestsPerOrganization);
        for (int i = 0; i < numOrganizations; i++) {
            final String licenseKey = "key_" + i;
            final NameSpaceInfo nameSpaceInfo = new NameSpaceInfo("test_" + i, "pinpoint_" + i, "ns_" + i);
            metadataService.register(licenseKey, nameSpaceInfo);
            for (int j = 0; j < numTestsPerOrganization; j++) {
                PinpointServer pinpointServer = mockPinpointServerWithLicenseKey(licenseKey);
                ProceedingJoinPoint proceedingJoinPoint = mockJoinPointForNameSpaceInfo(nameSpaceInfo);
                testRunnables.add(new TestRunnable(pinpointServer, proceedingJoinPoint));
            }
        }
        Collections.shuffle(testRunnables);
        List<CompletableFuture<Void>> completableFutures = new ArrayList<>(testRunnables.size());
        for (Runnable testRunnable : testRunnables) {
            completableFutures.add(CompletableFuture.runAsync(testRunnable, executorService));
        }
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
        resultFuture.get(5, TimeUnit.SECONDS);
    }

    private class TestRunnable implements Runnable {
        private final PinpointServer pinpointServer;
        private final ProceedingJoinPoint proceedingJoinPoint;

        private TestRunnable(PinpointServer pinpointServer, ProceedingJoinPoint proceedingJoinPoint) {
            this.pinpointServer = pinpointServer;
            this.proceedingJoinPoint = proceedingJoinPoint;
        }

        @Override
        public void run() {
            Assert.assertNull(RequestContextHolder.getAttributes());
            try {
                interceptor.aroundAdvice(proceedingJoinPoint, pinpointServer.getChannelProperties());
            } catch (AssertionError e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException("Error running interceptor", t);
            } finally {
                Assert.assertNull(RequestContextHolder.getAttributes());
            }
        }
    }

    private static class TestMetadataService implements MetadataService {

        private final Map<String, PaaSOrganizationKey> keyMap = new HashMap<>();
        private final Map<String, PaaSOrganizationInfo> infoMap = new HashMap<>();

        private void register(String licenseKey, NameSpaceInfo nameSpaceInfo) {
            String organization = nameSpaceInfo.getOrganization();
            PaaSOrganizationKey key = new PaaSOrganizationKey(licenseKey, organization);
            if (keyMap.putIfAbsent(licenseKey, key) != null) {
                throw new IllegalStateException("[" + licenseKey + "] licenseKey already registered");
            }
            PaaSOrganizationInfo info = new PaaSOrganizationInfo();
            info.setOrganization(organization);
            info.setDatabaseName(nameSpaceInfo.getMysqlDatabaseName());
            info.setHbaseNameSpace(nameSpaceInfo.getHbaseNamespace());
            if (infoMap.putIfAbsent(organization, info) != null) {
                throw new IllegalStateException("[" + organization + "] organization already registered");
            }
        }

        @Override
        public PaaSOrganizationKey selectPaaSOrganizationkey(String licenseKey) {
            return keyMap.get(licenseKey);
        }

        @Override
        public PaaSOrganizationInfo selectPaaSOrganizationInfo(String organizationName) {
            return infoMap.get(organizationName);
        }
    }

    private static ProceedingJoinPoint mockJoinPointForNameSpaceInfo(NameSpaceInfo expectedNameSpaceInfo) throws Throwable {
        ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);
        doAnswer((invocationOnMock) -> {
            RequestAttributes requestAttributes = RequestContextHolder.currentAttributes();
            NameSpaceInfo nameSpaceInfo = (NameSpaceInfo) requestAttributes.getAttribute(NameSpaceInfo.NAMESPACE_INFO);
            verifyNameSpaceInfo(expectedNameSpaceInfo, nameSpaceInfo);
            return null;
        }).when(proceedingJoinPoint).proceed();
        return proceedingJoinPoint;
    }

    private static PinpointServer mockPinpointServerWithLicenseKey(String licenseKey) {
        Map<Object, Object> channelProperties = Collections.singletonMap(SecurityConstants.KEY_LICENSE_KEY, licenseKey);
        PinpointServer pinpointServer = mock(PinpointServer.class);
        when(pinpointServer.getChannelProperties()).thenReturn(channelProperties);
        return pinpointServer;
    }

    private static void verifyNameSpaceInfo(NameSpaceInfo expected, NameSpaceInfo actual) {
        Assert.assertEquals(expected.getOrganization(), actual.getOrganization());
        Assert.assertEquals(expected.getMysqlDatabaseName(), actual.getMysqlDatabaseName());
        Assert.assertEquals(expected.getHbaseNamespace(), actual.getHbaseNamespace());
    }
}
