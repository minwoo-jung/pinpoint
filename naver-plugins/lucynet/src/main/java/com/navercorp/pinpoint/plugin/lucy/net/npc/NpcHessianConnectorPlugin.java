package com.navercorp.pinpoint.plugin.lucy.net.npc;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.PinpointClassFileTransformer;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;

import java.security.ProtectionDomain;

/**
 * @author Taejin Koo
 */
class NpcHessianConnectorPlugin extends NpcPlugin implements LucyNetConstants {
    
    public NpcHessianConnectorPlugin(ProfilerPluginSetupContext context) {
        super(context);
    }

    @Override
    public String getEditClazzName() {
        return "com.nhncorp.lucy.npc.connector.NpcHessianConnector";
    }

    @Override
    public void addRecipe() {
        context.addClassFileTransformer(getEditClazzName(), new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);

                for (NpcHessianConnectorVersion matchedVersion : NpcHessianConnectorVersion.values()) {
                    if (matchedVersion.checkCondition(target)) {
                        return matchedVersion.transform(target);
                    }
                }

                return target.toBytecode();
            }

        });
    }

}