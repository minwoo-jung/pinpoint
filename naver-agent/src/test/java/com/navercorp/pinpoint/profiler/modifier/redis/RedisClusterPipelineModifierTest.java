package com.navercorp.pinpoint.profiler.modifier.redis;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.bo.SpanEventBo;
import com.navercorp.pinpoint.test.junit4.BasePinpointTest;
import com.nhncorp.redis.cluster.RedisClusterPoolConfig;
import com.nhncorp.redis.cluster.gateway.GatewayClient;
import com.nhncorp.redis.cluster.gateway.GatewayConfig;
import com.nhncorp.redis.cluster.pipeline.RedisClusterPipeline;

public class RedisClusterPipelineModifierTest extends BasePinpointTest {
    private static final String ZK_ADDRESS = "dev.xnbasearc.navercorp.com:2181";
    private static final String CLUSTER_NAME = "java_client_test";

    private GatewayClient client;
    private RedisClusterPipeline pipeline;

    @Before
    public void before() {
        GatewayConfig config = new GatewayConfig();
        RedisClusterPoolConfig poolConfig = new RedisClusterPoolConfig();
        poolConfig.setMaxTotal(1);
        config.setPoolConfig(poolConfig);
        config.setZkAddress(ZK_ADDRESS);
        config.setClusterName(CLUSTER_NAME);

        GatewayClient client = new GatewayClient(config);
        pipeline = client.pipeline();
    }

    @After
    public void after() {
        if (client != null) {
            client.destroy();
        }
    }

    @Test
    public void traceMethod() {
        pipeline.get("foo");
        pipeline.syncAndReturnAll();

        final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
        SpanEventBo event = spanEvents.get(0);

        assertEquals("NBASE_ARC", event.getDestinationId());
        assertEquals(ServiceType.NBASE_ARC, event.getServiceType());
        assertNull(event.getExceptionMessage());
    }

    @Test
    public void traceBinaryMethod() {
        pipeline.get("foo".getBytes());
        pipeline.syncAndReturnAll();

        final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
        SpanEventBo event = spanEvents.get(0);

        assertEquals("NBASE_ARC", event.getDestinationId());
        assertEquals(ServiceType.NBASE_ARC, event.getServiceType());
        assertNull(event.getExceptionMessage());
    }

    @Test
    public void traceDestinationId() {
        GatewayConfig config = new GatewayConfig();
        config.setZkAddress(ZK_ADDRESS);
        config.setClusterName(CLUSTER_NAME);

        GatewayClient client = new GatewayClient(config);
        RedisClusterPipeline pipeline = client.pipeline();

        pipeline.get("foo");
        pipeline.syncAndReturnAll();

        final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
        SpanEventBo event = spanEvents.get(spanEvents.size() - 1);

        assertEquals(CLUSTER_NAME, event.getDestinationId());
        assertEquals(ServiceType.NBASE_ARC, event.getServiceType());
        assertNull(event.getExceptionMessage());

        client.destroy();
    }
}