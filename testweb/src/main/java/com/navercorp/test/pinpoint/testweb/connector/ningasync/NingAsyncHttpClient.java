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

package com.navercorp.test.pinpoint.testweb.connector.ningasync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.ning.http.client.multipart.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * 
 * @author netspider
 * 
 */
@Component
public class NingAsyncHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(NingAsyncHttpClient.class);

    private final AsyncHttpClient asyncHttpClient;
    private String defaultUserAgent;

    public NingAsyncHttpClient() {
        asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setAllowPoolingConnections(true).setCompressionEnforced(true).build());
        defaultUserAgent = "pinpoint/test";
        logger.debug("init HttpClient : defaultAgent={}", defaultUserAgent);
    }

    @PreDestroy
    public void close() {
        asyncHttpClient.close();
    }

    public Response requestPost(String url, Map<String, String> headers, String body) {
        if (url == null) {
            return null;
        }
        BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost(url);

        try {
            if (headers != null) {
                for (Entry<String, String> entry : headers.entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }

            requestBuilder.setBody(body).setBodyEncoding("UTF-8");

            Future<Response> f = requestBuilder.execute();
            Response response = f.get(500L, TimeUnit.MILLISECONDS);

            logger.debug("\n\t [POST] url \t: " + url + "\n\t headers \t: " + headers + "\n\t body \t\t: " + body + "\n\t reponse \t: " + response.toString());
            return response;
        } catch (Exception e) {
            logger.debug("request read-timeout : url \t: " + url + "\n\t headers \t: " + headers + "\n\t body \t: " + body);
            throw new RuntimeException(e);
        }
    }

    public Response requestMultipart(String url, Map<String, String> headers, List<Part> parts) {
        if (url == null) {
            return null;
        }
        BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost(url);

        try {
            if (headers != null) {
                for (Entry<String, String> entry : headers.entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }

            if (parts != null) {
                for (Part part : parts) {
                    requestBuilder.addBodyPart(part);
                }
            }

            Future<Response> f = requestBuilder.execute();
            Response response = f.get(500L, TimeUnit.MILLISECONDS);

            logger.debug("\n\t [POST] url \t: " + url + "\n\t headers \t: " + headers + "\n\t parts \t\t: " + parts + "\n\t reponse \t: " + response.toString());
            return response;
        } catch (Exception e) {
            logger.debug("request read-timeout : url \t: " + url + "\n\t headers \t: " + headers + "\n\t parts \t: " + parts);
            throw new RuntimeException(e);
        }
    }

    public Response requestGet(String url, Map<String, String> queries, Map<String, String> headers, List<Cookie> cookies) {
        if (url == null) {
            return null;
        }

        BoundRequestBuilder requestBuilder = asyncHttpClient.prepareGet(url);

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                requestBuilder.addCookie(cookie);
            }
        }

        if (queries != null) {
            for (Entry<String, String> entry : queries.entrySet()) {
                requestBuilder.addQueryParam(entry.getKey(), entry.getValue());
            }
        }

        if (headers != null) {
            for (Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        try {
            Future<Response> f = requestBuilder.execute();
            Response response = f.get(60000L, TimeUnit.MILLISECONDS);

            logger.debug("\n\t [GET] url \t: " + url + "\n\t headers \t: " + headers + "\n\t queries \t: " + queries + "\n\t reponse \t: " + response.toString());

            return response;
        } catch (Exception e) {
            logger.debug("request read-timeout : url \t: " + url + "\n\t headers \t: " + headers + "\n\t queries \t: " + queries);
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> getDummyParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("query", "naver");
        params.put("ie", "utf8");
        return params;
    }

    public static Map<String, String> getDummyHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("header1", "header1");
        headers.put("header2", "header2");
        return headers;
    }

    public static List<Cookie> getDummyCookies() {
        List<Cookie> cookies = new ArrayList<Cookie>();
        cookies.add(new Cookie("cookieName1", "cookieValue1", false, "", "/", 10, false, false));
        cookies.add(new Cookie("cookieName2", "cookieValue2", false, "", "/", 10, false, false));
        return cookies;
    }
}
