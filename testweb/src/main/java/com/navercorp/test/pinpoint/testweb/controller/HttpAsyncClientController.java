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

import javax.servlet.http.HttpServletRequest;

import com.navercorp.test.pinpoint.testweb.service.HttpAsyncClientService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author netspider
 * @author jaehong.kim
 */
@Controller
public class HttpAsyncClientController {
    private static final String HTTP_CLIENT3_GET_URL = "/httpclient3/get.pinpoint";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HttpAsyncClientService httpService;

    @RequestMapping(value = "/httpAsyncClient/get")
    @ResponseBody
    public String httpClient4Get(final HttpServletRequest request) {
        final String localAddress = getLocalAddress(request);
        httpService.get("http://naver.com");
        return "OK";
    }

    @RequestMapping(value = "/httpAsyncClient/get2")
    @ResponseBody
    public String httpClient4Get2(final HttpServletRequest request) {
        final String localAddress = getLocalAddress(request);
        httpService.get("http://" + localAddress + HTTP_CLIENT3_GET_URL);
        httpService.get("http://" + localAddress + HTTP_CLIENT3_GET_URL);

        return "OK";
    }

    @RequestMapping(value = "/httpAsyncClient/cancelled")
    @ResponseBody
    public String httpClient4Cancelled(final HttpServletRequest request) {
        final String localAddress = getLocalAddress(request);
        httpService.get("http://" + localAddress + HTTP_CLIENT3_GET_URL, null, true);
        return "OK";
    }

    @RequestMapping(value = "/httpAsyncClient/cancelledAndCallback")
    @ResponseBody
    public String httpClient4CancelledAndCallback(final HttpServletRequest request) {
        final String localAddress = getLocalAddress(request);
        Runnable callback = new Runnable() {
            public void run() {
                logger.info("Completed callback.");
            }
        };

        httpService.get("http://" + localAddress + HTTP_CLIENT3_GET_URL, callback, true);
        return "OK";
    }

    @RequestMapping(value = "/httpAsyncClient/failed")
    @ResponseBody
    public String httpClient4Failed() {
        String result = "OK";
        try {
            httpService.get("http://127.0.0.1:9999", null);
        } catch(Exception ignored) {
            result += ": " + ignored.getMessage();
        }

        return result;
    }

    @RequestMapping(value = "/httpAsyncClient/failedAndCallback")
    @ResponseBody
    public String httpClient4FailedAndCallback(final HttpServletRequest request) {
        final String localAddress = getLocalAddress(request);
        Runnable callback = new Runnable() {
            public void run() {
                logger.info("Completed callback.");
            }
        };
        String result = "OK";
        try {
            httpService.get("http://127.0.0.1:9999", callback);
        } catch(Exception ignored) {
            result += ": " + ignored.getMessage();
        }

        return result;
    }

    @RequestMapping(value = "/httpAsyncClient/getAndCallback")
    @ResponseBody
    public String httpClient4GetAndCallback(final HttpServletRequest request) {
        final String localAddress = getLocalAddress(request);
        Runnable callback = new Runnable() {
            public void run() {
                logger.info("Completed callback.");
            }
        };

        httpService.get("http://" + localAddress + HTTP_CLIENT3_GET_URL, callback);

        return "OK";
    }

    @RequestMapping(value = "/httpAsyncClient/getAndAsyncCallback")
    @ResponseBody
    public String httpClient4GetAndAsyncCallback(final HttpServletRequest request) {
        final String localAddress = getLocalAddress(request);
        Runnable callback = new Runnable() {
            public void run() {
                httpService.get("http://" + localAddress + "/async/httpClient4/getAndCallback.pinpoint");
            }
        };

        httpService.get("http://" + localAddress + HTTP_CLIENT3_GET_URL, callback);

        return "OK";
    }

    private String getLocalAddress(final HttpServletRequest request) {
        return request.getLocalAddr() + ":" + request.getLocalPort();
    }
}