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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.annotation.PreDestroy;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author netspider
 */
@Component
@Deprecated
public class ApacheClosableAsyncHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(ApacheClosableAsyncHttpClient.class);

    private final CloseableHttpAsyncClient httpClient;

    public ApacheClosableAsyncHttpClient() {
        this.httpClient = HttpAsyncClients.custom().useSystemProperties().build();
        this.httpClient.start();
    }

    @PreDestroy
    public void close() {
        try {
            this.httpClient.close();
        } catch (IOException e) {
            logger.warn("CloseableHttpAsyncClient close error Caused:" + e.getCause(), e);
        }
    }

    public String post() {
        try {
            HttpPost httpRequest = new HttpPost("http://dev.pinpoint.navercorp.com/");

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("param1", "value1"));
            httpRequest.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8.name()));

            Future<HttpResponse> responseFuture = this.httpClient.execute(httpRequest, null);
            HttpResponse response = (HttpResponse) responseFuture.get();

            String result = null;
            if ((response != null) && (response.getEntity() != null)) {
                try {
                    InputStream is = response.getEntity().getContent();
                    result = IOUtils.toString(is);
                } catch (IOException ignored) {
                }
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}