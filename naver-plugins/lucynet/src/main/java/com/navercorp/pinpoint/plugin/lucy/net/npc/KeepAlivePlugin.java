package com.navercorp.pinpoint.plugin.lucy.net.npc;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassCondition;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerSetup;
import com.navercorp.pinpoint.plugin.lucy.net.LucyNetConstants;

/**
 * @author Taejin Koo
 */
class KeepAlivePlugin extends NpcPlugin {

    public KeepAlivePlugin(ProfilerPluginContext context) {
        super(context);
    }

    @Override
    public String getEditClazzName() {
        return "com.nhncorp.lucy.npc.connector.KeepAliveNpcHessianConnector";
    }

    @Override
    public void addRecipe() {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder(getEditClazzName());
        builder.injectMetadata(LucyNetConstants.METADATA_NPC_SERVER_ADDRESS);
        
        builder.conditional(new V13to19ClassCondition(), new ConditionalClassFileTransformerSetup() {
            
            @Override
            public void setup(ConditionalClassFileTransformerBuilder conditional) {
                editConnectorConstructor(conditional);
                editConstructor(conditional, new String[] {"java.net.InetSocketAddress", "long", "long", "java.nio.charset.Charset"}, "com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.ConnectorConstructorInterceptor");
                
                editInitializeConnectorMethod(conditional);
                editMethod(conditional, "invokeImpl", new String[] { "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]" }, "com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor", LucyNetConstants.NPC_CLIENT_INTERNAL);
            }
        });
        
        builder.conditional(new V11to12ClassCondition(), new ConditionalClassFileTransformerSetup() {
            
            @Override
            public void setup(ConditionalClassFileTransformerBuilder conditional) {
                editConnectorConstructor(conditional);

                editInitializeConnectorMethod(conditional);
                editMethod(conditional, "invokeImpl", new String[] { "java.lang.String", "java.lang.String", "java.lang.Object[]" }, "com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor", LucyNetConstants.NPC_CLIENT_INTERNAL);
            }
        });

        
        builder.conditional(new V8to10ClassCondition(), new ConditionalClassFileTransformerSetup() {
            
            @Override
            public void setup(ConditionalClassFileTransformerBuilder conditional) {
                editConstructor(conditional, new String[] {"java.net.InetSocketAddress", "long", "long", "java.nio.charset.Charset"}, "com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.ConnectorConstructorInterceptor");
                
                editInitializeConnectorMethod(conditional);
                editMethod(conditional, "invoke", new String[] { "java.lang.String", "java.lang.String", "java.lang.Object[]" }, "com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor", LucyNetConstants.NPC_CLIENT_INTERNAL);
            }
        });
        
        context.addClassFileTransformer(builder.build());
    }

    private class V13to19ClassCondition implements ClassCondition {

        @Override
        public boolean check(ProfilerPluginContext context, ClassLoader classLoader, InstrumentClass target) {
            if (target.hasConstructor(new String[] { "com.nhncorp.lucy.npc.connector.NpcConnectorOption" })) {
                if (target.hasDeclaredMethod("invokeImpl", new String[] {"java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]"})) {
                    return true;
                }
            }

            return false;
        }

    }

    private class V11to12ClassCondition implements ClassCondition {

        @Override
        public boolean check(ProfilerPluginContext context, ClassLoader classLoader, InstrumentClass target) {
            if (target.hasConstructor(new String[] { "com.nhncorp.lucy.npc.connector.NpcConnectorOption" })) {
                if (target.hasDeclaredMethod("invokeImpl", new String[] {"java.lang.String", "java.lang.String", "java.lang.Object[]"})) {
                    return true;
                }
            }
            
            return false;
        }

    }

    private class V8to10ClassCondition implements ClassCondition {

        @Override
        public boolean check(ProfilerPluginContext context, ClassLoader classLoader, InstrumentClass target) {
            if (target.hasConstructor(new String[] { "com.nhncorp.lucy.npc.connector.NpcConnectorOption" })) {
                return false;
            }
            
            return true;
        }
        
    }
}