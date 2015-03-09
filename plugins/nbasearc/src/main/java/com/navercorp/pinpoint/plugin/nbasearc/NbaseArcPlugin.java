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

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassCondition;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ConstructorEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorBuilder;
import com.navercorp.pinpoint.plugin.nbasearc.filter.NameBasedMethodFilter;
import com.navercorp.pinpoint.plugin.nbasearc.filter.RedisClusterMethodNames;
import com.navercorp.pinpoint.plugin.nbasearc.filter.RedisClusterPipelineMethodNames;

/**
 * 
 * @author jaehong.kim
 *
 */
public class NbaseArcPlugin implements ProfilerPlugin, NbaseArcConstants {

    private static final String T_GATEWAY = "com.nhncorp.redis.cluster.gateway.Gateway";
    private static final String T_GATEWAY_SERVER = "com.nhncorp.redis.cluster.gateway.GatewayServer";
    private static final String T_REDIS_CLUSTER = "com.nhncorp.redis.cluster.RedisCluster";
    private static final String T_BINARY_REDIS_CLUSTER = "com.nhncorp.redis.cluster.BinaryRedisCluster";
    private static final String T_TRIPLES_REDIS_CLUSTER = "com.nhncorp.redis.cluster.triples.TriplesRedisCluster";
    private static final String T_BINARY_TRIPLES_REDIS_CLUSTER = "com.nhncorp.redis.cluster.triples.BinaryTriplesRedisCluster";
    private static final String T_REDIS_CLUSTER_PIPELINE = "com.nhncorp.redis.cluster.pipeline.RedisClusterPipeline";

    private static final String I_GATEWAY_CONSTRUCTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayConstructorInterceptor";
    private static final String I_GATEWAY_GET_SERVER_METHOD = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayGetServerMethodInterceptor";
    private static final String I_GATEWAY_SERVER_GET_RESOURCE_METHOD = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayServerGetResourceMethodInterceptor";
    private static final String I_REDIS_CLUSTER_CONSTRUCTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor";
    private static final String I_REDIS_CLUSTER_METHOD = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterMethodInterceptor";
    private static final String I_REDIS_CLUSTER_PIPELINE_CONSTRUCTOR = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineConstructorInterceptor";
    private static final String I_REDIS_CLUSTER_PIPELINE_SET_SERVER = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineSetServerInterceptor";
    private static final String I_REDIS_CLUSTER_PIPELINE_METHOD = "com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineMethodInterceptor";

    private static final String ARG_GATEWAY_SERVER = "com.nhncorp.redis.cluster.gateway.GatewayServer";
    private static final String ARG_GATEWAY_CONFIG = "com.nhncorp.redis.cluster.gateway.GatewayConfig";
    private static final String ARG_STRING = "java.lang.String";
    private static final String ARG_INT = "int";

    @Override
    public void setUp(ProfilerPluginSetupContext context) {
        final NbaseArcPluginConfig config = new NbaseArcPluginConfig(context.getConfig());
        final boolean enabled = config.isEnabled();
        final boolean pipelineEnabled = config.isPipelineEnabled();

        if (enabled || pipelineEnabled) {
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

    private void addGatewayServerClassEditor(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        final ClassEditorBuilder classEditorBuilder = context.newClassEditorBuilder();
        classEditorBuilder.target(T_GATEWAY_SERVER);
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);

        final MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethod("getResource");
        methodEditorBuilder.condition(new ClassCondition() {
            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.hasDeclaredMethod("getResource", new String[] {});
            }
        });
        methodEditorBuilder.injectInterceptor(I_GATEWAY_SERVER_GET_RESOURCE_METHOD);
    }

    private void addGatewayClassEditor(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        final ClassEditorBuilder classEditorBuilder = context.newClassEditorBuilder();
        classEditorBuilder.target(T_GATEWAY);
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);

