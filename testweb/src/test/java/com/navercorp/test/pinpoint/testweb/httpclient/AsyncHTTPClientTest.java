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

package com.navercorp.test.pinpoint.testweb.httpclient;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.ning.http.client.multipart.StringPart;
import org.junit.Assert;

import org.junit.Test;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;

public class AsyncHTTPClientTest {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Test
    public void asyncHttpClient() {
        AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder().build();
        AsyncHttpClient client = new AsyncHttpClient(new NettyAsyncHttpProvider(config), config);

        try {
            ListenableFuture<Response> future = client.prepareGet("http://www.naver.com").execute(new AsyncCompletionHandler<Response>() {
                @Override
                public Response onCompleted(Response response) throws Exception {
                    // do something
                    return response;
                }
            });

            future.get(3000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void bodyPart() {
        try {
            AsyncHttpClient asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setAllowPoolingConnections(true).setCompressionEnforced(true).build());
            BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost("http://dev.pinpoint.navercorp.com");

            requestBuilder.addBodyPart(new StringPart("name1", "value1"));
            requestBuilder.addBodyPart(new StringPart("name2", "value2"));

            ListenableFuture<Response> f = requestBuilder.execute();
            f.get(3000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void multiPart() {
        try {
            AsyncHttpClient asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setAllowPoolingConnections(true).setCompressionEnforced(true).build());
            BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost("http://dev.pinpoint.navercorp.com");

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("header1", "header1");
            headers.put("header2", "header2");

            requestBuilder.addBodyPart(new com.ning.http.client.multipart.ByteArrayPart("name1", "data".getBytes(), "plain/text", UTF_8, "filename1"));
            requestBuilder.addBodyPart(new com.ning.http.client.multipart.FilePart("name2", new File("pom.xml"), "mimeType", UTF_8));
            requestBuilder.addBodyPart(new com.ning.http.client.multipart.StringPart("name3", "value3"));
            requestBuilder.addBodyPart(new com.ning.http.client.multipart.FilePart("name4", new File("pom.xml")));
            requestBuilder.addBodyPart(new StringPart("name5", "value5"));

            ListenableFuture<Response> f = requestBuilder.execute();
            f.get(3000L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
