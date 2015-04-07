/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.nbasearc;

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ConstructorEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorExceptionHandler;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorProperty;
import com.navercorp.pinpoint.plugin.nbasearc.filter.GatewayClientMethodNames;
import com.navercorp.pinpoint.plugin.nbasearc.filter.NameBasedMethodFilter;
import com.navercorp.pinpoint.plugin.nbasearc.filter.RedisClusterMethodNames;
import com.navercorp.pinpoint.plugin.nbasearc.filter.RedisClusterPipelineMethodNames;
import com.navercorp.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor;

/**
 * 
 * @author jaehong.kim
 *
 */
public class NbaseArcPlugin implements ProfilerPlugin, NbaseArcConstants {
    private static final String INT = "int";
    private static final String STRING = "java.lang.String";

    private static final String GATEWAY = "com.nhncorp.redis.cluster.gateway.Gateway";
    private static final String GATEWAY_CONSTRUCTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayConstructorInterceptor";
    private static final String GATEWAY_GET_SERVER_METHOD_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayGetServerMethodInterceptor";

    private static final String GATEWAY_SERVER = "com.nhncorp.redis.cluster.gateway.GatewayServer";
    private static final String GATEWAY_SERVER_GET_RESOURCE_METHOD_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayServerGetResourceMethodInterceptor";

    private static final String GATEWAY_CONFIG = "com.nhncorp.redis.cluster.gateway.GatewayConfig";

    private static final String GATEWAY_CLIENT = "com.nhncorp.redis.cluster.gateway.GatewayClient";
    private static final String GATEWAY_CLIENT_CONSTRUCTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayClientConstructorInterceptor";
    private static final String GATEWAY_CLIENT_METHOD_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayClientMethodInterceptor";
    private static final String GATEWAY_CLIENT_INTERNAL_METHOD_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayClientInternalMethodInterceptor";
    
    private static final String REDIS_CLUSTER = "com.nhncorp.redis.cluster.RedisCluster";
    private static final String REDIS_CLUSTER_CONSTRUCTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor";
    private static final String REDIS_CLUSTER_METHOD_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterMethodInterceptor";

    private static final String BINARY_REDIS_CLUSTER = "com.nhncorp.redis.cluster.BinaryRedisCluster";
    private static final String TRIPLES_REDIS_CLUSTER = "com.nhncorp.redis.cluster.triples.TriplesRedisCluster";
    private static final String BINARY_TRIPLES_REDIS_CLUSTER = "com.nhncorp.redis.cluster.triples.BinaryTriplesRedisCluster";

    private static final String REDIS_CLUSTER_PIPELINE = "com.nhncorp.redis.cluster.pipeline.RedisClusterPipeline";
    private static final String REDIS_CLUSTER_PIPELINE_CONSTRUCTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineConstructorInterceptor";
    private static final String REDIS_CLUSTER_PIPELINE_SET_SERVER_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineSetServerInterceptor";
    private static final String REDIS_CLUSTER_PIPELINE_METHOD_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineMethodInterceptor";

    private static final String REDIS_CONNECTION = "com.nhncorp.redis.cluster.connection.RedisConnection";
    private static final String REDIS_CONNECTION_CONSTRUCTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisConnectionConstructorInterceptor";
    private static final String REDIS_CONNECTION_SEND_COMMAND_METHOD_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisConnectionSendCommandMethodInterceptor";
    
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final NbaseArcPluginConfig config = new NbaseArcPluginConfig(context.getConfig());
        final boolean enabled = config.isEnabled();
        final boolean pipelineEnabled = config.isPipelineEnabled();