        final ConstructorEditorBuilder constructorEditorBuilder = classEditorBuilder.editConstructor(ARG_GATEWAY_CONFIG);
        constructorEditorBuilder.condition(new ClassCondition() {
            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.getConstructor(new String[] { ARG_GATEWAY_CONFIG }) != null;
            }
        });
        constructorEditorBuilder.injectInterceptor(I_GATEWAY_CONSTRUCTOR);

        final MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new MethodFilter() {
            @Override
            public boolean filter(MethodInfo method) {
                return !method.getName().equals("getServer");
            }
        });
        methodEditorBuilder.injectInterceptor(I_GATEWAY_GET_SERVER_METHOD);
    }

    private void addRedisClusterClassEditor(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        // super
        final ClassEditorBuilder classEditorBuilder = addExtended(context, config, T_BINARY_TRIPLES_REDIS_CLUSTER);
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);
        classEditorBuilder.injectMetadata(METADATA_END_POINT);

        // extends
        addExtended(context, config, T_TRIPLES_REDIS_CLUSTER);
        addExtended(context, config, T_BINARY_REDIS_CLUSTER);
        addExtended(context, config, T_REDIS_CLUSTER);
    }

    private ClassEditorBuilder addExtended(ProfilerPluginSetupContext context, NbaseArcPluginConfig config, final String targetClassName) {
        final ClassEditorBuilder classEditorBuilder = context.newClassEditorBuilder();
        classEditorBuilder.target(targetClassName);

        final ConstructorEditorBuilder constructorEditorBuilderArg1 = classEditorBuilder.editConstructor(ARG_STRING);
        constructorEditorBuilderArg1.condition(new ClassCondition() {
            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.getConstructor(new String[] { ARG_STRING }) != null;
            }
        });
        constructorEditorBuilderArg1.injectInterceptor(I_REDIS_CLUSTER_CONSTRUCTOR);

        final ConstructorEditorBuilder constructorEditorBuilderArg2 = classEditorBuilder.editConstructor(ARG_STRING, ARG_INT);
        constructorEditorBuilderArg2.condition(new ClassCondition() {
            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.getConstructor(new String[] { ARG_STRING, ARG_INT }) != null;
            }
        });
        constructorEditorBuilderArg2.injectInterceptor(I_REDIS_CLUSTER_CONSTRUCTOR);

        final ConstructorEditorBuilder constructorEditorBuilderArg3 = classEditorBuilder.editConstructor(ARG_STRING, ARG_INT, ARG_INT);
        constructorEditorBuilderArg3.condition(new ClassCondition() {
            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.getConstructor(new String[] { ARG_STRING, ARG_INT, ARG_INT }) != null;
            }
        });
        constructorEditorBuilderArg3.injectInterceptor(I_REDIS_CLUSTER_CONSTRUCTOR);

        // TODO check for method parameter exists
        final MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(RedisClusterMethodNames.get()));
        methodEditorBuilder.injectInterceptor(I_REDIS_CLUSTER_METHOD);

        return classEditorBuilder;
    }

    private void addRedisClusterPipeline(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        final ClassEditorBuilder classEditorBuilder = context.newClassEditorBuilder();
        classEditorBuilder.target(T_REDIS_CLUSTER_PIPELINE);
        classEditorBuilder.injectMetadata(METADATA_DESTINATION_ID);
        classEditorBuilder.injectMetadata(METADATA_END_POINT);

        final ConstructorEditorBuilder constructorEditorBuilder = classEditorBuilder.editConstructor(ARG_GATEWAY_SERVER);
        constructorEditorBuilder.condition(new ClassCondition() {
            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.getConstructor(new String[] { ARG_GATEWAY_SERVER }) != null;
            }
        });
        constructorEditorBuilder.injectInterceptor(I_REDIS_CLUSTER_PIPELINE_CONSTRUCTOR);

        final MethodEditorBuilder setServerMethodEditorBuilder = classEditorBuilder.editMethods(new MethodFilter() {
            @Override
            public boolean filter(MethodInfo method) {
                if (!method.getName().equals("setServer")) {
                    return true;
                }

                final String[] types = method.getParameterTypes();
                if (types == null || types.length < 1 || !types[0].equals(ARG_GATEWAY_SERVER)) {
                    return true;
                }

                return false;
            }
        });
        setServerMethodEditorBuilder.injectInterceptor(I_REDIS_CLUSTER_PIPELINE_SET_SERVER);

        // TODO check for method parameter exists
        final MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(RedisClusterPipelineMethodNames.get()));
        methodEditorBuilder.injectInterceptor(I_REDIS_CLUSTER_PIPELINE_METHOD);
    }
}