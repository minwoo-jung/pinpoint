package com.navercorp.pinpoint.plugin.lucy.net.npc;

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;

/**
 * @author Taejin Koo
 */
class LegacyLightWeightPlugin extends NpcPlugin {

    public LegacyLightWeightPlugin(ProfilerPluginSetupContext context) {
        super(context);
    }

    @Override
    public String getEditClazzName() {
        return "com.nhncorp.lucy.npc.connector.LightweightConnector";
    }

    @Override
    public void addRecipe() {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder(getEditClazzName());
        builder.injectMetadata(LucyNetConstants.METADATA_NPC_SERVER_ADDRESS);

        builder.editConstructor("com.nhncorp.lucy.npc.connector.NpcConnectorOption").injectInterceptor("com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.ConnectorConstructorInterceptor");
        builder.editMethod("invoke", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]").injectInterceptor("com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.InvokeInterceptor");

        context.addClassFileTransformer(builder.build());
    }
    
}
