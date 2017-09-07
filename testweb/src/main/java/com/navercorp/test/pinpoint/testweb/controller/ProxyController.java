/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.test.pinpoint.testweb.controller;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

/**
 * @author jaehong.kim
 */
@Controller
public class ProxyController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/proxy")
    @ResponseBody
    public String proxy(HttpServletRequest request) {
        StringBuilder buffer = new StringBuilder();
        Enumeration<String> enumeration = request.getHeaderNames();
        while(enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            buffer.append(name).append("=").append(value).append("<br>\n");
        }

        logger.debug("## Headers " + buffer.toString());

        return buffer.toString();
    }


    @RequestMapping(value = "/proxy/apacheHttpd")
    @ResponseBody
    public String proxyApacheHttpd(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = "t=" + System.currentTimeMillis() + "999" + " D=12345 i=51 b=49";

        request(url, "Pinpoint-ProxyApache", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/apacheHttpd/plus1m")
    @ResponseBody
    public String proxyApacheHttpdPlus1m(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = "t=" + (System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1)) + "999" + " D=12345 i=51 b=49";

        request(url, "Pinpoint-ProxyApache", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/apacheHttpd/plus1h")
    @ResponseBody
    public String proxyApacheHttpdPlus1h(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = "t=" + (System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)) + "999" + " D=12345 i=51 b=49";

        request(url, "Pinpoint-ProxyApache", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/apacheHttpd/plus1d")
    @ResponseBody
    public String proxyApacheHttpdPlus1d(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = "t=" + (System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)) + "999" + " D=12345 i=51 b=49";

        request(url, "Pinpoint-ProxyApache", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/apacheHttpd/minus1m")
    @ResponseBody
    public String proxyApacheHttpdMinus1m(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = "t=" + (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1)) + "999" + " D=12345 i=51 b=49";

        request(url, "Pinpoint-ProxyApache", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/apacheHttpd/minus1h")
    @ResponseBody
    public String proxyApacheHttpdMinus1h(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = "t=" + (System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)) + "999" + " D=12345 i=51 b=49";

        request(url, "Pinpoint-ProxyApache", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/apacheHttpd/minus1d")
    @ResponseBody
    public String proxyApacheHttpdMinus1d(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = "t=" + (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)) + "999" + " D=12345 i=51 b=49";

        request(url, "Pinpoint-ProxyApache", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/nginx")
    @ResponseBody
    public String proxyNginx(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        String timestamp = String.valueOf(System.currentTimeMillis());
        timestamp = timestamp.substring(0, timestamp.length() - 3) + "." + timestamp.substring(timestamp.length() - 3);

        final String proxyHeaderValue = timestamp + " 0.000";

        request(url, "Pinpoint-ProxyNginx", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/nginx/plus3s")
    @ResponseBody
    public String proxyNginxPlus3s(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        String timestamp = String.valueOf(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3));
        timestamp = timestamp.substring(0, timestamp.length() - 3) + "." + timestamp.substring(timestamp.length() - 3);

        final String proxyHeaderValue = timestamp + " 0.123";

        request(url, "Pinpoint-ProxyNginx", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/nginx/minus3s")
    @ResponseBody
    public String proxyNginxMinus3s(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        String timestamp = String.valueOf(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(3));
        timestamp = timestamp.substring(0, timestamp.length() - 3) + "." + timestamp.substring(timestamp.length() - 3);

        final String proxyHeaderValue = timestamp + " 1.234";

        request(url, "Pinpoint-ProxyNginx", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/nginx/msec")
    @ResponseBody
    public String proxyNginxMsec(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        String timestamp = String.valueOf(System.currentTimeMillis());
        timestamp = timestamp.substring(0, timestamp.length() - 3) + "." + timestamp.substring(timestamp.length() - 3);

        final String proxyHeaderValue = timestamp;

        request(url, "Pinpoint-ProxyNginx", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/commons")
    @ResponseBody
    public String proxyCommons(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = String.valueOf(System.currentTimeMillis());

        request(url, "Pinpoint-ProxyApp", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/commons/plus2s")
    @ResponseBody
    public String proxyCommonsPlus2s(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = String.valueOf(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(2));

        request(url, "Pinpoint-ProxyApp", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/commons/plus2m")
    @ResponseBody
    public String proxyCommonsPlus2m(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = String.valueOf(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2));

        request(url, "Pinpoint-ProxyApp", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/commons/plus2h")
    @ResponseBody
    public String proxyCommonsPlus2h(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = String.valueOf(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2));

        request(url, "Pinpoint-ProxyApp", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/commons/plus2d")
    @ResponseBody
    public String proxyCommonsPlus2d(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = String.valueOf(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2));

        request(url, "Pinpoint-ProxyApp", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/commons/minus2s")
    @ResponseBody
    public String proxyCommonsMinus2s(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = String.valueOf(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(2));

        request(url, "Pinpoint-ProxyApp", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/commons/minus2m")
    @ResponseBody
    public String proxyCommonsMinus2m(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = String.valueOf(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2));

        request(url, "Pinpoint-ProxyApp", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/commons/minus2h")
    @ResponseBody
    public String proxyCommonsMinus2h(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = String.valueOf(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2));

        request(url, "Pinpoint-ProxyApp", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/commons/minus2d")
    @ResponseBody
    public String proxyCommonsMinus2d(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = String.valueOf(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2));

        request(url, "Pinpoint-ProxyApp", proxyHeaderValue);
        return proxyHeaderValue;
    }

    @RequestMapping(value = "/proxy/app")
    @ResponseBody
    public String proxyApp(HttpServletRequest request) {
        final String url = "http://" + request.getLocalAddr() + ":" + request.getLocalPort() + "/proxy.pinpoint";
        final String proxyHeaderValue = String.valueOf(System.currentTimeMillis()) + " foo-bar";

        request(url, "Pinpoint-ProxyApp", proxyHeaderValue);
        return proxyHeaderValue;
    }

    private void request(final String url, final String proxyHeaderName, final String proxyHeaderValue) {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        method.setRequestHeader(proxyHeaderName, proxyHeaderValue);
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                logger.error("Method failed: " + method.getStatusLine());
            }
        } catch (HttpException e) {
            logger.error("Fatal protocol violation: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Fatal transport error: " + e.getMessage());
        } finally {
            method.releaseConnection();
        }
    }
}