package com.navercorp.pinpoint.profiler.modifier;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.profiler.modifier.bloc.handler.HTTPHandlerModifier;
import com.navercorp.pinpoint.profiler.modifier.bloc4.NettyInboundHandlerModifier;
import com.navercorp.pinpoint.profiler.modifier.bloc4.NpcHandlerModifier;
import com.navercorp.pinpoint.profiler.modifier.bloc4.RequestProcessorModifier;
import com.navercorp.pinpoint.profiler.modifier.connector.lucynet.CompositeInvocationFutureModifier;
import com.navercorp.pinpoint.profiler.modifier.connector.lucynet.DefaultInvocationFutureModifier;
import com.navercorp.pinpoint.profiler.modifier.connector.nimm.NimmInvokerModifier;
import com.navercorp.pinpoint.profiler.modifier.connector.npc.KeepAliveNpcHessianConnectorModifier;
import com.navercorp.pinpoint.profiler.modifier.connector.npc.LightWeightConnectorModifier;
import com.navercorp.pinpoint.profiler.modifier.connector.npc.NioNpcHessianConnectorModifier;
import com.navercorp.pinpoint.profiler.modifier.connector.npc.NpcHessianConnectorModifier;
import com.navercorp.pinpoint.profiler.modifier.linegame.HandlerInvokeTaskModifier;
import com.navercorp.pinpoint.profiler.modifier.linegame.HttpCustomServerHandlerModifier;
import com.navercorp.pinpoint.profiler.modifier.redis.BinaryJedisModifier;
import com.navercorp.pinpoint.profiler.modifier.redis.BinaryRedisClusterModifier;
import com.navercorp.pinpoint.profiler.modifier.redis.BinaryTriplesRedisClusterModifier;
import com.navercorp.pinpoint.profiler.modifier.redis.GatewayModifier;
import com.navercorp.pinpoint.profiler.modifier.redis.GatewayServerModifier;
import com.navercorp.pinpoint.profiler.modifier.redis.JedisClientModifier;
import com.navercorp.pinpoint.profiler.modifier.redis.JedisModifier;
import com.navercorp.pinpoint.profiler.modifier.redis.JedisMultiKeyPipelineBaseModifier;
import com.navercorp.pinpoint.profiler.modifier.redis.JedisPipelineBaseModifier;
import com.navercorp.pinpoint.profiler.modifier.redis.JedisPipelineModifier;
import com.navercorp.pinpoint.profiler.modifier.redis.RedisClusterModifier;
import com.navercorp.pinpoint.profiler.modifier.redis.RedisClusterPipelineModifier;
import com.navercorp.pinpoint.profiler.modifier.redis.TriplesRedisClusterModifier;

public class NaverModifierProvider implements ModifierProvider {

    @Override
    public List<Modifier> getModifiers(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        List<Modifier> modifiers = new ArrayList<Modifier>();
        
        addBLOC3Modifier(modifiers, byteCodeInstrumentor, agent);
        addBLOC4Modifier(modifiers, byteCodeInstrumentor, agent);
        addNpcModifier(modifiers, byteCodeInstrumentor, agent);
        addNimmModifier(modifiers, byteCodeInstrumentor, agent);
        addLucyNetModifier(modifiers, byteCodeInstrumentor, agent);
        addLineGameBaseFrameworkModifier(modifiers, byteCodeInstrumentor, agent);
        addNbaseArcSupport(modifiers, byteCodeInstrumentor, agent);
        addRedisSupport(modifiers, byteCodeInstrumentor, agent);
        
        return modifiers;
    }
    /**
     * BLOC 3.x
     */
    public void addBLOC3Modifier(List<Modifier> modifiers, ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        HTTPHandlerModifier httpHandlerModifier = new HTTPHandlerModifier(byteCodeInstrumentor, agent);
        modifiers.add(httpHandlerModifier);
    }

