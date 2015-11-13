package com.navercorp.pinpoint.plugin.lucy.net.npc;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import java.security.ProtectionDomain;

/**
 * @author Taejin Koo
 */
class KeepAlivePlugin extends NpcPlugin {

    public KeepAlivePlugin(ProfilerPluginSetupContext context, TransformTemplate transformTemplate) {
        super(context, transformTemplate);
    }

    @Override
    public String getEditClazzName() {
        return "com.nhncorp.lucy.npc.connector.KeepAliveNpcHessianConnector";
    }

    @Override
    public void transform() {
        transformTemplate.transform(getEditClazzName(), new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);

                for (KeepAliveVersion matchedVersion : KeepAliveVersion.values()) {
                    if (matchedVersion.checkCondition(target)) {
                        return matchedVersion.transform(target);
                    }
                }

                return target.toBytecode();
            }

        });
    }

}