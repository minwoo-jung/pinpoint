package com.navercorp.pinpoint.plugin.lucy.net.npc;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetPlugin;

import java.security.ProtectionDomain;

/**
 * @author Taejin Koo
 */
class LegacyLightWeightPlugin extends NpcPlugin {

    public LegacyLightWeightPlugin(ProfilerPluginSetupContext context, TransformTemplate transformTemplate) {
        super(context, transformTemplate);
    }

    @Override
    public String getEditClazzName() {
        return "com.nhncorp.lucy.npc.connector.LightweightConnector";
    }

    @Override
    public void transform() {
        transformTemplate.transform(getEditClazzName(), new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(LucyNetConstants.METADATA_NPC_SERVER_ADDRESS);

                InstrumentMethod constructor = target.getConstructor("com.nhncorp.lucy.npc.connector.NpcConnectorOption");
                LucyNetPlugin.addInterceptor(constructor, LucyNetConstants.NPC_CONSTRUCTOR_INTERCEPTOR);

                InstrumentMethod method = target.getDeclaredMethod("invoke", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]");
                LucyNetPlugin.addInterceptor(method, LucyNetConstants.NPC_INVOKE_INTERCEPTOR);

                return target.toBytecode();
            }

        });

    }


}
