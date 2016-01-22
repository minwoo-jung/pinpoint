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
package com.navercorp.pinpoint.plugin.google.httpclient;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.async.AsyncTraceIdAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.ClassFilters;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger_제거예정;
import com.navercorp.pinpoint.bootstrap.logging.SLF4jLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

/**
 * @author jaehong.kim
 *
 */
public class HttpClientPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger_제거예정 logger = SLF4jLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final HttpClientPluginConfig config = new HttpClientPluginConfig(context.getConfig());
        logger.debug("[GoogleHttpClient] Initialized config={}", config);

        logger.debug("[GoogleHttpClient] Add HttpRequest class.");
        addHttpRequestClass(config);
    }

    private void addHttpRequestClass(final HttpClientPluginConfig config) {
        transformTemplate.transform("com.google.api.client.http.HttpRequest", new TransformCallback() {
            
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                
                InstrumentMethod execute = target.getDeclaredMethod("execute");
                if (execute != null) {
                    execute.addInterceptor("com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteMethodInterceptor");
                }
                
                if (config.isAsync()) {
                    InstrumentMethod executeAsync = target.getDeclaredMethod("executeAsync", "java.util.concurrent.Executor");
                    if (executeAsync != null) {
                        executeAsync.addInterceptor("com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteAsyncMethodInterceptor");
                    }

                    for (InstrumentClass nestedClass : target.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("executeAsync", "java.util.concurrent.Executor"), ClassFilters.interfaze("java.util.concurrent.Callable")))) {
                        logger.debug("Find nested class {}", target.getName());
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                target.addField(AsyncTraceIdAccessor.class.getName());

                                InstrumentMethod constructor = target.getConstructor("com.google.api.client.http.HttpRequest");
                                if (constructor != null) {
                                    logger.debug("Add constuctor interceptor for nested class {}", target.getName());
                                    constructor.addInterceptor("com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteAsyncMethodInnerClassConstructorInterceptor");
                                }

                                InstrumentMethod m = target.getDeclaredMethod("call");
                                if (m != null) {
                                    logger.debug("Add method interceptor for nested class {}.{}", target.getName(), m.getName());
                                    m.addInterceptor("com.navercorp.pinpoint.plugin.google.httpclient.interceptor.HttpRequestExecuteAsyncMethodInnerClassCallMethodInterceptor");
                                }

                                return target.toBytecode();
                            }
                        });
                    }
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