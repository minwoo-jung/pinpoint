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
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * 
 * @author jaehong.kim
 *
 */
public class NbaseArcPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final NbaseArcPluginConfig config = new NbaseArcPluginConfig(context.getConfig());
        final boolean enabled = config.isEnabled();
        final boolean pipelineEnabled = config.isPipelineEnabled();
        final boolean io = config.isIo();

        if (enabled || pipelineEnabled) {
            addGatewayClientClassEditor(config);
            addRedisConnectionClassEditor();
            if (io) {
                addRedisProtocolClassEditor();
            }
            addGatewayServerClassEditor(config);
            addGatewayClassEditor();

            if (enabled) {
                addRedisClusterClassEditor();
            }

            if (pipelineEnabled) {
                addRedisClusterPipeline(config);
            }
        }
    }

    private void addGatewayClientClassEditor(final NbaseArcPluginConfig config) {
        transformTemplate.transform("com.nhncorp.redis.cluster.gateway.GatewayClient", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(DestinationIdAccessor.class.getName());

                InstrumentMethod constructorMethod = target.getConstructor("com.nhncorp.redis.cluster.gateway.GatewayConfig");
                if (constructorMethod != null) {
                    constructorMethod.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayClientConstructorInterceptor");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.name(RedisClusterMethodNames.get()), MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
                    try {
                        method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayClientMethodInterceptor", va(config.isIo()));
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

    private void addRedisConnectionClassEditor() {
        transformTemplate.transform("com.nhncorp.redis.cluster.connection.RedisConnection", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(EndPointAccessor.class.getName());

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

    private void addRedisProtocolClassEditor() {
        transformTemplate.transform("com.nhncorp.redis.cluster.connection.RedisProtocol", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("sendCommand", "read"))) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisProtocolSendCommandAndReadMethodInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addGatewayServerClassEditor(NbaseArcPluginConfig config) {
        transformTemplate.transform("com.nhncorp.redis.cluster.gateway.GatewayServer", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(DestinationIdAccessor.class.getName());

                InstrumentMethod method = target.getDeclaredMethod("getResource");
                if (method != null) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayServerGetResourceMethodInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addGatewayClassEditor() {
        transformTemplate.transform("com.nhncorp.redis.cluster.gateway.Gateway", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(DestinationIdAccessor.class.getName());

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

    private void addRedisClusterClassEditor() {
        addRedisClusterExtendedClassEditor("com.nhncorp.redis.cluster.triples.BinaryTriplesRedisCluster", new TransformHandler() {
            @Override
            public void handle(InstrumentClass target) throws InstrumentException {
                target.addField(DestinationIdAccessor.class.getName());
                target.addField(EndPointAccessor.class.getName());
            }
        });
        addRedisClusterExtendedClassEditor("com.nhncorp.redis.cluster.triples.TriplesRedisCluster", null);
        addRedisClusterExtendedClassEditor("com.nhncorp.redis.cluster.BinaryRedisCluster", null);
        addRedisClusterExtendedClassEditor("com.nhncorp.redis.cluster.RedisCluster", null);
    }

    private void addRedisClusterExtendedClassEditor(String targetClassName, final TransformHandler handler) {
        transformTemplate.transform(targetClassName, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
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

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.name(RedisClusterMethodNames.get()), MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
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

    private void addRedisClusterPipeline(final NbaseArcPluginConfig config) {
        transformTemplate.transform("com.nhncorp.redis.cluster.pipeline.RedisClusterPipeline", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(DestinationIdAccessor.class.getName());
                target.addField(EndPointAccessor.class.getName());

                if (target.hasConstructor("com.nhncorp.redis.cluster.gateway.GatewayServer")) {
                    InstrumentMethod constructor = target.getConstructor("com.nhncorp.redis.cluster.gateway.GatewayServer");
                    constructor.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineConstructorInterceptor");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("setServer"))) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineSetServerMethodInterceptor");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.name(RedisClusterPipelineMethodNames.get()), MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
                    try {
                        method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineMethodInterceptor", va(config.isIo()));
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

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}