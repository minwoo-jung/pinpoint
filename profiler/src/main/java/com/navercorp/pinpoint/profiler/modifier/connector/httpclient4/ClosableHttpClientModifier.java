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

package com.navercorp.pinpoint.profiler.modifier.connector.httpclient4;

import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.LifeCycleEventListener;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor.HttpClient4Scope;
import com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor.HttpClient4Scope2;
import com.navercorp.pinpoint.profiler.modifier.db.mysql.MYSQLScope;
import com.navercorp.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Apache httpclient 4.3 CloseableHttpClient modifier
 * 
 * <p/>
 * <p/>
 * <pre>
 * http://grepcode.com/file/repo1.maven.org/maven2/org.apache.httpcomponents/httpclient/4.3/org/apache/http/impl/client/CloseableHttpClient.java?av=h#CloseableHttpClient
 * </pre>
 *
 * @author netspider
 */
public class ClosableHttpClientModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ClosableHttpClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public String getTargetClass() {
        return "org/apache/http/impl/client/CloseableHttpClient";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

            addHttpRequestApi(classLoader, protectedDomain, aClass);

            addHttpUriRequestApi(classLoader, protectedDomain, aClass);

            return aClass.toBytecode();
        } catch (Throwable e) {
            logger.warn("httpClient4 modifier error. Caused:{}", e.getMessage(), e);
            return null;
        }
    }

    private void addHttpRequestApi(ClassLoader classLoader, ProtectionDomain protectedDomain, InstrumentClass aClass) throws InstrumentException {
//        Interceptor httpRequestApi1= newHttpRequestInterceptor(classLoader, protectedDomain);
//        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest"}, httpRequestApi1, HttpClient4Scope.SCOPE);
//
//        Interceptor httpRequestApi2 = newHttpRequestInterceptor(classLoader, protectedDomain);
//        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext"}, httpRequestApi2, HttpClient4Scope.SCOPE);
//
//        Interceptor httpRequestApi3 = newHttpRequestInterceptor(classLoader, protectedDomain);
//        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler"}, httpRequestApi3, HttpClient4Scope2.SCOPE);
//
//        Interceptor httpRequestApi4 = newHttpRequestInterceptor(classLoader, protectedDomain);
//        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext"}, httpRequestApi4, HttpClient4Scope2.SCOPE);

//        scope 2개 만들어서 테스트        
//        Interceptor httpRequestApi1= newHttpRequestInterceptor(classLoader, protectedDomain);
//        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest"}, httpRequestApi1, HttpClient4Scope.SCOPE);
//
//        Interceptor httpRequestApi2 = newHttpRequestInterceptor(classLoader, protectedDomain);
//        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext"}, httpRequestApi2, HttpClient4Scope.SCOPE);
//
//        Interceptor httpRequestApi3 = new MethodInterceptor();
//        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler"}, httpRequestApi3, HttpClient4Scope2.SCOPE);
//
//        Interceptor httpRequestApi4 = new MethodInterceptor();
//        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext"}, httpRequestApi4, HttpClient4Scope2.SCOPE);
        
        
        //depth 조작 
        ThreadLocal<Map<Object, Object>> tmpData = new ThreadLocal<Map<Object, Object>>() {
            @Override
            protected Map<Object, Object> initialValue() {
                return new HashMap<Object, Object>();
            }
        };
        
//        depth 조작        
        Interceptor httpRequestApi1= newHttpRequestInterceptor(classLoader, protectedDomain, ServiceType.HTTP_CLIENT, false, tmpData);
        aClass.addInterceptor("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest"}, httpRequestApi1);
  
        Interceptor httpRequestApi2 = newHttpRequestInterceptor(classLoader, protectedDomain, ServiceType.HTTP_CLIENT, false, tmpData);
        aClass.addInterceptor("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext"}, httpRequestApi2);
  
        Interceptor httpRequestApi3 = newHttpRequestInterceptor(classLoader, protectedDomain, ServiceType.HTTP_CLIENT_CALL_BACK, true, tmpData);
        aClass.addInterceptor("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler"}, httpRequestApi3);
  
        Interceptor httpRequestApi4 = newHttpRequestInterceptor(classLoader, protectedDomain, ServiceType.HTTP_CLIENT_CALL_BACK, true, tmpData);
        aClass.addInterceptor("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext"}, httpRequestApi4);
        
        
