package com.navercorp.pinpoint.profiler.modifier.nbase.arc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;

import com.navercorp.pinpoint.common.bo.SpanEventBo;
import com.navercorp.pinpoint.test.junit4.BasePinpointTest;
import com.nhncorp.redis.cluster.RedisCluster;
import com.nhncorp.redis.cluster.RedisClusterClient;

public class RedisClusterModifierTest extends BasePinpointTest {
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
            assertEquals(HOST + ":" + PORT, eventBo.getEndPoint());
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