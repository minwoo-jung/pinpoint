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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

/**
 * @author emeroad
 */
@Controller
public class HideHeaderController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String PINPOINT_HEADER = "Pinpoint-TraceID";
    private String PINPOINT_HEADER_STARTWITH = "Pinpoint-";

    @RequestMapping(value = "/hideHeader")
    @ResponseBody
    public String hideHeader(HttpServletRequest request) {
        String pinpointHeader = request.getHeader(PINPOINT_HEADER);
        if (pinpointHeader != null) {
            throw new RuntimeException("Test fail");
        }
        ApacheHttpClient4 client = new ApacheHttpClient4(new HttpConnectorOptions());

        String execute = client.execute("http://localhost:" + request.getLocalPort() + "/hideHeaderNestedTest.pinpoint", new HashMap<String, Object>());
        if (execute != null && !execute.equalsIgnoreCase("ok")) {
            throw new RuntimeException("pinpoint-header-test fail Caused:" + execute);
        }

        return "OK";
    }

    @RequestMapping(value = "/hideHeaderNestedTest")
    @ResponseBody
    public String hideHeaderTest(HttpServletRequest request) {
        logger.debug("{}", "hideHeaderTest");
        String pinpointHeader = request.getHeader(PINPOINT_HEADER);
        if (pinpointHeader != null) {
            logger.debug("getHeader:{}", pinpointHeader);
            return "fail(getHeader)";
        }
        Enumeration<String> headers = request.getHeaders(PINPOINT_HEADER);
        List<String> list = Collections.list(headers);
        if (list.size() != 0) {
            return "fail(getHeaders)";
        }

        Enumeration<String> headerNames = request.getHeaderNames();
        List<String> headerNamesList = Collections.list(headerNames);
        logger.debug("headernames:{}", headerNamesList);
        for (String headerName : headerNamesList) {
            if (headerName.regionMatches(true, 0, PINPOINT_HEADER_STARTWITH, 0, PINPOINT_HEADER_STARTWITH.length())) {
                return "fail(getHeaderNames) headerName:" + headerName;
            }
        }

        return "OK";
    }

    @RequestMapping(value = "/assertHeaderTest")
    @ResponseBody
    public String assertHeaderTest(HttpServletRequest request) {
        logger.debug("{}", "assertExistHeaderTest");
        String noExist = request.getHeader("noExist");
        if (noExist != null) {
            return "fail(noExist)";
        }


        Enumeration<String> headers = request.getHeaders("noExist");
        List<String> list = Collections.list(headers);
        if (list.isEmpty()) {
            return "fail(getHeaders)";
        }

        Enumeration<String> headerNames = request.getHeaderNames();
        ArrayList<String> headerNamesList = Collections.list(headerNames);
        for (String headerName : headerNamesList) {
            if (headerName.startsWith(PINPOINT_HEADER_STARTWITH)) {
                return "fail(getHeaderNames)";
            }
        }

        return "OK";
    }


}
