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

package com.navercorp.test.pinpoint.testweb.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("httpAsyncClientService")
public class HttpAsyncClientService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void post(final String uri) {
        post(uri, null);
    }

    public void post(final String uri, final Runnable callback) {
        post(uri, callback, false);
    }

    public void post(final String uri, final Runnable callback, final boolean cancelled) {
        try {
            HttpPost httpRequest = new HttpPost(uri);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("foo", "bar"));
            httpRequest.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8.name()));

            execute(httpRequest, null, cancelled);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void get(final String uri) {
        get(uri, null);
    }

    public void get(final String uri, final Runnable callback) {
        get(uri, callback, false);
    }

    public void get(final String uri, final Runnable callback, final boolean cancelled) {
        try {
            final HttpGet request = new HttpGet(uri);
            execute(request, callback, cancelled);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void execute(final HttpUriRequest request, final Runnable callback, final boolean cancelled) throws Exception {
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        try {
            httpclient.start();
            Future<HttpResponse> future = httpclient.execute(request, new FutureCallback<HttpResponse>() {
                public void completed(HttpResponse result) {
                    logger.debug("Completed");
                    if (callback != null) {
                        callback.run();
                    }
                }

                public void failed(Exception ex) {
                    logger.debug("Failed");
                    if (callback != null) {
                        callback.run();
                    }
                }

                public void cancelled() {
                    logger.debug("Cancelled");
                    if (callback != null) {
                        callback.run();
                    }
                }
            });

            if (cancelled) {
                future.cancel(true);
                return;
            }

            final HttpResponse response = (HttpResponse) future.get();
            String result = null;
            if ((response != null) && (response.getEntity() != null)) {
                try {
                    InputStream is = response.getEntity().getContent();
                    result = IOUtils.toString(is);
                    logger.debug("Response {} {} {}", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), result);
                } catch (IOException ignored) {
                }
            }
        } finally {
            httpclient.close();
        }
    }
}