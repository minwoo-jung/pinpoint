/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.ngrinder;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Jongho Moon
 *
 */
public class NGrinder {
    private static final String BASE_URL = "http://ngrinder.navercorp.com/perftest/api";

    private final CloseableHttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    private final String userId;
    private final String password;

    public NGrinder(String userId, String password) {
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
            client = HttpClients.custom().setSSLSocketFactory(sslsf).setRedirectStrategy(new LaxRedirectStrategy()).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.userId = userId;
        this.password = password;
    }

    public String test(int agent, int process, int thread, long duration) {
        try {
            login();
            String nextTestParam = getNextTestParam();
            Map<String, Object> nextTest = createTestData(agent, process, thread, duration, nextTestParam);
            int testId = startTest(nextTest);
            waitTest(testId);

            return nextTestParam;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void login() throws IOException {
        System.out.println("Login to SSO system...");
        HttpUriRequest post = RequestBuilder.post()
                .setUri("https://sso.navercorp.com/siteminderagent/forms/login.fcc")
                .addParameter("target", "http://ngrinder.navercorp.com")
                .addParameter("smauthreason", "0")
                .addParameter("smagentname", "ngrinder.nhncorp.com")
                .addParameter("USER", userId)
                .addParameter("PASSWORD", password).build();

        CloseableHttpResponse response = client.execute(post);
        EntityUtils.consume(response.getEntity());
    }

    private String getNextTestParam() throws IOException {
        System.out.println("Resolve next test param...");
        
        int page = 1;

        while (true) {
            HttpUriRequest get = RequestBuilder.get()
                    .setUri(BASE_URL)
                    .addParameter("size", "10")
                    .addParameter("page", String.valueOf(page)).build();
            CloseableHttpResponse response = client.execute(get);

            try {
                HttpEntity entity = response.getEntity();
                String content = EntityUtils.toString(entity);
                List<Map<String, Object>> list = mapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {
                });

                if (list.isEmpty()) {
                    System.out.println("No previous test found. Use test param: 1");
                    return String.valueOf(1);
                }

                for (Map<String, Object> test : list) {
                    String tags = (String) test.get("tagString");
                    List<String> tagList = Arrays.asList(tags.split(","));

                    if (tagList.contains("pinpoint-collector") && tagList.contains("auto")) {
                        String testParam = (String) test.get("param");
                        String nextParam = String.valueOf(Integer.parseInt(testParam) + 1);
                        
                        System.out.println("Resolved next param: " + nextParam);
                        
                        return nextParam;
                    }
                }
            } finally {
                response.close();
            }

            page++;
        }
    }

    private Map<String, Object> createTestData(int agent, int process, int thread, long duration, String param) {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("testName", "Pinpoint Collector Load Test");
        map.put("tagString", "pinpoint-collector,auto");
        map.put("description", "");
        map.put("status", "READY");
        // map.put("scheduledTime", "");
        map.put("useRampUp", false);
        map.put("rampUpType", "PROCESS");
        map.put("threshold", "D");
        map.put("scriptName", "collector/collector.groovy");
        map.put("duration", duration);
        map.put("runCount", 0);
        map.put("region", "chuncheon");
        map.put("agentCount", agent);
        map.put("vuserPerAgent", process * thread);
        map.put("processes", process);
        map.put("threads", thread);
        map.put("samplingInterval", 2);
        map.put("ignoreSampleCount", 0);
        map.put("param", param);
        map.put("safeDistribution", "false");
        // map.put("rampUpInitCount", 0);
        // map.put("rampUpInitSleepTime", 0);
        // map.put("rampUpStep", 1);
        // map.put("rampUpIncrementInterval", 1000);
        map.put("testComment", "");

        return map;
    }

    private int startTest(Map<String, Object> test) throws IOException {
        System.out.println("Start Test: ");
        RequestBuilder builder = RequestBuilder.post().setUri(BASE_URL);

        for (Map.Entry<String, Object> entry : test.entrySet()) {
            builder.addParameter(entry.getKey(), String.valueOf(entry.getValue()));
            System.out.println("\t" + entry.getKey() + ": " + entry.getValue());
        }

        HttpUriRequest post = builder.build();
        CloseableHttpResponse response = client.execute(post);
        String content = EntityUtils.toString(response.getEntity());

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new RuntimeException("Failed: " + response.getStatusLine().getStatusCode());
        }

        Map<String, Object> registered = mapper.readValue(content, new TypeReference<Map<String, Object>>() {});
        int id = (Integer) registered.get("id");
        
        System.out.println("Test ID: " + id);

        return id;
    }
    
    private static final String[] FINISHED_STATUSES = {
        "FINISHED",
        "STOP_BY_ERROR",
        "CANCELED",
        "UNKNOWN"
    };

    private void waitTest(int testId) throws Exception {
        while (true) {
            HttpGet get = new HttpGet(BASE_URL + "/status?ids=" + testId);
            CloseableHttpResponse response = client.execute(get);
            String content = EntityUtils.toString(response.getEntity());

            Map<String, List<Map<String, Object>>> map = mapper.readValue(content, new TypeReference<Map<String, List<Map<String, Object>>>>() {});
            String status = (String) map.get("status").get(0).get("status_id");

            for (String s : FINISHED_STATUSES) {
                if (s.equals(status)) {
                    System.out.println("Test finished: " + status);
                    return;
                }
            }
            
            System.out.println("Waiting for the test to finish: " + status);
            Thread.sleep(5 * 1000);
        }
    }

    public static void main(String args[]) throws Exception {
        System.out.println(new NGrinder("kr12097", "web12097!@#").test(1, 2, 10, 60 * 1000));
    }
}
