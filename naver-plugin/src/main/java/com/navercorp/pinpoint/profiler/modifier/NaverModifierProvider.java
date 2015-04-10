package com.navercorp.pinpoint.profiler.modifier;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.profiler.modifier.linegame.HandlerInvokeTaskModifier;
import com.navercorp.pinpoint.profiler.modifier.linegame.HttpCustomServerHandlerModifier;

public class NaverModifierProvider implements ModifierProvider {

    @Override
    public List<Modifier> getModifiers(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        List<Modifier> modifiers = new ArrayList<Modifier>();

        addLineGameBaseFrameworkModifier(modifiers, byteCodeInstrumentor, agent);

        return modifiers;
    }

    /**
     * line game에서 사용하는 baseframework의 http handler를 지원.
     */
    private void addLineGameBaseFrameworkModifier(List<Modifier> modifiers, ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        modifiers.add(new HandlerInvokeTaskModifier(byteCodeInstrumentor, agent));
        modifiers.add(new HttpCustomServerHandlerModifier(byteCodeInstrumentor, agent));
    }
}