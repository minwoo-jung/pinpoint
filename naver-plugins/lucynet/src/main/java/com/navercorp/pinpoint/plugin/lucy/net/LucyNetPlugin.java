/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.lucy.net;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.lucy.net.npc.KeepAliveVersion;
import com.navercorp.pinpoint.plugin.lucy.net.npc.NpcHessianConnectorVersion;

import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author Jongho Moon
 *
 */
public class LucyNetPlugin implements ProfilerPlugin, TransformTemplateAware {


    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        // lucy-net
        addCompositeInvocationFutureTransformer("com.nhncorp.lucy.net.invoker.CompositeInvocationFuture");
        addDefaultInvocationFutureTransformer("com.nhncorp.lucy.net.invoker.DefaultInvocationFuture");
        
        // nimm
        addNimmInvokerTransformer("com.nhncorp.lucy.nimm.connector.bloc.NimmInvoker");

        // npc
        addNpcTransformer("com.nhncorp.lucy.npc.connector.NpcHessianConnector");
        addNpcNioTransformer("com.nhncorp.lucy.npc.connector.NioNpcHessianConnector");
        addNpcLightWeightTransformer("com.nhncorp.lucy.npc.connector.LightWeightNpcHessianConnector");
        addNpcLegacyLightWeightTransformer("com.nhncorp.lucy.npc.connector.LightWeightConnector");
        addNpcLegacyLightWeightTransformer("com.nhncorp.lucy.npc.connector.LightweightConnector");
        addNpcKeepAlivePluginTransformer("com.nhncorp.lucy.npc.connector.KeepAliveNpcHessianConnector");
    }

    private void addCompositeInvocationFutureTransformer(String clazzName) {
        transformTemplate.transform(clazzName, CompositeInvocationFutureTransformer.class);
    }

    public static class CompositeInvocationFutureTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            InstrumentMethod method = target.getDeclaredMethod("getReturnValue");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.BASIC_INTERCEPTOR, ServiceType.INTERNAL_METHOD);

            return target.toBytecode();
        }

    }


    private void addDefaultInvocationFutureTransformer(String clazzName) {
        transformTemplate.transform(clazzName, DefaultInvocationFutureTransformer.class);
    }

    public static class DefaultInvocationFutureTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            // FIXME 이렇게 하면 api type이 internal method로 보이는데 사실 NPC_CLIENT, NIMM_CLIENT로 보여야함. servicetype으로 넣기에 애매해서. 어떻게 수정할 것인지는 나중에 고민.
            List<InstrumentMethod> methods = target.getDeclaredMethods(MethodFilters.name("getReturnValue", "get", "isReadyAndSet"));
            for (InstrumentMethod method : methods) {
                LucyNetUtils.addInterceptor(method, InterceptorConstants.NET_INVOCATION_FUTURE_INTERCEPTOR);
            }

            return target.toBytecode();
        }

    }

    private void addNimmInvokerTransformer(String clazzName) {
        transformTemplate.transform(clazzName, NimmInvokerTransformer.class);
    }

    public static class NimmInvokerTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(InterceptorConstants.NIMM_ADDRESS_ACCESSOR);
            target.addField(AsyncContextAccessor.class);

            InstrumentMethod constructor = target.getConstructor("com.nhncorp.lucy.nimm.connector.address.NimmAddress", "com.nhncorp.lucy.nimm.connector.NimmSocket", "long");
            LucyNetUtils.addInterceptor(constructor, InterceptorConstants.NIMM_CONSTRUCTOR_INTERCEPTOR);

            InstrumentMethod method = target.getDeclaredMethod("invoke", "long", "java.lang.String", "java.lang.String", "java.lang.Object[]");
            LucyNetUtils.addScopedInterceptor(method, InterceptorConstants.NIMM_INVOKE_INTERCEPTOR, LucyNetConstants.NIMM_INVOKER_METHOD_SCOPE, ExecutionPolicy.BOUNDARY);

            InstrumentMethod encodeMessageMethod = target.getDeclaredMethod("encodeMessage", "java.util.Map", "com.nhncorp.lucy.net.call.Call");
            LucyNetUtils.addScopedInterceptor(encodeMessageMethod, InterceptorConstants.NIMM_ENCODE_MESSAGE_INTERCEPTOR, LucyNetConstants.NIMM_INVOKER_METHOD_SCOPE, ExecutionPolicy.INTERNAL);

            return target.toBytecode();
        }

    }

    private void addNpcTransformer(String clazzName) {
        transformTemplate.transform(clazzName, NpcTransformer.class);
    }

    public static class NpcTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            for (NpcHessianConnectorVersion matchedVersion : NpcHessianConnectorVersion.values()) {
                if (matchedVersion.checkCondition(target)) {
                    return matchedVersion.transform(target);
                }
            }

            return target.toBytecode();
        }

    }


    private void addNpcNioTransformer(String clazzName) {
        transformTemplate.transform(clazzName, NpcNioTransformer.class);
    }

    public static class NpcNioTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(InterceptorConstants.METADATA_NPC_SERVER_ADDRESS);

            InstrumentMethod constructor = target.getConstructor("com.nhncorp.lucy.npc.connector.NpcConnectorOption");
            LucyNetUtils.addInterceptor(constructor, InterceptorConstants.NPC_CONSTRUCTOR_INTERCEPTOR);

            InstrumentMethod method = target.getDeclaredMethod("invoke", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.NPC_INVOKE_INTERCEPTOR);

            method = target.getDeclaredMethod("makeMessage", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.NET_MAKE_MESSAGE_INTERCEPTOR);

            return target.toBytecode();
        }

    }

    private void addNpcLightWeightTransformer(String clazzName) {
        transformTemplate.transform(clazzName, NpcLightWeightTransformer.class);
    }

    public static class NpcLightWeightTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(InterceptorConstants.METADATA_NPC_SERVER_ADDRESS);

            String superClazz = target.getSuperClass();
            if (superClazz != null && superClazz.equals("com.nhncorp.lucy.npc.connector.AbstractNpcHessianConnector")) {
                InstrumentMethod constructor = target.getConstructor("com.nhncorp.lucy.npc.connector.NpcConnectorOption");
                LucyNetUtils.addInterceptor(constructor, InterceptorConstants.NPC_CONSTRUCTOR_INTERCEPTOR);
            }

            InstrumentMethod method = target.getDeclaredMethod("invoke", "java.lang.String", "java.lang.String", "java.lang.Object[]");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.NPC_INVOKE_INTERCEPTOR);

            return target.toBytecode();
        }

    }

    private void addNpcLegacyLightWeightTransformer(String clazzName) {
        transformTemplate.transform(clazzName, NpcLegacyLightWeightTransformer.class);
    }

    public static class NpcLegacyLightWeightTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(InterceptorConstants.METADATA_NPC_SERVER_ADDRESS);

            InstrumentMethod constructor = target.getConstructor("com.nhncorp.lucy.npc.connector.NpcConnectorOption");
            LucyNetUtils.addInterceptor(constructor, InterceptorConstants.NPC_CONSTRUCTOR_INTERCEPTOR);

            InstrumentMethod method = target.getDeclaredMethod("invoke", "java.lang.String", "java.lang.String", "java.nio.charset.Charset", "java.lang.Object[]");
            LucyNetUtils.addInterceptor(method, InterceptorConstants.NPC_INVOKE_INTERCEPTOR);

            return target.toBytecode();
        }

    }

    private void addNpcKeepAlivePluginTransformer(String clazzName) {
        transformTemplate.transform(clazzName, NpcKeepAlivePluginTransformer.class);
    }

    public static class NpcKeepAlivePluginTransformer implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            for (KeepAliveVersion matchedVersion : KeepAliveVersion.values()) {
                if (matchedVersion.checkCondition(target)) {
                    return matchedVersion.transform(target);
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
