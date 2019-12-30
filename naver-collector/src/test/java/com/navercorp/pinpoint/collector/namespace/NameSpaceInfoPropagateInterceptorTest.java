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

import com.navercorp.pinpoint.collector.service.NamespaceService;
import com.navercorp.pinpoint.collector.service.async.AgentProperty;
import com.navercorp.pinpoint.collector.service.async.AgentPropertyChannelAdaptor;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationKey;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationLifeCycle;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.ChannelProperties;
import com.navercorp.pinpoint.rpc.server.ChannelPropertiesFactory;
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

/**
 * @author HyunGil Jeong
 */
public class NameSpaceInfoPropagateInterceptorTest {

    private final TestMetadataService metadataService = new TestMetadataService();

    private final ChannelPropertiesFactory channelPropertiesFactory = new ChannelPropertiesFactory(SecurityConstants.KEY_LICENSE_KEY);

    private final NameSpaceInfoPropagateInterceptor interceptor = new NameSpaceInfoPropagateInterceptor(metadataService, false);

    @Test
    public void nameSpaceInfoShouldBePropagated() throws Throwable {
        final String licenseKey = "TEST";
        final NameSpaceInfo nameSpaceInfo = new NameSpaceInfo("test", "pinpoint", "default");
        metadataService.register(licenseKey, nameSpaceInfo);
        Map<Object, Object> properties =  mockPinpointServerWithLicenseKey(licenseKey);
        ProceedingJoinPoint proceedingJoinPoint = mockJoinPointForNameSpaceInfo(nameSpaceInfo);
        ChannelProperties channelProperties = channelPropertiesFactory.newChannelProperties(properties);
        AgentProperty agentProperty = new AgentPropertyChannelAdaptor(channelProperties);
        interceptor.aroundAdvice(proceedingJoinPoint, agentProperty);
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
                Map<Object, Object> properties =  mockPinpointServerWithLicenseKey(licenseKey);
                ProceedingJoinPoint proceedingJoinPoint = mockJoinPointForNameSpaceInfo(nameSpaceInfo);
                testRunnables.add(new TestRunnable(properties, proceedingJoinPoint));
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
        private final Map<Object, Object> properties;
        private final ProceedingJoinPoint proceedingJoinPoint;

        private TestRunnable(Map<Object, Object> properties, ProceedingJoinPoint proceedingJoinPoint) {
            this.properties = properties;
            this.proceedingJoinPoint = proceedingJoinPoint;
        }

        @Override
        public void run() {
            Assert.assertNull(RequestContextHolder.getAttributes());
            try {
                ChannelProperties channelProperties = channelPropertiesFactory.newChannelProperties(properties);
                AgentProperty agentProperty = new AgentPropertyChannelAdaptor(channelProperties);
                interceptor.aroundAdvice(proceedingJoinPoint, agentProperty);
            } catch (AssertionError e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException("Error running interceptor", t);
            } finally {
                Assert.assertNull(RequestContextHolder.getAttributes());
            }
        }
    }

    private static class TestMetadataService implements NamespaceService {

        private final Map<String, PaaSOrganizationKey> keyMap = new HashMap<>();
        private final Map<String, PaaSOrganizationInfo> infoMap = new HashMap<>();

        private void register(String licenseKey, NameSpaceInfo nameSpaceInfo) {
            String organization = nameSpaceInfo.getOrganization();
            PaaSOrganizationKey key = new PaaSOrganizationKey(licenseKey, organization);
            if (keyMap.putIfAbsent(licenseKey, key) != null) {
                throw new IllegalStateException("[" + licenseKey + "] licenseKey already registered");
            }
            PaaSOrganizationInfo info = new PaaSOrganizationInfo(organization, nameSpaceInfo.getMysqlDatabaseName(), nameSpaceInfo.getHbaseNamespace());
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

        @Override
        public List<PaaSOrganizationLifeCycle> selectPaaSOrganizationLifeCycle() {
            return null;
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

    private static Map<Object, Object> mockPinpointServerWithLicenseKey(String licenseKey) {
        Map<Object, Object> channelProperties = new HashMap<>();
        channelProperties.put(HandshakePropertyType.AGENT_ID.getName(), "agentId");
        channelProperties.put(HandshakePropertyType.APPLICATION_NAME.getName(), "appName");
        channelProperties.put(SecurityConstants.KEY_LICENSE_KEY, licenseKey);

        return channelProperties;
    }

    private static void verifyNameSpaceInfo(NameSpaceInfo expected, NameSpaceInfo actual) {
        Assert.assertEquals(expected.getOrganization(), actual.getOrganization());
        Assert.assertEquals(expected.getMysqlDatabaseName(), actual.getMysqlDatabaseName());
        Assert.assertEquals(expected.getHbaseNamespace(), actual.getHbaseNamespace());
    }
}
