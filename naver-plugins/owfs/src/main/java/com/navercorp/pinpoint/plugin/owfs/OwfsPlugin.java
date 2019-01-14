/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.owfs;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.owfs.interceptor.OwfsFileConstructorInterceptor;
import com.navercorp.pinpoint.plugin.owfs.interceptor.OwfsFileInterceptor;
import com.navercorp.pinpoint.plugin.owfs.interceptor.OwfsInterceptor;
import com.navercorp.pinpoint.plugin.owfs.interceptor.OwfsOwnerConstructorInterceptor;

import java.security.ProtectionDomain;

/**
 * @author jaehong.kim
 */
public class OwfsPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final OwfsPluginConfig config = new OwfsPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            if (logger.isInfoEnabled()) {
                logger.info("Disable {}. version range=[3.6.0, 4.0], config={}", this.getClass().getSimpleName(), config);
            }
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Enable {}. version range=[3.6.0, 4.0], config={}", this.getClass().getSimpleName(), config);
        }
        owfs();
        owfsOwner();
        owfsFile();
    }

    private void owfs() {
        transformTemplate.transform("com.nhncorp.owfs.Owfs", OwfsTransform.class);
    }

    public static class OwfsTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            final InstrumentMethod initMethod = target.getDeclaredMethod("init", "java.net.InetAddress", "java.lang.String", "com.nhncorp.owfs.base.Configuration");
            if (initMethod != null) {
                initMethod.addInterceptor(OwfsInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void owfsOwner() {
        transformTemplate.transform("com.nhncorp.owfs.OwfsOwner", OwfsOwnerTransform.class);
    }

    public static class OwfsOwnerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DestinationIdAccessor.class);
            target.addField(EndPointAccessor.class);
            // set destinationId & endPoint
            final InstrumentMethod constructor = target.getConstructor("com.nhncorp.owfs.Owfs", "java.lang.String");
            if (constructor != null) {
                constructor.addInterceptor(OwfsOwnerConstructorInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void owfsFile() {
        transformTemplate.transform("com.nhncorp.owfs.OwfsFile", OwfsFileTransform.class);
    }

    public static class OwfsFileTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(DestinationIdAccessor.class);
            target.addField(EndPointAccessor.class);
            // set destinationId & endPoint
            final InstrumentMethod constructor = target.getConstructor("com.nhncorp.owfs.OwfsOwner", "java.lang.String", "java.lang.String", "boolean");
            if (constructor != null) {
                constructor.addInterceptor(OwfsFileConstructorInterceptor.class);
            }
            // read/write/append
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("read", "write", "append"))) {
                method.addScopedInterceptor(OwfsFileInterceptor.class, "OwfsFile");
            }
            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}