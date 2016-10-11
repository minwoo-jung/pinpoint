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
package com.navercorp.pinpoint.plugin.nelo;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
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
 * check up on sending nelo server a log. 
 *  
 * we don't consider that log was not send nelo server 
 * because buffer of nelo appender is fulled or happen connection exception occured while communication with nelo server. 
 * It need to many modified class, interceptor class to judge above sitiation. 
 * 
 * We don't consider threshold config of NeloAppender for log level when using NeloAsyncAppender
 * Pinpoint could not intercept NeloAppender class Because if NeloAsyncAppender is used NeloAppender is executed in separate thread.
 * 
 * @author minwoo.jung
 */
public class NeloPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final ProfilerConfig config = context.getConfig();
        NeloPluginConfig neloPluginConfig = new NeloPluginConfig(config);
        if (logger.isInfoEnabled()) {
            logger.info("NeloPlugin config:{}", neloPluginConfig);
        }

        if (neloPluginConfig.isLog4jLoggingTransactionInfo()) {
            addLog4jNelo2AsyncAppenderEditor();
            addLog4jNeloAppenderEditor();
        }
        
        if (neloPluginConfig.isLogbackLoggingTransactionInfo()) {
            addLogBackNelo2AsyncAppenderEditor();
            addLogBackNeloAppenderEditor();
        }
    }
    
    private void addLogBackNeloAppenderEditor() {
        transformTemplate.transform("com.nhncorp.nelo2.logback.NeloLogbackAppender", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                InstrumentMethod append = target.getDeclaredMethod("append", "ch.qos.logback.classic.spi.ILoggingEvent");
                append.addInterceptor("com.navercorp.pinpoint.plugin.nelo.interceptor.AppenderInterceptor");

                return target.toBytecode();
            }
        });
    }

    private void addLogBackNelo2AsyncAppenderEditor() {
        transformTemplate.transform("com.nhncorp.nelo2.logback.LogbackAsyncAppender", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                InstrumentMethod append = target.getDeclaredMethod("append", "java.lang.Object");
                append.addInterceptor("com.navercorp.pinpoint.plugin.nelo.interceptor.AppenderInterceptor");

                return target.toBytecode();
            }
        });
    }

    private void addLog4jNelo2AsyncAppenderEditor() {
        transformTemplate.transform("com.nhncorp.nelo2.log4j.Nelo2AsyncAppender", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                InstrumentMethod append = target.getDeclaredMethod("append", "org.apache.log4j.spi.LoggingEvent");

                if (append == null) {
                    append = target.addDelegatorMethod("append", "org.apache.log4j.spi.LoggingEvent");
                }

                append.addInterceptor("com.navercorp.pinpoint.plugin.nelo.interceptor.AppenderInterceptor");

                return target.toBytecode();
            }
        });
    }

    private void addLog4jNeloAppenderEditor() {
        transformTemplate.transform("com.nhncorp.nelo2.log4j.NeloAppender", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                InstrumentMethod append = target.getDeclaredMethod("append", "org.apache.log4j.spi.LoggingEvent");
                append.addInterceptor("com.navercorp.pinpoint.plugin.nelo.interceptor.AppenderInterceptor");

                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
