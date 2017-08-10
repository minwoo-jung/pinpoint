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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

/**
 * 해당 테스트를 위해서는 profiler.resttemplate 설정을 활성화 하여야 합니다.
 *
 * @author Taejin Koo
 */
@Controller
public class RestTemplateController {

    public static final String TEST_URL = "http://www.naver.com";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/resttemplate/default")
    @ResponseBody
    public String test1() {
        logger.info("/resttemplate/default");

        RestTemplate restTemplate = new RestTemplate();
        String forObject = restTemplate.getForObject(TEST_URL, String.class);

        logger.info(forObject);

        return "OK";
    }

    @RequestMapping(value = "/resttemplate/netty4")
    @ResponseBody
    public String test2(Model model) {
        logger.info("/resttemplate/netty4");

        RestTemplate restTemplate = new RestTemplate(new Netty4ClientHttpRequestFactory());
        String forObject = restTemplate.getForObject(TEST_URL, String.class);

        logger.info(forObject);

        return "OK";
    }

    @RequestMapping(value = "/resttemplate/httpComponent")
    @ResponseBody
    public String test3(Model model) {
        logger.info("/resttemplate/httpComponent");

        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        String forObject = restTemplate.getForObject(TEST_URL, String.class);

        return "OK";
    }

    @RequestMapping(value = "/resttemplate/okHttp3")
    @ResponseBody
    public String test4(Model model) {
        logger.info("/resttemplate/okHttp3");

        RestTemplate restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory());
        String forObject = restTemplate.getForObject(TEST_URL, String.class);

        return "OK";
    }

}
