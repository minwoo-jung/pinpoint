/*
 * Copyright 2020 NAVER Corp.
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
package com.navercorp.pinpoint.manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author minwoo.jung
 */
public class HbaseAdminClient {

    private static final String CREATE_NAMESPACE_PATH = "/namespace/create/";
    private static final String RESULT = "result";
    private static final String MESSAGE = "message";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String targetDomain;


    @Autowired
    private RestTemplate restTemplate;

    public HbaseAdminClient(String targetDomain) {
        this.targetDomain = targetDomain;
    }

    public boolean createNamespaceIfNotExists(String namespaceName) {
        String url = targetDomain + CREATE_NAMESPACE_PATH + namespaceName;
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, null, Map.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            logger.error("fail to call hbase admin server for {}. return status code : {}. message : {}", namespaceName, responseEntity.getStatusCode(), responseEntity.getBody());
            return false;
        }

        Map<String, String> message = responseEntity.getBody();
        if ("success".equals(message.get(RESULT)) == false) {
            logger.error("can't create namespace({}). because : {}", namespaceName, message.get(MESSAGE));
            return false;
        }
        return true;
    }
}
