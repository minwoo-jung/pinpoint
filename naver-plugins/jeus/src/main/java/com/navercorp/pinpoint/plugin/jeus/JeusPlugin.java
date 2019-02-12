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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.jeus.interceptor.HttpServletRequestImplStartAsyncInterceptor;
import com.navercorp.pinpoint.plugin.jeus.interceptor.ServletWrapperExecuteInterceptor;

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
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            final JeusDetector jeusDetector = new JeusDetector(config.getBootstrapMains());
            if (jeusDetector.detect()) {
                logger.info("Detected application type : {}", JeusConstants.JEUS);
                if (!context.registerApplicationType(JeusConstants.JEUS)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), JeusConstants.JEUS);
                }
            }
        }

        logger.info("Adding Jeus transformers");
        // Hide pinpoint header & Add async listener. Servlet 3.0
        addHttpServletRequestImpl();
        // Entry Point
        addServletWrapper();
    }

    private void addHttpServletRequestImpl() {
        transformTemplate.transform("jeus.servlet.engine.HttpServletRequestImpl", HttpServletRequestImpl.class);
    }

    public static class HttpServletRequestImpl implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            final JeusConfig config = new JeusConfig(instrumentor.getProfilerConfig());
            if (config.isHidePinpointHeader()) {
                // Hide pinpoint headers
                target.weave("com.navercorp.pinpoint.plugin.jeus.aspect.HttpServletRequestImplAspect");
            }

            // Add async listener. Servlet 3.0
            final InstrumentMethod startAsyncMethodEditor = target.getDeclaredMethod("startAsync", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
            if (startAsyncMethodEditor != null) {
                startAsyncMethodEditor.addInterceptor(HttpServletRequestImplStartAsyncInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void addServletWrapper() {
        transformTemplate.transform("jeus.servlet.engine.ServletWrapper", ServletWrapperTransform.class);
    }

    public static class ServletWrapperTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // Entry Point
            final InstrumentMethod handleMethodEditorBuilder = target.getDeclaredMethod("execute", "javax.servlet.ServletRequest", "javax.servlet.ServletResponse");
            if (handleMethodEditorBuilder != null) {
                handleMethodEditorBuilder.addInterceptor(ServletWrapperExecuteInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}