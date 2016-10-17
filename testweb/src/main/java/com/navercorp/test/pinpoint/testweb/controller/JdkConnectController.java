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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Naver on 2015-11-17.
 */
@Controller
public class JdkConnectController {

    @RequestMapping(value = "/jdk/connect")
    @ResponseBody
    public String get(@RequestHeader(value = "Cookie", required = false) String cookie, HttpServletRequest request) {

        try {
            URL url = new URL("http://google.com?foo=bar");
            try {
                URLConnection connection = url.openConnection();
                connection.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
        }

        return "OK";
    }

    @RequestMapping(value = "/jdk/connect2")
    @ResponseBody
    public String get2(@RequestHeader(value = "Cookie", required = false) String cookie, HttpServletRequest request) {

        try {
            URL url = new URL("http://google.com?foo=bar");
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                connection.getResponseCode();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
        }

        return "OK";
    }

}
