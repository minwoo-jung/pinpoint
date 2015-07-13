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
class LightWeightNpcHessianPlugin extends NpcPlugin {
    
    public LightWeightNpcHessianPlugin(ProfilerPluginContext context) {
        super(context);
    }

    @Override
    public String getEditClazzName() {
        return "com.nhncorp.lucy.npc.connector.LightWeightNpcHessianConnector";
    }

    @Override
    public void addRecipe() {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder(getEditClazzName());
        builder.injectMetadata(LucyNetConstants.METADATA_NPC_SERVER_ADDRESS);
        
        builder.conditional(new V11to12ClassCondition(), new ConditionalClassFileTransformerSetup() {

            @Override
            public void setup(ConditionalClassFileTransformerBuilder conditional) {
                editConnectorConstructor(conditional);
                
                editMethod(conditional, "invoke", new String[] { "java.lang.String", "java.lang.String", "java.lang.Object[]" }, "com.navercorp.pinpoint.plugin.lucy.net.npc.interceptor.InvokeInterceptor");
            }
            
        });
        
        context.addClassFileTransformer(builder.build());
    }

    private class V11to12ClassCondition implements ClassCondition {

        @Override
        public boolean check(ProfilerPluginContext context, ClassLoader classLoader, InstrumentClass target) {
            String superClazz = target.getSuperClass();
            if (superClazz != null && superClazz.equals("com.nhncorp.lucy.npc.connector.AbstractNpcHessianConnector")) {
                return true;
            }
            
            return false;
        }
        
    }
    
}
