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

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginInstrumentContext;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.PinpointClassFileTransformer;

/**
 * 
 * @author jaehong.kim
 *
 */
public class NbaseArcPlugin implements ProfilerPlugin, NbaseArcConstants {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final NbaseArcPluginConfig config = new NbaseArcPluginConfig(context.getConfig());
        final boolean enabled = config.isEnabled();
        final boolean pipelineEnabled = config.isPipelineEnabled();
        final boolean io = config.isIo();

        if (enabled || pipelineEnabled) {
            addGatewayClientClassEditor(context, config);
            addRedisConnectionClassEditor(context);
            if (io) {
                addRedisProtocolClassEditor(context);
            }
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

    private void addGatewayClientClassEditor(ProfilerPluginSetupContext context, final NbaseArcPluginConfig config) {
        context.addClassFileTransformer("com.nhncorp.redis.cluster.gateway.GatewayClient", new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(ProfilerPluginInstrumentContext instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(METADATA_DESTINATION_ID);

                InstrumentMethod constructorMethod = target.getConstructor("com.nhncorp.redis.cluster.gateway.GatewayConfig");
                if (constructorMethod != null) {
                    constructorMethod.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayClientConstructorInterceptor");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name(new int[] { MethodFilters.SYNTHETIC }, RedisClusterMethodNames.get()))) {
                    try {
                        method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayClientMethodInterceptor", config.isIo());
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + method, e);
                        }
                    }
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("pipeline", "pipelineCallback"))) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayClientInternalMethodInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addRedisConnectionClassEditor(final ProfilerPluginSetupContext context) {
        context.addClassFileTransformer("com.nhncorp.redis.cluster.connection.RedisConnection", new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(ProfilerPluginInstrumentContext instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(METADATA_END_POINT);

                InstrumentMethod constructorEditorBuilderArg1 = target.getConstructor("java.lang.String");
                if (constructorEditorBuilderArg1 != null) {
                    constructorEditorBuilderArg1.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisConnectionConstructorInterceptor");
                }

                InstrumentMethod constructorEditorBuilderArg2 = target.getConstructor("java.lang.String", "int");
                if (constructorEditorBuilderArg2 != null) {
                    constructorEditorBuilderArg2.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisConnectionConstructorInterceptor");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("sendCommand"))) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisConnectionSendCommandMethodInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addRedisProtocolClassEditor(final ProfilerPluginSetupContext context) {
        context.addClassFileTransformer("com.nhncorp.redis.cluster.connection.RedisProtocol", new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(ProfilerPluginInstrumentContext instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("sendCommand", "read"))) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisProtocolSendCommandAndReadMethodInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addGatewayServerClassEditor(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        context.addClassFileTransformer("com.nhncorp.redis.cluster.gateway.GatewayServer", new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(ProfilerPluginInstrumentContext instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(METADATA_DESTINATION_ID);

                InstrumentMethod method = target.getDeclaredMethod("getResource");
                if (method != null) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayServerGetResourceMethodInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addGatewayClassEditor(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        context.addClassFileTransformer("com.nhncorp.redis.cluster.gateway.Gateway", new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(ProfilerPluginInstrumentContext instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(METADATA_DESTINATION_ID);

                InstrumentMethod constructor = target.getConstructor("com.nhncorp.redis.cluster.gateway.GatewayConfig");
                if (constructor != null) {
                    constructor.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayConstructorInterceptor");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("getServer"))) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayGetServerMethodInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addRedisClusterClassEditor(ProfilerPluginSetupContext context, NbaseArcPluginConfig config) {
        addRedisClusterExtendedClassEditor(context, "com.nhncorp.redis.cluster.triples.BinaryTriplesRedisCluster", new TransformHandler() {
            @Override
            public void handle(InstrumentClass target) throws InstrumentException {
                target.addField(METADATA_DESTINATION_ID);
                target.addField(METADATA_END_POINT);
            }
        });
        addRedisClusterExtendedClassEditor(context, "com.nhncorp.redis.cluster.triples.TriplesRedisCluster", null);
        addRedisClusterExtendedClassEditor(context, "com.nhncorp.redis.cluster.BinaryRedisCluster", null);
        addRedisClusterExtendedClassEditor(context, "com.nhncorp.redis.cluster.RedisCluster", null);
    }

    private void addRedisClusterExtendedClassEditor(ProfilerPluginSetupContext context, String targetClassName, final TransformHandler handler) {
        context.addClassFileTransformer(targetClassName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(ProfilerPluginInstrumentContext instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);
                if (handler != null) {
                    handler.handle(target);
                }

                InstrumentMethod constructorEditorBuilderArg1 = target.getConstructor("java.lang.String");
                if (constructorEditorBuilderArg1 != null) {
                    constructorEditorBuilderArg1.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");
                }

                InstrumentMethod constructorEditorBuilderArg2 = target.getConstructor("java.lang.String", "int");
                if (constructorEditorBuilderArg2 != null) {
                    constructorEditorBuilderArg2.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");
                }

                InstrumentMethod constructorEditorBuilderArg3 = target.getConstructor("java.lang.String", "int", "int");
                if (constructorEditorBuilderArg3 != null) {
                    constructorEditorBuilderArg3.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name(new int[] { MethodFilters.SYNTHETIC }, RedisClusterMethodNames.get()))) {
                    try {
                        method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterMethodInterceptor");
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + method, e);
                        }
                    }
                }

                return target.toBytecode();
            }
        });
    }

    private void addRedisClusterPipeline(ProfilerPluginSetupContext context, final NbaseArcPluginConfig config) {
        context.addClassFileTransformer("com.nhncorp.redis.cluster.pipeline.RedisClusterPipeline", new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(ProfilerPluginInstrumentContext instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(METADATA_DESTINATION_ID);
                target.addField(METADATA_END_POINT);

                if (target.hasConstructor("com.nhncorp.redis.cluster.gateway.GatewayServer")) {
                    InstrumentMethod constructor = target.getConstructor("com.nhncorp.redis.cluster.gateway.GatewayServer");
                    constructor.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineConstructorInterceptor");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("setServer"))) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineSetServerMethodInterceptor");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name(new int[] { MethodFilters.SYNTHETIC }, RedisClusterPipelineMethodNames.get()))) {
                    try {
                        method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineMethodInterceptor", config.isIo());
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + method, e);
                        }
                    }
                }

                return target.toBytecode();
            }
        });
    }

    private interface TransformHandler {
        void handle(InstrumentClass target) throws InstrumentException;
    }
}