    /**
     * BLOC 4.x
     */
    private void addBLOC4Modifier(List<Modifier> modifiers, ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        NettyInboundHandlerModifier nettyInboundHandlerModifier = new NettyInboundHandlerModifier(byteCodeInstrumentor, agent);
        modifiers.add(nettyInboundHandlerModifier);
        
        NpcHandlerModifier npcHandlerModifier = new NpcHandlerModifier(byteCodeInstrumentor, agent);
        modifiers.add(npcHandlerModifier);
        
        RequestProcessorModifier requestProcessorModifier = new RequestProcessorModifier(byteCodeInstrumentor, agent);
        modifiers.add(requestProcessorModifier);
    }

    private void addNpcModifier(List<Modifier> modifiers, ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        modifiers.add(new KeepAliveNpcHessianConnectorModifier(byteCodeInstrumentor, agent));
        // modifiers.add(new LightWeightNbfpConnectorModifier(byteCodeInstrumentor, agent));
        // modifiers.add(new LightWeightNpcHessianConnectorModifier(byteCodeInstrumentor, agent));
        modifiers.add(new LightWeightConnectorModifier(byteCodeInstrumentor, agent));
        modifiers.add(new NioNpcHessianConnectorModifier(byteCodeInstrumentor, agent));
        modifiers.add(new NpcHessianConnectorModifier(byteCodeInstrumentor, agent));
    }
    
    private void addNimmModifier(List<Modifier> modifiers, ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        modifiers.add(new NimmInvokerModifier(byteCodeInstrumentor, agent));
    }
    
    private void addLucyNetModifier(List<Modifier> modifiers, ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        modifiers.add(new DefaultInvocationFutureModifier(byteCodeInstrumentor, agent));
        modifiers.add(new CompositeInvocationFutureModifier(byteCodeInstrumentor, agent));
    }
    
    /**
     * line game에서 사용하는 baseframework의 http handler를 지원.
     */
    private void addLineGameBaseFrameworkModifier(List<Modifier> modifiers, ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        modifiers.add(new HandlerInvokeTaskModifier(byteCodeInstrumentor, agent));
        modifiers.add(new HttpCustomServerHandlerModifier(byteCodeInstrumentor, agent));
    }

    private void addRedisSupport(List<Modifier> modifiers, ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        ProfilerConfig profilerConfig = agent.getProfilerConfig();
        
        if (profilerConfig.isRedisEnabled()) {
            modifiers.add(new BinaryJedisModifier(byteCodeInstrumentor, agent));
            modifiers.add(new JedisModifier(byteCodeInstrumentor, agent));
        }
        
        if (profilerConfig.isRedisPipelineEnabled()) {
            modifiers.add(new JedisClientModifier(byteCodeInstrumentor, agent));
            modifiers.add(new JedisPipelineBaseModifier(byteCodeInstrumentor, agent));
            modifiers.add(new JedisMultiKeyPipelineBaseModifier(byteCodeInstrumentor, agent));
            modifiers.add(new JedisPipelineModifier(byteCodeInstrumentor, agent));
        }
    }
    
    private void addNbaseArcSupport(List<Modifier> modifiers, ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        ProfilerConfig profilerConfig = agent.getProfilerConfig();
        
        if (profilerConfig.isNbaseArcEnabled() || profilerConfig.isNbaseArcPipelineEnabled()) {
            modifiers.add(new GatewayModifier(byteCodeInstrumentor, agent));
            modifiers.add(new GatewayServerModifier(byteCodeInstrumentor, agent));
            
            if (profilerConfig.isNbaseArcEnabled()) {
                modifiers.add(new RedisClusterModifier(byteCodeInstrumentor, agent));
                modifiers.add(new BinaryRedisClusterModifier(byteCodeInstrumentor, agent));
                modifiers.add(new TriplesRedisClusterModifier(byteCodeInstrumentor, agent));
                modifiers.add(new BinaryTriplesRedisClusterModifier(byteCodeInstrumentor, agent));
            }

            if (profilerConfig.isNbaseArcPipelineEnabled()) {
                modifiers.add(new RedisClusterPipelineModifier(byteCodeInstrumentor, agent));
            }
        }
    }
}
