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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.navercorp.test.pinpoint.testweb.util.Charsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Naver on 2015-11-17.
 */
@Controller
public class JdkConnectController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/jdk/connect")
    @ResponseBody
    public String get(@RequestHeader(value = "Cookie", required = false) String cookie, HttpServletRequest request) {

        final URL url = newURL("http://google.com?foo=bar");
        try {
            URLConnection connection = url.openConnection();
            connection.connect();
        } catch (IOException e) {
            logger.warn("{} open error", url, e);
            return "fail";
        }

        return "OK";
    }

    @RequestMapping(value = "/jdk/connect2")
    @ResponseBody
    public String get2(@RequestHeader(value = "Cookie", required = false) String cookie, HttpServletRequest request) {

        final URL url = newURL("http://google.com?foo=bar");
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            final int responseCode = connection.getResponseCode();
            final List<String> contents = readStream(connection);
            logger.info("code:{} contents:{}", responseCode, contents);

        } catch (IOException e) {
            logger.warn("{} open error", url, e);
            return "fail";
        }


        return "OK";
    }

    private List<String> readStream(HttpURLConnection connection) throws IOException {
        final InputStream inputStream = connection.getInputStream();
        try {
            return IOUtils.readLines(inputStream, Charsets.UTF_8);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private URL newURL(String spec) {
        try {
            return new URL(spec);
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("invalid url" + spec, exception);
        }
    }

}
