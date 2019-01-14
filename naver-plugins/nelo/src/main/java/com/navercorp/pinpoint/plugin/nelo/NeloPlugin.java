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

import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
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
import com.navercorp.pinpoint.plugin.nelo.interceptor.AddNeloAppenderMethodInterceptor;
import com.navercorp.pinpoint.plugin.nelo.interceptor.AppenderInterceptor;
import com.navercorp.pinpoint.plugin.nelo.interceptor.AsyncAppenderInterceptor;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * check up on sending nelo server a log.
 * <p>
 * we don't consider that log was not send nelo server
 * because buffer of nelo appender is fulled or happen connection exception occured while communication with nelo server.
 * It need to many modified class, interceptor class to judge above sitiation.
 * <p>
 * We don't consider threshold config of NeloAppender for log level when using NeloAsyncAppender
 * Pinpoint could not intercept NeloAppender class Because if NeloAsyncAppender is used NeloAppender is executed in separate thread.
 *
 * @author minwoo.jung
 * @author jaehong.kim
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
            // 1.3.3 ~ 1.4.x
            addLog4jNeloAppenderEditor();
            // 1.5.x
            addLog4jNeloAppenderBaseEditor();
            // ~ 1.5.x
            addLog4jNeloAsyncAppenderEditor();
            //1.6.x ~
            addLog4jNeloAppenderBase2Editor();
        }

        if (neloPluginConfig.isLogbackLoggingTransactionInfo()) {
            // ~ 1.5.5
            addLogBackNeloAsyncAppenderEditor();
            addLogBackNeloAppenderEditor();
            //1.6.x ~
            addLogBackNeloAppender2Editor();
        }
    }

    private void addLogBackNeloAppender2Editor() {
        transformTemplate.transform("com.naver.nelo2.logback.AppenderBase", LogBackAppenderBaseTransform.class);

        transformTemplate.transform("ch.qos.logback.core.AsyncAppenderBase", LogbackAsyncAppenderBaseTransform.class);
    }

    public static class LogBackAppenderBaseTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final InstrumentMethod append = target.getDeclaredMethod("append", "ch.qos.logback.classic.spi.ILoggingEvent");
            if (append != null) {
                append.addInterceptor(AppenderInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class LogbackAsyncAppenderBaseTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain
                protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(UsingNeloAppenderAccessor.class);

            final InstrumentMethod addAppenderMethod = target.getDeclaredMethod("addAppender", "ch.qos.logback.core.Appender");
            if (addAppenderMethod != null) {
                List<String> neloAppenderClassNameList = Arrays.asList(
                        "com.naver.nelo2.logback.HttpAppender",
                        "com.naver.nelo2.logback.ThriftAppender"
                );
                addAppenderMethod.addInterceptor(AddNeloAppenderMethodInterceptor.class, va(neloAppenderClassNameList));
            }

            final InstrumentMethod appendMethod = target.getDeclaredMethod("append", "java.lang.Object");
            if (appendMethod != null) {
                appendMethod.addInterceptor(AsyncAppenderInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addLogBackNeloAppenderEditor() {
        transformTemplate.transform("com.nhncorp.nelo2.logback.NeloLogbackAppender", NeloLogbackAppenderTransform.class);
    }

    public static class NeloLogbackAppenderTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final InstrumentMethod append = target.getDeclaredMethod("append", "ch.qos.logback.classic.spi.ILoggingEvent");
            if (append != null) {
                append.addInterceptor(AppenderInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addLogBackNeloAsyncAppenderEditor() {
        transformTemplate.transform("com.nhncorp.nelo2.logback.LogbackAsyncAppender", LogbackAsyncAppenderTransform.class);
    }

    public static class LogbackAsyncAppenderTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final InstrumentMethod append = target.getDeclaredMethod("append", "java.lang.Object");
            if (append != null) {
                append.addInterceptor(AppenderInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addLog4jNeloAsyncAppenderEditor() {
        transformTemplate.transform("com.nhncorp.nelo2.log4j.Nelo2AsyncAppender", Nelo2AsyncAppenderTransform.class);
    }

    public static class Nelo2AsyncAppenderTransform implements TransformCallback {
        private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod append = target.getDeclaredMethod("append", "org.apache.log4j.spi.LoggingEvent");
            if (append == null) {
                append = target.addDelegatorMethod("append", "org.apache.log4j.spi.LoggingEvent");
                if (logger.isInfoEnabled()) {
                    logger.info("Add delegator method=com.nhncorp.nelo2.log4j.Nelo2AsyncAppender.append");
                }
            }

            if (append != null) {
                append.addInterceptor(AppenderInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addLog4jNeloAppenderEditor() {
        transformTemplate.transform("com.nhncorp.nelo2.log4j.NeloAppender", NeloAppenderTransform.class);
    }

    public static class NeloAppenderTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final InstrumentMethod append = target.getDeclaredMethod("append", "org.apache.log4j.spi.LoggingEvent");
            if (append != null) {
                // ~ 1.4.x
                append.addInterceptor(AppenderInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addLog4jNeloAppenderBaseEditor() {
        transformTemplate.transform("com.nhncorp.nelo2.log4j.NeloAppenderBase", NeloAppenderBaseTransform.class);
    }

    public static class NeloAppenderBaseTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.name("append"), MethodFilters.args("org.apache.log4j.spi.LoggingEvent"), MethodFilters.modifier(Modifier.PROTECTED, Modifier.ABSTRACT)))) {
                if (method != null) {
                    method.addInterceptor(AppenderInterceptor.class);
                }
            }

            return target.toBytecode();
        }
    }

    private void addLog4jNeloAppenderBase2Editor() {
        transformTemplate.transform("com.naver.nelo2.log4j.AppenderBase", Log4jAppenderBaseTransform.class);

        transformTemplate.transform("org.apache.log4j.AsyncAppender", Log4jAsyncAppenderTransform.class);
    }

    public static class Log4jAppenderBaseTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.name("append"), MethodFilters.args("org.apache.log4j.spi.LoggingEvent"), MethodFilters.modifier(Modifier.PROTECTED, Modifier.ABSTRACT)))) {
                if (method != null) {
                    method.addInterceptor(AppenderInterceptor.class);
                }
            }

            return target.toBytecode();
        }
    }

    public static class Log4jAsyncAppenderTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(UsingNeloAppenderAccessor.class);

            final InstrumentMethod addAppenderMethod = target.getDeclaredMethod("addAppender", "org.apache.log4j.Appender");
            if (addAppenderMethod != null) {
                List<String> neloAppenderClassNameList = Arrays.asList(
                        "com.naver.nelo2.log4j.HttpAppender",
                        "com.naver.nelo2.log4j.ThriftAppender"
                );
                addAppenderMethod.addInterceptor(AddNeloAppenderMethodInterceptor.class, va(neloAppenderClassNameList));
            }

            final InstrumentMethod appendMethod = target.getDeclaredMethod("append", "org.apache.log4j.spi.LoggingEvent");
            if (appendMethod != null) {
                appendMethod.addInterceptor(AsyncAppenderInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
