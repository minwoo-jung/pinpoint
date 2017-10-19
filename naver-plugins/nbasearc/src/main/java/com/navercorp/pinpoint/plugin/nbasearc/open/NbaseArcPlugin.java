/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.nbasearc.open;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.nbasearc.GatewayClientMethodNameFilter;
import com.navercorp.pinpoint.plugin.nbasearc.NbaseArcConstants;
import com.navercorp.pinpoint.plugin.nbasearc.NbaseArcPluginConfig;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * for opensource version.
 *
 * @author jaehong.kim
 */
public class NbaseArcPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final GatewayClientMethodNameFilter methodNameFilter = new GatewayClientMethodNameFilter();

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final NbaseArcPluginConfig config = new NbaseArcPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            if (logger.isInfoEnabled()) {
                logger.info("Disable NbaseArcPlugin. version range=[1.5,) config={}", config);
            }
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Enable NbaseArcPlugin. version range=[1.5,) config={}", config);
        }

        addGatewayClient(config);
        addRedisConnection();
        if (config.isIo()) {
            addRedisProtocol();
        }
        addGatewayServer(config);
        addGateway();
        addRedisCluster();
        if (config.isPipeline()) {
            addRedisClusterPipeline(config);
        }
    }

    private void addGatewayClient(final NbaseArcPluginConfig config) {
        transformTemplate.transform("com.navercorp.redis.cluster.gateway.GatewayClient", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(NbaseArcConstants.DESTINATION_ID_ACCESSOR);

                final InstrumentMethod constructorMethod = target.getConstructor("com.navercorp.redis.cluster.gateway.GatewayConfig");
                if (constructorMethod != null) {
                    constructorMethod.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.open.interceptor.GatewayClientConstructorInterceptor");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(methodNameFilter, MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
                    try {
                        method.addScopedInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayClientMethodInterceptor", va(config.isIo()), NbaseArcConstants.NBASE_ARC_SCOPE);
                    } catch (Exception e) {
                        if (logger.isInfoEnabled()) {
                            logger.info("Unsupported method {}", method, e);
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

    private void addRedisConnection() {
        transformTemplate.transform("com.navercorp.redis.cluster.connection.RedisConnection", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(NbaseArcConstants.END_POINT_ACCESSOR);

                final InstrumentMethod constructorEditorBuilderArg1 = target.getConstructor("java.lang.String");
                if (constructorEditorBuilderArg1 != null) {
                    constructorEditorBuilderArg1.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisConnectionConstructorInterceptor");
                }

                final InstrumentMethod constructorEditorBuilderArg2 = target.getConstructor("java.lang.String", "int");
                if (constructorEditorBuilderArg2 != null) {
                    constructorEditorBuilderArg2.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisConnectionConstructorInterceptor");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("sendCommand"))) {
                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisConnectionSendCommandMethodInterceptor", NbaseArcConstants.NBASE_ARC_SCOPE, ExecutionPolicy.INTERNAL);
                }

                return target.toBytecode();
            }
        });
    }

    private void addRedisProtocol() {
        transformTemplate.transform("com.navercorp.redis.cluster.connection.RedisProtocol", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("sendCommand", "read"))) {
                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisProtocolSendCommandAndReadMethodInterceptor", NbaseArcConstants.NBASE_ARC_SCOPE, ExecutionPolicy.INTERNAL);
                }

                return target.toBytecode();
            }
        });
    }

    private void addGatewayServer(NbaseArcPluginConfig config) {
        transformTemplate.transform("com.navercorp.redis.cluster.gateway.GatewayServer", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(NbaseArcConstants.DESTINATION_ID_ACCESSOR);

                final InstrumentMethod method = target.getDeclaredMethod("getResource");
                if (method != null) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayServerGetResourceMethodInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addGateway() {
        transformTemplate.transform("com.navercorp.redis.cluster.gateway.Gateway", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(NbaseArcConstants.DESTINATION_ID_ACCESSOR);

                final InstrumentMethod constructor = target.getConstructor("com.navercorp.redis.cluster.gateway.GatewayConfig");
                if (constructor != null) {
                    constructor.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.open.interceptor.GatewayConstructorInterceptor");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("getServer"))) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayGetServerMethodInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addRedisCluster() {
        addRedisClusterExtends("com.navercorp.redis.cluster.triples.BinaryTriplesRedisCluster", new TransformHandler() {
            @Override
            public void handle(InstrumentClass target) throws InstrumentException {
                target.addField(NbaseArcConstants.DESTINATION_ID_ACCESSOR);
                target.addField(NbaseArcConstants.END_POINT_ACCESSOR);
            }
        });
        addRedisClusterExtends("com.navercorp.redis.cluster.triples.TriplesRedisCluster", null);
        addRedisClusterExtends("com.navercorp.redis.cluster.BinaryRedisCluster", null);
        addRedisClusterExtends("com.navercorp.redis.cluster.RedisCluster", null);
    }

    private void addRedisClusterExtends(String targetClassName, final TransformHandler handler) {
        transformTemplate.transform(targetClassName, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                if (handler != null) {
                    handler.handle(target);
                }

                final InstrumentMethod constructorEditorBuilderArg1 = target.getConstructor("java.lang.String");
                if (constructorEditorBuilderArg1 != null) {
                    constructorEditorBuilderArg1.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");
                }

                final InstrumentMethod constructorEditorBuilderArg2 = target.getConstructor("java.lang.String", "int");
                if (constructorEditorBuilderArg2 != null) {
                    constructorEditorBuilderArg2.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");
                }

                final InstrumentMethod constructorEditorBuilderArg3 = target.getConstructor("java.lang.String", "int", "int");
                if (constructorEditorBuilderArg3 != null) {
                    constructorEditorBuilderArg3.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(methodNameFilter, MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
                    try {
                        method.addScopedInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterMethodInterceptor", NbaseArcConstants.NBASE_ARC_SCOPE);
                    } catch (Exception e) {
                        if (logger.isInfoEnabled()) {
                            logger.info("Unsupported method {}", method, e);
                        }
                    }
                }

                return target.toBytecode();
            }
        });
    }

    private void addRedisClusterPipeline(final NbaseArcPluginConfig config) {
        transformTemplate.transform("com.navercorp.redis.cluster.pipeline.RedisClusterPipeline", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(NbaseArcConstants.DESTINATION_ID_ACCESSOR);
                target.addField(NbaseArcConstants.END_POINT_ACCESSOR);

                if (target.hasConstructor("com.navercorp.redis.cluster.gateway.GatewayServer")) {
                    final InstrumentMethod constructor = target.getConstructor("com.navercorp.redis.cluster.gateway.GatewayServer");
                    constructor.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.open.interceptor.RedisClusterPipelineConstructorInterceptor");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("setServer"))) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.nbasearc.open.interceptor.RedisClusterPipelineSetServerMethodInterceptor");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(methodNameFilter, MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
                    try {
                        method.addScopedInterceptor("com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineMethodInterceptor", va(config.isIo()), NbaseArcConstants.NBASE_ARC_SCOPE);
                    } catch (Exception e) {
                        if (logger.isInfoEnabled()) {
                            logger.info("Unsupported method {}", method, e);
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