        if (enabled || pipelineEnabled) {
            addGatewayClientClassEditor(context);
            addRedisConnectionClassEditor(context);
            addGatewayServerClassEditor(context, config);
            addGatewayClassEditor(context, config);

            if (enabled) {
                addRedisClusterClassEditor(context, config);
            }

            if (pipelineEnabled) {
                addRedisClusterPipeline(context, config);
            }
        }
    }

    
    private void addGatewayClientClassEditor(ProfilerPluginSetupContext context) {
        final ClassEditorBuilder classEditorBuilder = context.getClassEditorBuilder(GATEWAY_CLIENT);
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);
        
        final ConstructorEditorBuilder constructorEditorBuilder = classEditorBuilder.editConstructor(GATEWAY_CONFIG);
        constructorEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilder.injectInterceptor(GATEWAY_CLIENT_CONSTRUCTOR_INTERCEPTOR);

        final MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(RedisClusterMethodNames.get()));
        methodEditorBuilder.exceptionHandler(new MethodEditorExceptionHandler() {
            @Override
            public void handle(String targetClassName, String targetMethodName, String[] targetMethodParameterTypes, Throwable exception) throws Exception {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported method " + targetClassName + "." + targetMethodName, exception);
                }
            }
        });
        methodEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor(GATEWAY_CLIENT_METHOD_INTERCEPTOR);
        context.addClassEditor(classEditorBuilder.build());
        
        final MethodEditorBuilder internalMethodEditorBuilder = classEditorBuilder.editMethods(new MethodFilter() {
            @Override
            public boolean filter(MethodInfo method) {
                return !(method.getName().equals("pipeline") || method.getName().equals("pipelineCallback"));
            }
        });
        internalMethodEditorBuilder.injectInterceptor(GATEWAY_CLIENT_INTERNAL_METHOD_INTERCEPTOR);
        internalMethodEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        context.addClassEditor(classEditorBuilder.build());
    }
    
    private void addRedisConnectionClassEditor(ProfilerPluginSetupContext context) {
        final ClassEditorBuilder classEditorBuilder = context.getClassEditorBuilder(REDIS_CONNECTION);
        classEditorBuilder.injectMetadata(METADATA_END_POINT);

        final ConstructorEditorBuilder constructorEditorBuilderArg1 = classEditorBuilder.editConstructor(STRING);
        constructorEditorBuilderArg1.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg1.injectInterceptor(REDIS_CONNECTION_CONSTRUCTOR_INTERCEPTOR);

        final ConstructorEditorBuilder constructorEditorBuilderArg2 = classEditorBuilder.editConstructor(STRING, INT);
        constructorEditorBuilderArg2.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg2.injectInterceptor(REDIS_CONNECTION_CONSTRUCTOR_INTERCEPTOR);

        final MethodEditorBuilder sendCommandMethodEditorBuilder = classEditorBuilder.editMethods(new MethodFilter() {
            @Override
            public boolean filter(MethodInfo method) {
                return !method.getName().equals("sendCommand");
            }
        });
        sendCommandMethodEditorBuilder.injectInterceptor(REDIS_CONNECTION_SEND_COMMAND_METHOD_INTERCEPTOR);
        sendCommandMethodEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        
        context.addClassEditor(classEditorBuilder.build());
    }
    
    private void addGatewayServerClassEditor(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        final ClassEditorBuilder classEditorBuilder = context.getClassEditorBuilder(GATEWAY_SERVER);
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);

        final MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethod("getResource");
        methodEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor(GATEWAY_SERVER_GET_RESOURCE_METHOD_INTERCEPTOR);
        
        context.addClassEditor(classEditorBuilder.build());
    }

    private void addGatewayClassEditor(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        final ClassEditorBuilder classEditorBuilder = context.getClassEditorBuilder(GATEWAY);
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);

        final ConstructorEditorBuilder constructorEditorBuilder = classEditorBuilder.editConstructor(GATEWAY_CONFIG);
        constructorEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilder.injectInterceptor(GATEWAY_CONSTRUCTOR_INTERCEPTOR);

        final MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new MethodFilter() {
            @Override
            public boolean filter(MethodInfo method) {
                return !method.getName().equals("getServer");
            }
        });
        methodEditorBuilder.injectInterceptor(GATEWAY_GET_SERVER_METHOD_INTERCEPTOR);
        
        context.addClassEditor(classEditorBuilder.build());
    }

    private void addRedisClusterClassEditor(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        // super
        final ClassEditorBuilder classEditorBuilder = addRedisClusterExtendedClassEditor(context, config, BINARY_TRIPLES_REDIS_CLUSTER);
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);
        classEditorBuilder.injectMetadata(METADATA_END_POINT);
        context.addClassEditor(classEditorBuilder.build());

        // extends BinaryTriplesRedisCluster
        context.addClassEditor(addRedisClusterExtendedClassEditor(context, config, TRIPLES_REDIS_CLUSTER).build());
        // extends TriplesRedisCluster
        context.addClassEditor(addRedisClusterExtendedClassEditor(context, config, BINARY_REDIS_CLUSTER).build());
        // extends BinaryRedisCluster
        context.addClassEditor(addRedisClusterExtendedClassEditor(context, config, REDIS_CLUSTER).build());
    }

    private ClassEditorBuilder addRedisClusterExtendedClassEditor(ProfilerPluginSetupContext context, NbaseArcPluginConfig config, final String targetClassName) {
        final ClassEditorBuilder classEditorBuilder = context.getClassEditorBuilder(targetClassName);

        final ConstructorEditorBuilder constructorEditorBuilderArg1 = classEditorBuilder.editConstructor(STRING);
        constructorEditorBuilderArg1.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg1.injectInterceptor(REDIS_CLUSTER_CONSTRUCTOR_INTERCEPTOR);

        final ConstructorEditorBuilder constructorEditorBuilderArg2 = classEditorBuilder.editConstructor(STRING, INT);
        constructorEditorBuilderArg2.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg2.injectInterceptor(REDIS_CLUSTER_CONSTRUCTOR_INTERCEPTOR);

        final ConstructorEditorBuilder constructorEditorBuilderArg3 = classEditorBuilder.editConstructor(STRING, INT, INT);
        constructorEditorBuilderArg3.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg3.injectInterceptor(REDIS_CLUSTER_CONSTRUCTOR_INTERCEPTOR);

        final MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(RedisClusterMethodNames.get()));
        methodEditorBuilder.exceptionHandler(new MethodEditorExceptionHandler() {
            @Override
            public void handle(String targetClassName, String targetMethodName, String[] targetMethodParameterTypes, Throwable exception) throws Exception {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported method " + targetClassName + "." + targetMethodName, exception);
                }
            }
        });
        methodEditorBuilder.injectInterceptor(REDIS_CLUSTER_METHOD_INTERCEPTOR);

        return classEditorBuilder;
    }

    private void addRedisClusterPipeline(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        final ClassEditorBuilder classEditorBuilder = context.getClassEditorBuilder(REDIS_CLUSTER_PIPELINE);
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);
        classEditorBuilder.injectMetadata(METADATA_END_POINT);

        final ConstructorEditorBuilder constructorEditorBuilder = classEditorBuilder.editConstructor(GATEWAY_SERVER);
        constructorEditorBuilder.property(MethodEditorProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilder.injectInterceptor(REDIS_CLUSTER_PIPELINE_CONSTRUCTOR_INTERCEPTOR);

        final MethodEditorBuilder setServerMethodEditorBuilder = classEditorBuilder.editMethods(new MethodFilter() {
            @Override
            public boolean filter(MethodInfo method) {
                return !method.getName().equals("setServer");
            }
        });
        setServerMethodEditorBuilder.injectInterceptor(REDIS_CLUSTER_PIPELINE_SET_SERVER_INTERCEPTOR);

        final MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(RedisClusterPipelineMethodNames.get()));
        methodEditorBuilder.exceptionHandler(new MethodEditorExceptionHandler() {
            @Override
            public void handle(String targetClassName, String targetMethodName, String[] targetMethodParameterTypes, Throwable exception) throws Exception {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported method " + targetClassName + "." + targetMethodName, exception);
                }
            }
        });
        methodEditorBuilder.injectInterceptor(REDIS_CLUSTER_PIPELINE_METHOD_INTERCEPTOR);
        
        context.addClassEditor(classEditorBuilder.build());
    }
}