/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.test.pinpoint.testweb.connector.apachehttp4;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 
 * @author netspider
 * 
 */
@Component
public class ApacheHttpClient4 {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public final static int SLOW_REQUEST_TIME = 1000;

    private HttpConnectorOptions connectorOptions;

    public ApacheHttpClient4() {
        this(new HttpConnectorOptions());
    }

    public ApacheHttpClient4(HttpConnectorOptions connectorOptions) {
        this.connectorOptions = connectorOptions;
    }

    private HttpClient getHttpClient(HttpParams params) {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        if (connectorOptions != null && connectorOptions.getPort() > 0) {
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), connectorOptions.getPort()));
        }
        schemeRegistry.register(new Scheme("https", PlainSocketFactory.getSocketFactory(), 443));

        SingleClientConnManager cm = new SingleClientConnManager(getHttpParams(), schemeRegistry);
        DefaultHttpClient httpClient = new DefaultHttpClient(cm, getHttpParams());
        httpClient.setParams(params);
        return httpClient;
    }

    public String execute(String uri, Map<String, Object> paramMap, String cookie) {
        if (null == uri) {
            return null;
        }

        HttpClient httpClient = null;
        try {
            HttpPost post = new HttpPost(uri);
            if (cookie != null) {
                post.setHeader("Cookie", cookie);
            }
            post.setEntity(getEntity(paramMap));
            post.setParams(getHttpParams());
            post.addHeader("Content-Type", "application/json;charset=UTF-8");

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            httpClient = getHttpClient(getHttpParams());

            return httpClient.execute(post, responseHandler);
        } catch (Exception e) {
            logger.warn("HttpClient.execute() error. Caused:{}", e.getMessage(), e);
            return e.getMessage();
        } finally {
            if (null != httpClient && null != httpClient.getConnectionManager()) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    public String execute2(String uri, Map<String, Object> paramMap) {
        return execute2(uri, paramMap, null);
    }
    
    public String execute2(String uri, Map<String, Object> paramMap, String cookie) {
        if (null == uri) {
            return null;
        }

        HttpClient httpClient = null;
        httpClient = getHttpClient(getHttpParams());
        try {
            HttpPost post = new HttpPost(uri);
            
            if (cookie != null) {
                post.setHeader("Cookie", cookie);
            }
            post.setEntity(getEntity(paramMap));
            post.setParams(getHttpParams());
            post.addHeader("Content-Type", "application/json;charset=UTF-8");

            //case 1-1
            httpClient = getHttpClient(getHttpParams());
            HttpResponse response = httpClient.execute(post);
            response.getStatusLine().getStatusCode();
            
            //case 1-2
            //call back 함수
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            httpClient = getHttpClient(getHttpParams());
            httpClient.execute(post, responseHandler);
            
            //case 2-1
            httpClient = getHttpClient(getHttpParams());
            response = httpClient.execute(post, new BasicHttpContext());
            System.out.println("status code : " + response.getStatusLine().getStatusCode());
            
            //case 2-2
            //call back 함수
            httpClient = getHttpClient(getHttpParams());
            httpClient.execute(post, responseHandler, new BasicHttpContext());

            //case 3-1
            httpClient = getHttpClient(getHttpParams());
            HttpHost target = new HttpHost("cafe.naver.com", 80, "http");
            HttpGet request = new HttpGet("/toto5kin/36557");
            response = httpClient.execute(target, request);
            response.getStatusLine().getStatusCode();
            
            //case 3-2
            httpClient = getHttpClient(getHttpParams());
            target = new HttpHost("cafe.naver.com", 80, "http");
            request = new HttpGet("/toto5kin/36557");
            httpClient.execute(target, request, responseHandler);
            
            //case 4-1
            httpClient = getHttpClient(getHttpParams());
            response = httpClient.execute(target, request, new BasicHttpContext());
            response.getStatusLine().getStatusCode();
            
            //case 4-2
            //call back 함수
            httpClient = getHttpClient(getHttpParams());
            httpClient.execute(target, request, responseHandler, new BasicHttpContext());
            
            return "OK";
        } catch (Exception e) {
            logger.warn("HttpClient.execute() error. Caused:{}", e.getMessage(), e);
            return e.getMessage();
        } finally {
            if (null != httpClient && null != httpClient.getConnectionManager()) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    public String execute(String uri, Map<String, Object> paramMap) {
        return execute(uri, paramMap, null);
    }

    public int executeToBlocWithReturnInt(String uri, Map<String, Object> paramMap) {
        if (null == uri) {
            return 0;
        }

        String responseBody = null;
        HttpClient httpClient = null;
        try {
            HttpPost post = new HttpPost(uri);
            post.setEntity(getEntity(paramMap));
            post.setParams(getHttpParams());
            post.addHeader("Content-Type", "application/json;charset=UTF-8");

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            httpClient = getHttpClient(getHttpParams());

            responseBody = httpClient.execute(post, responseHandler);

            return Integer.parseInt(responseBody);
        } catch (Exception e) {
            logger.warn("HttpClient.execute() error. Caused:{}", e.getMessage(), e);
            return 0;
        } finally {
            if (null != httpClient && null != httpClient.getConnectionManager()) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }

    private HttpEntity getEntity(Map<String, Object> paramMap) throws UnsupportedEncodingException {
        if (paramMap.size() != 0) {
            // size가 0일때 호출하면 entity에 {}가 들어감.
            return new StringEntity(paramMap.toString(), "UTF-8");
        } else {
            return new StringEntity("", "UTF-8");
        }
    }

    private HttpParams getHttpParams() {
        HttpParams params = new BasicHttpParams();
        if(connectorOptions != null) {
            HttpConnectionParams.setConnectionTimeout(params, (int) connectorOptions.getConnectionTimeout());
            HttpConnectionParams.setSoTimeout(params, connectorOptions.getSoTimeout());
        }
        params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
        params.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
        return params;
    }

    public void fileUpload(String uri) {
        HttpPost post = new HttpPost(uri + "/fileUpload/uploadFile.pinpoint");
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("log4j.xml").getFile());
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addBinaryBody("upfile", file, ContentType.DEFAULT_BINARY, "log4j.xml");
        // 
        HttpEntity entity = builder.build();
        post.setEntity(entity);
        
        HttpClient httpClient = getHttpClient(getHttpParams());
        try {
            HttpResponse response = httpClient.execute(post);
        } catch (Exception e) {
            logger.error("HttpClient.execute() error. Caused:{}", e.getMessage(), e);
        }
    }
}
