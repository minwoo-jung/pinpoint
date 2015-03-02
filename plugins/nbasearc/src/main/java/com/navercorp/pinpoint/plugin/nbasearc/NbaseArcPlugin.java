package com.navercorp.pinpoint.plugin.nbasearc;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassCondition;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ConstructorEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorProperty;
import com.navercorp.pinpoint.plugin.nbasearc.filter.NameBasedMethodFilter;
import com.navercorp.pinpoint.plugin.nbasearc.filter.RedisClusterMethodNames;
import com.navercorp.pinpoint.plugin.nbasearc.filter.RedisClusterPipelineMethodNames;

public class NbaseArcPlugin implements ProfilerPlugin, NbaseArcConstants {

    private static final String I_GATEWAY_CONSTRUCTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayConstructorInterceptor";
    private static final String I_GATEWAY_METHOD = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayMethodInterceptor";
    private static final String I_GATEWAY_SERVER_METHOD = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayServerMethodInterceptor";
    
    @Override
    public void setUp(ProfilerPluginSetupContext context) {
        NbaseArcPluginConfig config = new NbaseArcPluginConfig(context.getConfig());

        boolean enabled = config.isEnabled();
        boolean pipelineEnabled = config.isPipelineEnabled();

        if (enabled || pipelineEnabled) {
            addGatewayModifier(context, config);
            addGatewayServerModifier(context, config);

            if (enabled) {
                addRedisClusterModifier(context, config);
                addBinaryRedisClusterModifier(context, config);
                addTriplesRedisClusterModifier(context, config);
                addBinaryTriplesRedisClusterModifier(context, config);
            }

            if (pipelineEnabled) {
                addRedisClusterPipelineModifier(context, config);
            }
        }
    }
    
    private void addGatewayModifier(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        ClassEditorBuilder classEditorBuilder = context.newClassEditorBuilder();
        classEditorBuilder.target("com.nhncorp.redis.cluster.gateway.Gateway");
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);
        
        ConstructorEditorBuilder constructorEditorBuilder = classEditorBuilder.editConstructor("com.nhncorp.redis.cluster.gateway.GatewayConfig");
        constructorEditorBuilder.injectInterceptor(I_GATEWAY_CONSTRUCTOR);

        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new MethodFilter() {
            @Override
            public boolean filter(MethodInfo method) {
                return !method.getName().equals("getServer");
            }
        });
        methodEditorBuilder.injectInterceptor(I_GATEWAY_METHOD);
    }
    
    private void addGatewayServerModifier(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        ClassEditorBuilder classEditorBuilder = context.newClassEditorBuilder();
        classEditorBuilder.target("com.nhncorp.redis.cluster.gateway.GatewayServer");
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);

        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new MethodFilter() {
            @Override
            public boolean filter(MethodInfo method) {
                return !method.getName().equals("getResource");
            }
        });
        
        methodEditorBuilder.injectInterceptor(I_GATEWAY_SERVER_METHOD);
    }
    
    
    private void addRedisClusterModifier(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        ClassEditorBuilder classEditorBuilder = context.newClassEditorBuilder();
        classEditorBuilder.target("com.nhncorp.redis.cluster.RedisCluster");
        
        classEditorBuilder.editConstructor("java.lang.String").injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");
        classEditorBuilder.editConstructor("java.lang.String", "int").injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");
        classEditorBuilder.editConstructor("java.lang.String", "int", "int").injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");

        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(RedisClusterMethodNames.get()));
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterMethodInterceptor");
    }
    
    
    private void addBinaryRedisClusterModifier(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        ClassEditorBuilder classEditorBuilder = context.newClassEditorBuilder();
        classEditorBuilder.target("com.nhncorp.redis.cluster.BinaryRedisCluster");

        classEditorBuilder.editConstructor("java.lang.String").injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");
        classEditorBuilder.editConstructor("java.lang.String", "int").injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");
        classEditorBuilder.editConstructor("java.lang.String", "int", "int").injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");

        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(RedisClusterMethodNames.get()));
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterMethodInterceptor");
        
    }
    
    private void addTriplesRedisClusterModifier(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        ClassEditorBuilder classEditorBuilder = context.newClassEditorBuilder();
        classEditorBuilder.target("com.nhncorp.redis.cluster.triples.TriplesRedisCluster");

        classEditorBuilder.editConstructor("java.lang.String").injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");
        classEditorBuilder.editConstructor("java.lang.String", "int").injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");
        classEditorBuilder.editConstructor("java.lang.String", "int", "int").injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");

        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(RedisClusterMethodNames.get()));
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterMethodInterceptor");        
    }
    
    private void addBinaryTriplesRedisClusterModifier(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        ClassEditorBuilder classEditorBuilder = context.newClassEditorBuilder();
        classEditorBuilder.target("com.nhncorp.redis.cluster.triples.BinaryTriplesRedisCluster");
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);
        classEditorBuilder.injectMetadata(METADATA_END_POINT);

        classEditorBuilder.editConstructor("java.lang.String").injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");
        classEditorBuilder.editConstructor("java.lang.String", "int").injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");
        classEditorBuilder.editConstructor("java.lang.String", "int", "int").injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");

        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(RedisClusterMethodNames.get()));
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterMethodInterceptor");        
    }
    
    private void addRedisClusterPipelineModifier(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        ClassEditorBuilder classEditorBuilder = context.newClassEditorBuilder();
        classEditorBuilder.target("com.nhncorp.redis.cluster.pipeline.RedisClusterPipeline");
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);
        classEditorBuilder.injectMetadata(METADATA_END_POINT);

        ConstructorEditorBuilder constructorEditorBuilder = classEditorBuilder.editConstructor("com.nhncorp.redis.cluster.gateway.GatewayServer");
        constructorEditorBuilder.condition(new ClassCondition() {
            
            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.getConstructor(new String[] {"com.nhncorp.redis.cluster.gateway.GatewayServer"}) != null;
            }
        });
        //constructorEditorBuilder.setProperty(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineConstructorInterceptor");
        
        MethodEditorBuilder setServerMethodEditorBuilder = classEditorBuilder.editMethods(new MethodFilter() {
            @Override
            public boolean filter(MethodInfo method) {
                return !method.getName().equals("setServer");
            }
        });
        setServerMethodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineSetServerMethodInterceptor");
        
        MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(RedisClusterPipelineMethodNames.get()));
        methodEditorBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineMethodInterceptor");
    }
}