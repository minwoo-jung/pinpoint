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
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConstructorTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerExceptionHandler;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerProperty;
import com.navercorp.pinpoint.plugin.nbasearc.filter.NameBasedMethodFilter;
import com.navercorp.pinpoint.plugin.nbasearc.filter.RedisClusterMethodNames;
import com.navercorp.pinpoint.plugin.nbasearc.filter.RedisClusterPipelineMethodNames;

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
    private static final String REDIS_CLUSTER_PIPELINE_SET_SERVER_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineSetServerMethodInterceptor";
    private static final String REDIS_CLUSTER_PIPELINE_METHOD_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineMethodInterceptor";

    private static final String REDIS_CONNECTION = "com.nhncorp.redis.cluster.connection.RedisConnection";
    private static final String REDIS_CONNECTION_CONSTRUCTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisConnectionConstructorInterceptor";
    private static final String REDIS_CONNECTION_SEND_COMMAND_METHOD_INTERCEPTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisConnectionSendCommandMethodInterceptor";
    
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public void setup(ProfilerPluginContext context) {
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

    
    private void addGatewayClientClassEditor(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder(GATEWAY_CLIENT);
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);
        
        final ConstructorTransformerBuilder constructorEditorBuilder = classEditorBuilder.editConstructor(GATEWAY_CONFIG);
        constructorEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilder.injectInterceptor(GATEWAY_CLIENT_CONSTRUCTOR_INTERCEPTOR);

        final MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(RedisClusterMethodNames.get()));
        methodEditorBuilder.exceptionHandler(new MethodTransformerExceptionHandler() {
            @Override
            public void handle(String targetClassName, String targetMethodName, String[] targetMethodParameterTypes, Throwable exception) throws Exception {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported method " + targetClassName + "." + targetMethodName, exception);
                }
            }
        });
        methodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor(GATEWAY_CLIENT_METHOD_INTERCEPTOR);
        
        final MethodTransformerBuilder internalMethodTransformerBuilder = classEditorBuilder.editMethods(new MethodFilter() {
            @Override
            public boolean filter(MethodInfo method) {
                return !(method.getName().equals("pipeline") || method.getName().equals("pipelineCallback"));
            }
        });
        internalMethodTransformerBuilder.injectInterceptor(GATEWAY_CLIENT_INTERNAL_METHOD_INTERCEPTOR);
        internalMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        context.addClassFileTransformer(classEditorBuilder.build());
    }
    
    private void addRedisConnectionClassEditor(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder(REDIS_CONNECTION);
        classEditorBuilder.injectMetadata(METADATA_END_POINT);

        final ConstructorTransformerBuilder constructorEditorBuilderArg1 = classEditorBuilder.editConstructor(STRING);
        constructorEditorBuilderArg1.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg1.injectInterceptor(REDIS_CONNECTION_CONSTRUCTOR_INTERCEPTOR);

        final ConstructorTransformerBuilder constructorEditorBuilderArg2 = classEditorBuilder.editConstructor(STRING, INT);
        constructorEditorBuilderArg2.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg2.injectInterceptor(REDIS_CONNECTION_CONSTRUCTOR_INTERCEPTOR);

        final MethodTransformerBuilder sendCommandMethodTransformerBuilder = classEditorBuilder.editMethods(new MethodFilter() {
            @Override
            public boolean filter(MethodInfo method) {
                return !method.getName().equals("sendCommand");
            }
        });
        sendCommandMethodTransformerBuilder.injectInterceptor(REDIS_CONNECTION_SEND_COMMAND_METHOD_INTERCEPTOR);
        sendCommandMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        
        context.addClassFileTransformer(classEditorBuilder.build());
    }
    
    private void addGatewayServerClassEditor(ProfilerPluginContext context, NbaseArcPluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder(GATEWAY_SERVER);
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);

        final MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethod("getResource");
        methodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodEditorBuilder.injectInterceptor(GATEWAY_SERVER_GET_RESOURCE_METHOD_INTERCEPTOR);
        
        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private void addGatewayClassEditor(ProfilerPluginContext context, NbaseArcPluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder(GATEWAY);
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);

        final ConstructorTransformerBuilder constructorEditorBuilder = classEditorBuilder.editConstructor(GATEWAY_CONFIG);
        constructorEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilder.injectInterceptor(GATEWAY_CONSTRUCTOR_INTERCEPTOR);

        final MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethods(new MethodFilter() {
            @Override
            public boolean filter(MethodInfo method) {
                return !method.getName().equals("getServer");
            }
        });
        methodEditorBuilder.injectInterceptor(GATEWAY_GET_SERVER_METHOD_INTERCEPTOR);
        
        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private void addRedisClusterClassEditor(ProfilerPluginContext context, NbaseArcPluginConfig config) {
        // super
        final ClassFileTransformerBuilder classEditorBuilder = addRedisClusterExtendedClassEditor(context, config, BINARY_TRIPLES_REDIS_CLUSTER);
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);
        classEditorBuilder.injectMetadata(METADATA_END_POINT);
        context.addClassFileTransformer(classEditorBuilder.build());

        // extends BinaryTriplesRedisCluster
        context.addClassFileTransformer(addRedisClusterExtendedClassEditor(context, config, TRIPLES_REDIS_CLUSTER).build());
        // extends TriplesRedisCluster
        context.addClassFileTransformer(addRedisClusterExtendedClassEditor(context, config, BINARY_REDIS_CLUSTER).build());
        // extends BinaryRedisCluster
        context.addClassFileTransformer(addRedisClusterExtendedClassEditor(context, config, REDIS_CLUSTER).build());
    }

    private ClassFileTransformerBuilder addRedisClusterExtendedClassEditor(ProfilerPluginContext context, NbaseArcPluginConfig config, final String targetClassName) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder(targetClassName);

        final ConstructorTransformerBuilder constructorEditorBuilderArg1 = classEditorBuilder.editConstructor(STRING);
        constructorEditorBuilderArg1.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg1.injectInterceptor(REDIS_CLUSTER_CONSTRUCTOR_INTERCEPTOR);

        final ConstructorTransformerBuilder constructorEditorBuilderArg2 = classEditorBuilder.editConstructor(STRING, INT);
        constructorEditorBuilderArg2.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg2.injectInterceptor(REDIS_CLUSTER_CONSTRUCTOR_INTERCEPTOR);

        final ConstructorTransformerBuilder constructorEditorBuilderArg3 = classEditorBuilder.editConstructor(STRING, INT, INT);
        constructorEditorBuilderArg3.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg3.injectInterceptor(REDIS_CLUSTER_CONSTRUCTOR_INTERCEPTOR);

        final MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(RedisClusterMethodNames.get()));
        methodEditorBuilder.exceptionHandler(new MethodTransformerExceptionHandler() {
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

    private void addRedisClusterPipeline(ProfilerPluginContext context, NbaseArcPluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder(REDIS_CLUSTER_PIPELINE);
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);
        classEditorBuilder.injectMetadata(METADATA_END_POINT);

        final ConstructorTransformerBuilder constructorEditorBuilder = classEditorBuilder.editConstructor(GATEWAY_SERVER);
        constructorEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilder.injectInterceptor(REDIS_CLUSTER_PIPELINE_CONSTRUCTOR_INTERCEPTOR);

        final MethodTransformerBuilder setServerMethodTransformerBuilder = classEditorBuilder.editMethods(new MethodFilter() {
            @Override
            public boolean filter(MethodInfo method) {
                return !method.getName().equals("setServer");
            }
        });
        setServerMethodTransformerBuilder.injectInterceptor(REDIS_CLUSTER_PIPELINE_SET_SERVER_INTERCEPTOR);

        final MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(RedisClusterPipelineMethodNames.get()));
        methodEditorBuilder.exceptionHandler(new MethodTransformerExceptionHandler() {
            @Override
            public void handle(String targetClassName, String targetMethodName, String[] targetMethodParameterTypes, Throwable exception) throws Exception {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported method " + targetClassName + "." + targetMethodName, exception);
                }
            }
        });
        methodEditorBuilder.injectInterceptor(REDIS_CLUSTER_PIPELINE_METHOD_INTERCEPTOR);
        
        context.addClassFileTransformer(classEditorBuilder.build());
    }
}