//        Interceptor httpRequestApi1= newHttpRequestInterceptor(classLoader, protectedDomain, ServiceType.HTTP_CLIENT, false, tmpData);
//        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest"}, httpRequestApi1, HttpClient4Scope.SCOPE, true);
//  
//        Interceptor httpRequestApi2 = newHttpRequestInterceptor(classLoader, protectedDomain, ServiceType.HTTP_CLIENT, false, tmpData);
//        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.protocol.HttpContext"}, httpRequestApi2, HttpClient4Scope.SCOPE, true);
//  
//        Interceptor httpRequestApi3 = newHttpRequestInterceptor(classLoader, protectedDomain, ServiceType.HTTP_CLIENT_CALL_BACK, true, tmpData);
//        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler"}, httpRequestApi3, HttpClient4Scope.SCOPE, true);
//  
//        Interceptor httpRequestApi4 = newHttpRequestInterceptor(classLoader, protectedDomain, ServiceType.HTTP_CLIENT_CALL_BACK, true, tmpData);
//        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.HttpHost", "org.apache.http.HttpRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext"}, httpRequestApi4, HttpClient4Scope.SCOPE, true);
    }

    private Interceptor newHttpRequestInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, ServiceType serviceType, boolean isHasCallbackParam, ThreadLocal<Map<Object, Object>> tmpData) throws InstrumentException {
//        return byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor.HttpRequestExecuteInterceptor");
       
        final Scope scope = byteCodeInstrumentor.getScope(HttpClient4Scope.SCOPE);
        return byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor.HttpRequestExecuteInterceptor",  new Object[] {serviceType, scope, isHasCallbackParam, tmpData }, new Class[] { ServiceType.class, Scope.class, boolean.class, ThreadLocal.class });
    }

    private void addHttpUriRequestApi(ClassLoader classLoader, ProtectionDomain protectedDomain, InstrumentClass aClass) throws InstrumentException {
//        Interceptor httpUriRequestInterceptor1 = newHttpUriRequestInterceptor(classLoader, protectedDomain);
//        aClass.addInterceptor("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest"}, httpUriRequestInterceptor1);
//
//        Interceptor httpUriRequestInterceptor2 = newHttpUriRequestInterceptor(classLoader, protectedDomain);
//        aClass.addInterceptor("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest", "org.apache.http.protocol.HttpContext"}, httpUriRequestInterceptor2);
//
//        Interceptor httpUriRequestInterceptor3 = newHttpUriRequestInterceptor(classLoader, protectedDomain);
//        aClass.addInterceptor("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler"}, httpUriRequestInterceptor3);
//
//        Interceptor httpUriRequestInterceptor4 = newHttpUriRequestInterceptor(classLoader, protectedDomain);
//        aClass.addInterceptor("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext"}, httpUriRequestInterceptor4);
        
//        scope 2개 만들어서 테스트
//        Interceptor httpUriRequestInterceptor1 = newHttpUriRequestInterceptor(classLoader, protectedDomain);
//        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest"}, httpUriRequestInterceptor1, HttpClient4Scope.SCOPE);
//
//        Interceptor httpUriRequestInterceptor2 = newHttpUriRequestInterceptor(classLoader, protectedDomain);
//        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest", "org.apache.http.protocol.HttpContext"}, httpUriRequestInterceptor2, HttpClient4Scope.SCOPE);
//
//        Interceptor httpUriRequestInterceptor3 = new MethodInterceptor();
//        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler"}, httpUriRequestInterceptor3, HttpClient4Scope2.SCOPE);
//
//        Interceptor httpUriRequestInterceptor4 = new MethodInterceptor();
//        aClass.addScopeInterceptorIfDeclared("execute", new String[]{"org.apache.http.client.methods.HttpUriRequest", "org.apache.http.client.ResponseHandler", "org.apache.http.protocol.HttpContext"}, httpUriRequestInterceptor4, HttpClient4Scope2.SCOPE);
        
    }

    private Interceptor newHttpUriRequestInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain) throws InstrumentException {
//        return byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor.HttpUriRequestExecuteInterceptor");
        
        final Scope scope = byteCodeInstrumentor.getScope(HttpClient4Scope.SCOPE);
        return byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor.HttpUriRequestExecuteInterceptor",  new Object[] { scope }, new Class[] { Scope.class });
    }
}