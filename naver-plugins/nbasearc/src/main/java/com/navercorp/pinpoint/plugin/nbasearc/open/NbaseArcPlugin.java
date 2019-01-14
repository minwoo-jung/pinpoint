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

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final NbaseArcPluginConfig config = new NbaseArcPluginConfig(context.getConfig());

        if (!config.isEnable()) {
            if (logger.isInfoEnabled()) {
                logger.info("Disable {}. version range=[1.5,) config={}", this.getClass().getSimpleName(), config);
            }
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Enable {}. version range=[1.5,) config={}", this.getClass().getSimpleName(), config);
        }

        addGatewayClient();
        addRedisConnection();
        if (config.isIo()) {
            addRedisProtocol();
        }
        addGatewayServer();
        addGateway();
        addRedisCluster();
        if (config.isPipeline()) {
            addRedisClusterPipeline();
        }
    }

    private void addGatewayClient() {
        transformTemplate.transform("com.navercorp.redis.cluster.gateway.GatewayClient", GatewayClientTransform.class);
    }

    public static class GatewayClientTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(com.navercorp.pinpoint.plugin.nbasearc.DestinationIdAccessor.class);

            final InstrumentMethod constructorMethod = target.getConstructor("com.navercorp.redis.cluster.gateway.GatewayConfig");
            if (constructorMethod != null) {
                constructorMethod.addInterceptor(com.navercorp.pinpoint.plugin.nbasearc.open.interceptor.GatewayClientConstructorInterceptor.class);
            }

            final NbaseArcPluginConfig config = new NbaseArcPluginConfig(instrumentor.getProfilerConfig());
            final GatewayClientMethodNameFilter methodNameFilter = new GatewayClientMethodNameFilter();
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(methodNameFilter, MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
                try {
                    method.addScopedInterceptor(com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayClientMethodInterceptor.class, va(config.isIo()), NbaseArcConstants.NBASE_ARC_SCOPE);
                } catch (Exception e) {
                    PLogger logger = PLoggerFactory.getLogger(this.getClass());
                    if (logger.isInfoEnabled()) {
                        logger.info("Unsupported method {}", method, e);
                    }
                }
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("pipeline", "pipelineCallback"))) {
                method.addInterceptor(com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayClientInternalMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addRedisConnection() {
        transformTemplate.transform("com.navercorp.redis.cluster.connection.RedisConnection", RedisConnectionTransform.class);
    }

    public static class RedisConnectionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(com.navercorp.pinpoint.plugin.nbasearc.EndPointAccessor.class);

            final InstrumentMethod constructorEditorBuilderArg1 = target.getConstructor("java.lang.String");
            if (constructorEditorBuilderArg1 != null) {
                constructorEditorBuilderArg1.addInterceptor(com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisConnectionConstructorInterceptor.class);
            }

            final InstrumentMethod constructorEditorBuilderArg2 = target.getConstructor("java.lang.String", "int");
            if (constructorEditorBuilderArg2 != null) {
                constructorEditorBuilderArg2.addInterceptor(com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisConnectionConstructorInterceptor.class);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("sendCommand"))) {
                method.addScopedInterceptor(com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisConnectionSendCommandMethodInterceptor.class, NbaseArcConstants.NBASE_ARC_SCOPE, ExecutionPolicy.INTERNAL);
            }

            return target.toBytecode();
        }
    }

    private void addRedisProtocol() {
        transformTemplate.transform("com.navercorp.redis.cluster.connection.RedisProtocol", RedisProtocolTransform.class);
    }

    public static class RedisProtocolTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("sendCommand", "read"))) {
                method.addScopedInterceptor(com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisProtocolSendCommandAndReadMethodInterceptor.class, NbaseArcConstants.NBASE_ARC_SCOPE, ExecutionPolicy.INTERNAL);
            }

            return target.toBytecode();
        }
    }

    private void addGatewayServer() {
        transformTemplate.transform("com.navercorp.redis.cluster.gateway.GatewayServer", GatewayServerTransform.class);
    }

    public static class GatewayServerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(com.navercorp.pinpoint.plugin.nbasearc.DestinationIdAccessor.class);

            final InstrumentMethod method = target.getDeclaredMethod("getResource");
            if (method != null) {
                method.addInterceptor(com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayServerGetResourceMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addGateway() {
        transformTemplate.transform("com.navercorp.redis.cluster.gateway.Gateway", GatewayTransform.class);
    }

    public static class GatewayTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(com.navercorp.pinpoint.plugin.nbasearc.DestinationIdAccessor.class);

            final InstrumentMethod constructor = target.getConstructor("com.navercorp.redis.cluster.gateway.GatewayConfig");
            if (constructor != null) {
                constructor.addInterceptor(com.navercorp.pinpoint.plugin.nbasearc.open.interceptor.GatewayConstructorInterceptor.class);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("getServer"))) {
                method.addInterceptor(com.navercorp.pinpoint.plugin.nbasearc.interceptor.GatewayGetServerMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addRedisCluster() {
        addRedisClusterExtends("com.navercorp.redis.cluster.triples.BinaryTriplesRedisCluster", BinaryTriplesRedisClusterTransform.class);
        addRedisClusterExtends("com.navercorp.redis.cluster.triples.TriplesRedisCluster", RedisClusterTransform.class);
        addRedisClusterExtends("com.navercorp.redis.cluster.BinaryRedisCluster", RedisClusterTransform.class);
        addRedisClusterExtends("com.navercorp.redis.cluster.RedisCluster", RedisClusterTransform.class);
    }

    private void addRedisClusterExtends(String targetClassName, final Class<? extends TransformCallback> transformCallback) {
        transformTemplate.transform(targetClassName, transformCallback);
    }

    public static class RedisClusterTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            handle(target);

            final InstrumentMethod constructorEditorBuilderArg1 = target.getConstructor("java.lang.String");
            if (constructorEditorBuilderArg1 != null) {
                constructorEditorBuilderArg1.addInterceptor(com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor.class);
            }

            final InstrumentMethod constructorEditorBuilderArg2 = target.getConstructor("java.lang.String", "int");
            if (constructorEditorBuilderArg2 != null) {
                constructorEditorBuilderArg2.addInterceptor(com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor.class);
            }

            final InstrumentMethod constructorEditorBuilderArg3 = target.getConstructor("java.lang.String", "int", "int");
            if (constructorEditorBuilderArg3 != null) {
                constructorEditorBuilderArg3.addInterceptor(com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterConstructorInterceptor.class);
            }

            final GatewayClientMethodNameFilter methodNameFilter = new GatewayClientMethodNameFilter();
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(methodNameFilter, MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
                try {
                    method.addScopedInterceptor(com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterMethodInterceptor.class, NbaseArcConstants.NBASE_ARC_SCOPE);
                } catch (Exception e) {
                    PLogger logger = PLoggerFactory.getLogger(this.getClass());
                    if (logger.isInfoEnabled()) {
                        logger.info("Unsupported method {}", method, e);
                    }
                }
            }

            return target.toBytecode();
        }

        protected void handle(InstrumentClass target) throws InstrumentException {

        }
    }

    public static class BinaryTriplesRedisClusterTransform extends RedisClusterTransform {
        @Override
        protected void handle(InstrumentClass target) throws InstrumentException {
            target.addField(com.navercorp.pinpoint.plugin.nbasearc.DestinationIdAccessor.class);
            target.addField(com.navercorp.pinpoint.plugin.nbasearc.EndPointAccessor.class);
        }
    }

    private void addRedisClusterPipeline() {
        transformTemplate.transform("com.navercorp.redis.cluster.pipeline.RedisClusterPipeline", RedisClusterPipelineTransform.class);
    }

    public static class RedisClusterPipelineTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(com.navercorp.pinpoint.plugin.nbasearc.DestinationIdAccessor.class);
            target.addField(com.navercorp.pinpoint.plugin.nbasearc.EndPointAccessor.class);

            if (target.hasConstructor("com.navercorp.redis.cluster.gateway.GatewayServer")) {
                final InstrumentMethod constructor = target.getConstructor("com.navercorp.redis.cluster.gateway.GatewayServer");
                constructor.addInterceptor(com.navercorp.pinpoint.plugin.nbasearc.open.interceptor.RedisClusterPipelineConstructorInterceptor.class);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("setServer"))) {
                method.addInterceptor(com.navercorp.pinpoint.plugin.nbasearc.open.interceptor.RedisClusterPipelineSetServerMethodInterceptor.class);
            }

            final NbaseArcPluginConfig config = new NbaseArcPluginConfig(instrumentor.getProfilerConfig());
            final GatewayClientMethodNameFilter methodNameFilter = new GatewayClientMethodNameFilter();
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(methodNameFilter, MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
                try {
                    method.addScopedInterceptor(com.navercorp.pinpoint.plugin.nbasearc.interceptor.RedisClusterPipelineMethodInterceptor.class, va(config.isIo()), NbaseArcConstants.NBASE_ARC_SCOPE);
                } catch (Exception e) {
                    PLogger logger = PLoggerFactory.getLogger(this.getClass());
                    if (logger.isInfoEnabled()) {
                        logger.info("Unsupported method {}", method, e);
                    }
                }
            }

            return target.toBytecode();
        }
    }


    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}