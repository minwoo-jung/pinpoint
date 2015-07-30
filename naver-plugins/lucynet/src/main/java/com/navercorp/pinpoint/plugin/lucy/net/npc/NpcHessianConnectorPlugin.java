package com.navercorp.pinpoint.plugin.lucy.net.npc;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentableClass;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassCondition;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerSetup;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;

/**
 * @author Taejin Koo
 */
class NpcHessianConnectorPlugin extends NpcPlugin {
    
    public NpcHessianConnectorPlugin(ProfilerPluginSetupContext context) {
        super(context);
    }

    @Override
    public String getEditClazzName() {
        return "com.nhncorp.lucy.npc.connector.NpcHessianConnector";
    }

    @Override
    public void addRecipe() {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder(getEditClazzName());
        builder.injectMetadata(LucyNetConstants.METADATA_NPC_SERVER_ADDRESS);

        builder.conditional(new NpcHessianConnectorCondition("V13to19"), new ConditionalClassFileTransformerSetup() {

            @Override
            public void setup(ConditionalClassFileTransformerBuilder conditional) {
                editMethod(conditional, "createConnecor", new String[] { "com.nhncorp.lucy.npc.connector.NpcConnectorOption" }, "com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.CreateConnectorInterceptor");
                
                editInternalInvokeMethod(conditional);
            }}
        );
        

        builder.conditional(new NpcHessianConnectorCondition("V12"), new ConditionalClassFileTransformerSetup() {

            @Override
            public void setup(ConditionalClassFileTransformerBuilder conditional) {
                editMethod(conditional, "initialize", new String[] { "com.nhncorp.lucy.npc.connector.NpcConnectorOption" }, "com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.CreateConnectorInterceptor");
                
                editInternalInvokeMethod(conditional);
            }}
        );
        
        builder.conditional(new NpcHessianConnectorCondition("V11"), new ConditionalClassFileTransformerSetup() {

            @Override
            public void setup(ConditionalClassFileTransformerBuilder conditional) {
                editConnectorConstructor(conditional);
                
                editInternalInvokeMethod(conditional);
            }}
        );

        builder.conditional(new NpcHessianConnectorCondition("V6to10"), new ConditionalClassFileTransformerSetup() {

            @Override
            public void setup(ConditionalClassFileTransformerBuilder conditional) {
                editConstructor(conditional, new String[] {"java.net.InetSocketAddress", "boolean", "boolean", "boolean", "java.nio.charset.Charset", "long"}, "com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.OldVersionConnectorConstructorInterceptor");
                editConstructor(conditional, new String[] {"java.net.InetSocketAddress", "com.nhncorp.lucy.npc.connector.ConnectionFactory"}, "com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.OldVersionConnectorConstructorInterceptor");

                editMethod(conditional, "invoke", new String[] { "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]" }, "com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.InvokeInterceptor");
            }}
        );

        builder.conditional(new NpcHessianConnectorCondition("V5"), new ConditionalClassFileTransformerSetup() {

            @Override
            public void setup(ConditionalClassFileTransformerBuilder conditional) {
                editConstructor(conditional, new String[] {"java.net.InetSocketAddress", "boolean", "boolean", "boolean", "java.nio.charset.Charset", "long"}, "com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.OldVersionConnectorConstructorInterceptor");

                editMethod(conditional, "invoke", new String[] { "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]" }, "com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.InvokeInterceptor");
            }}
        );
        
        context.addClassFileTransformer(builder.build());
    }

    private class NpcHessianConnectorCondition implements ClassCondition {
    
        private final String versionName;
        
        public NpcHessianConnectorCondition(String versionName) {
            this.versionName = versionName;
        }
        
        @Override
        public boolean check(ProfilerPluginSetupContext context, ClassLoader classLoader, InstrumentableClass target) {
            return NpcHessianConnectorConditionType.getCondition(versionName).isSupport(target);
        }
    }
}