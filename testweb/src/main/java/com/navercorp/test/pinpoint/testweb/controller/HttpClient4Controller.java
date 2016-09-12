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

package com.navercorp.test.pinpoint.testweb.controller;

import com.navercorp.test.pinpoint.testweb.connector.apachehttp4.ApacheHttpClient4;
import com.navercorp.test.pinpoint.testweb.connector.apachehttp4.HttpConnectorOptions;
import com.navercorp.test.pinpoint.testweb.util.Description;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;

/**
 * @author netspider
 */
@Controller
public class HttpClient4Controller {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Description("에러시 cookie덤프")
    @RequestMapping(value = "/httpclient4/cookie")
    @ResponseBody
    public String cookie(@RequestHeader(value = "Cookie", required = false) String cookie, HttpServletRequest request) {
        logger.info("Cookie:{}", cookie);

        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        client.execute("http://localhost:" + request.getLocalPort(), new HashMap<String, Object>(), cookie);

        return "OK";
    }

    @Description("에러시 post덤프")
    @RequestMapping(value = "/httpclient4/post")
    @ResponseBody
    public String post(HttpServletRequest request) {
        logger.info("Post");
        // String[] ports = new String[] { "9080", "10080", "11080" };
        // Random random = new Random();
        // String port = ports[random.nextInt(3)];
        //
        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        HashMap<String, Object> post = new HashMap<String, Object>();
        post.put("test", "1");
        post.put("test2", "2");
        client.execute("http://localhost:" + request.getLocalPort(), post);

        return "OK";
    }
    
    @RequestMapping(value = "/httpclient4/statusCode")
    @ResponseBody
    public String statusCode(HttpServletRequest request) {
        logger.info("status code");
        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        HashMap<String, Object> post = new HashMap<>();
        post.put("test", "1");
        post.put("test2", "2");
        client.execute2("http://www.naver.com", post);

        return "OK";
    }
    
    @RequestMapping(value = "/httpclient4/failed")
    @ResponseBody
    public String failed(HttpServletRequest request) {
        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());
        HashMap<String, Object> post = new HashMap<>();
        post.put("foo", "bar");
        client.execute("http://127.0.0.1:9999", post);

        return "OK";
    }
    
    @RequestMapping(value = "/httpclient4/fileUpload")
    @ResponseBody
    public String mutlpartFileUpload(HttpServletRequest request) {
        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());

        client.fileUpload("http://localhost:" + request.getLocalPort());

        return "OK";
    }
    

    
    

}
