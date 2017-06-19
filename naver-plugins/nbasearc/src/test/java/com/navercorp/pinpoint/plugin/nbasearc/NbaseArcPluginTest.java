/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.nbasearc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import org.junit.Test;

import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.test.junit4.BasePinpointTest;
import com.nhncorp.redis.cluster.RedisCluster;
import com.nhncorp.redis.cluster.RedisClusterClient;

/**
 * 
 * @author jaehong.kim
 *
 */
public class NbaseArcPluginTest extends BasePinpointTest {

    private static final String HOST = "localhost";
    private static final int PORT = 6379;

    @Test
    public void command() {
        RedisClusterMock redis = new RedisClusterMock(HOST, PORT);
        try {
            redis.get("foo");
            redis.get("foo".getBytes());

            final List<SpanEventBo> events = getCurrentSpanEvents();
            assertEquals(2, events.size());

            final SpanEventBo eventBo = events.get(0);
            assertEquals(HostAndPort.toHostAndPortString(HOST, PORT), eventBo.getEndPoint());
            assertEquals("NBASE_ARC", eventBo.getDestinationId());
        } finally {
            if (redis != null) {
                redis.disconnect();
            }
        }
    }

    public class RedisClusterMock extends RedisCluster {

        public RedisClusterMock(String host, int port) {
            super(host, port);

            client = mock(RedisClusterClient.class);
            when(client.getIntegerReply()).thenReturn(1L);
            when(client.getBulkReply()).thenReturn("bar");
            when(client.getStatusCodeReply()).thenReturn("Ok");
        }
    }
}
