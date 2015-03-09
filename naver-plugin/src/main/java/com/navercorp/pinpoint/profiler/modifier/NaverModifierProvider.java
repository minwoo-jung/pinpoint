package com.navercorp.pinpoint.profiler.modifier;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.profiler.modifier.connector.lucynet.CompositeInvocationFutureModifier;
import com.navercorp.pinpoint.profiler.modifier.connector.lucynet.DefaultInvocationFutureModifier;
import com.navercorp.pinpoint.profiler.modifier.connector.nimm.NimmInvokerModifier;
import com.navercorp.pinpoint.profiler.modifier.connector.npc.KeepAliveNpcHessianConnectorModifier;
import com.navercorp.pinpoint.profiler.modifier.connector.npc.LightWeightConnectorModifier;
import com.navercorp.pinpoint.profiler.modifier.connector.npc.NioNpcHessianConnectorModifier;
import com.navercorp.pinpoint.profiler.modifier.connector.npc.NpcHessianConnectorModifier;
import com.navercorp.pinpoint.profiler.modifier.linegame.HandlerInvokeTaskModifier;
import com.navercorp.pinpoint.profiler.modifier.linegame.HttpCustomServerHandlerModifier;

public class NaverModifierProvider implements ModifierProvider {

    @Override
    public List<Modifier> getModifiers(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        List<Modifier> modifiers = new ArrayList<Modifier>();

        addNpcModifier(modifiers, byteCodeInstrumentor, agent);
        addNimmModifier(modifiers, byteCodeInstrumentor, agent);
        addLucyNetModifier(modifiers, byteCodeInstrumentor, agent);
        addLineGameBaseFrameworkModifier(modifiers, byteCodeInstrumentor, agent);

        return modifiers;
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
}