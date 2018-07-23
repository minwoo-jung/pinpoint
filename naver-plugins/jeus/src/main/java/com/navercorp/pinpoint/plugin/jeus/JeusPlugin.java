/**
 * Copyright 2018 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.jeus;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

/**
 * @author jaehong.kim
 */
public class JeusPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final JeusConfig config = new JeusConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("WebspherePlugin disabled");
            return;
        }
        logger.info("WebspherePlugin config:{}", config);

        context.addApplicationTypeDetector(new JeusDetector(config.getBootstrapMains()));

        // Hide pinpoint header & Add async listener. Servlet 3.0
        addHttpServletRequestImpl(config);
        // Entry Point
        addServletWrapper();
    }

    private void addHttpServletRequestImpl(final JeusConfig config) {
        transformTemplate.transform("jeus.servlet.engine.HttpServletRequestImpl", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                if (config.isHidePinpointHeader()) {
                    // Hide pinpoint headers
                    target.weave("com.navercorp.pinpoint.plugin.jeus.aspect.HttpServletRequestImplAspect");
                }

                // Add async listener. Servlet 3.0
                final InstrumentMethod startAsyncMethodEditor = target.getDeclaredMethod("startAsync", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
                if (startAsyncMethodEditor != null) {
                    startAsyncMethodEditor.addInterceptor("com.navercorp.pinpoint.plugin.jeus.interceptor.HttpServletRequestImplStartAsyncInterceptor");
                }
                return target.toBytecode();
            }
        });
    }

    private void addServletWrapper() {
        transformTemplate.transform("jeus.servlet.engine.ServletWrapper", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                // Entry Point
                final InstrumentMethod handleMethodEditorBuilder = target.getDeclaredMethod("execute", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
                if (handleMethodEditorBuilder != null) {
                    handleMethodEditorBuilder.addInterceptor("com.navercorp.pinpoint.plugin.jeus.interceptor.ServletWrapperExecuteInterceptor");
                }
                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}