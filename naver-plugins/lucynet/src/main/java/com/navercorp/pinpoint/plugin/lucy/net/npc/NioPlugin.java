package com.navercorp.pinpoint.plugin.lucy.net.npc;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginInstrumentContext;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.PinpointClassFileTransformer;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetPlugin;

import java.security.ProtectionDomain;

/**
 * @author Taejin Koo
 */
class NioPlugin extends NpcPlugin implements LucyNetConstants {

    public NioPlugin(ProfilerPluginSetupContext context) {
        super(context);
    }

    @Override
    public String getEditClazzName() {
        return "com.nhncorp.lucy.npc.connector.NioNpcHessianConnector";
    }

    @Override
    public void addRecipe() {
        context.addClassFileTransformer(getEditClazzName(), new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(ProfilerPluginInstrumentContext instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(METADATA_NPC_SERVER_ADDRESS);

                InstrumentMethod constructor = target.getConstructor("com.nhncorp.lucy.npc.connector.NpcConnectorOption");
                LucyNetPlugin.addInterceptor(constructor, NPC_CONSTRUCTOR_INTERCEPTOR);

                InstrumentMethod method = target.getDeclaredMethod("invoke", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]");
                LucyNetPlugin.addInterceptor(method, NPC_INVOKE_INTERCEPTOR);

                method = target.getDeclaredMethod("makeMessage", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]");
                LucyNetPlugin.addInterceptor(method, NET_MAKE_MESSAGE_INTERCEPTOR);

                return target.toBytecode();
            }

        });

    }

}
