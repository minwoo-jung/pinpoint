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

package com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor;

import java.util.Map;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.instrument.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.navercorp.pinpoint.bootstrap.pair.NameIntValuePair;
import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.profiler.util.DepthScope;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

/**
 * MethodInfo interceptor
 * <p/>
 * <pre>
 * org.apache.http.impl.client.AbstractHttpClient.
 * public <T> T execute(
 *            final HttpHost target,
 *            final HttpRequest request,
 *            final ResponseHandler<? extends T> responseHandler,
 *            final HttpContext context)
 *            throws IOException, ClientProtocolException {
 * </pre>
 * @author emeroad
 */
public class HttpRequestExecuteInterceptor extends AbstractHttpRequestExecute implements TargetClassLoader {

    private static final int HTTP_HOST_INDEX = 0;
    private static final int HTTP_REQUEST_INDEX = 1;
    
    private Scope scope;
    private boolean isHasCallbackParam;
    private ThreadLocal<Map<Object, Object>> tmpData;

//    public HttpRequestExecuteInterceptor() {
//        super(HttpRequestExecuteInterceptor.class);
//    }    
    
    public HttpRequestExecuteInterceptor(ServiceType serviceType, Scope scope, boolean isHasCallbackParam, ThreadLocal<Map<Object, Object>> tmpData) {
        super(HttpRequestExecuteInterceptor.class);
        this.serviceType = serviceType;
        this.scope = scope;
        this.isHasCallbackParam = isHasCallbackParam;
        this.tmpData = tmpData;
    }
    
    @Override
    public void before(Object target, Object[] args) {
        if (!isPassibleBeforeProcess()) {
            return;
        }
        
        super.before(target, args);
    }
    
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (!isPassibleAfterProcess()) {
            addStatusCode(result);
            return;
        }
        
        super.after(target, args, result, throwable);
    };
    
    private boolean isPassibleBeforeProcess() {
        if (scope.depth() == DepthScope.ZERO) {
            tmpData.get().clear();
        }

        tmpData.get().put(scope.depth(), this.serviceType);
        final int push = scope.push();
        
        if (push == DepthScope.ZERO) {
            return true;
        }
        
        return false;
    }
    
    private boolean isPassibleAfterProcess() {
        final int push = scope.pop();
        
        if (push == DepthScope.ZERO) {
            return true;
        }
        
        return false;
    }

    @Override
    protected NameIntValuePair<String> getHost(Object[] args) {
        final Object arg = args[HTTP_HOST_INDEX];
        if (arg instanceof HttpHost) {
            final HttpHost httpHost = (HttpHost) arg;
            return new NameIntValuePair<String>(httpHost.getHostName(), httpHost.getPort());
        }
        return null;
    }

    @Override
    protected HttpRequest getHttpRequest(Object[] args) {
        final Object arg = args[HTTP_REQUEST_INDEX];
        if (arg instanceof HttpRequest) {
            return (HttpRequest) arg;
        }
        return null;
    }

    @Override
    Integer getStatusCode(Object[] args, Object result) {
        if (result instanceof HttpResponse) {
            HttpResponse response = (HttpResponse)result;
            
            if (response.getStatusLine() != null) {
                return response.getStatusLine().getStatusCode(); 
            }
        }
        
        return null;
    }

    void addStatusCode(Object result) {
        if(!needGetStatusCode()) {
            return;
        }
        
        //!!!!!!!!!!!! 이건 getstatuscode 함수를 재용ㅇ하면 될듯.
        if (result instanceof HttpResponse) {
            HttpResponse response = (HttpResponse)result;
            
            if (response.getStatusLine() != null) {
                System.out.println("tmpdata!! : " + tmpData);
                traceContext.currentRawTraceObject().recordAttribute(AnnotationKey.HTTP_STATUS_CODE, response.getStatusLine().getStatusCode());
            }
        }
    }
    
    private boolean needGetStatusCode() {
        if (isHasCallbackParam) {
            return false;
        }
        
        final Trace trace = traceContext.currentRawTraceObject();
        
        if (trace == null || trace.getServiceType() != ServiceType.HTTP_CLIENT_CALL_BACK) {
            return false;
        }
        
        Object value = tmpData.get().get(scope.depth()-1);
        
        if (value != null && value instanceof ServiceType) {
            if (ServiceType.HTTP_CLIENT_CALL_BACK != (ServiceType)value) {
                return false;
            }
        }

        return true;
    }
}