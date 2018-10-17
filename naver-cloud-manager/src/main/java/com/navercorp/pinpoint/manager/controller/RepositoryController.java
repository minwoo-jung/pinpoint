/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.manager.controller;

import com.navercorp.pinpoint.manager.service.MetadataService;
import com.navercorp.pinpoint.manager.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author minwoo.jung
 */

@RestController
@RequestMapping("/repository")
public class RepositoryController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    MetadataService metadataService;

    @RequestMapping(method = RequestMethod.GET)
    public Map<String, String> createRepository(@RequestParam("organizationName") String organizationName,@RequestParam("userId") String userId) throws Exception {
        Map<String, String> result = new HashMap<>();

        if (metadataService.existOrganization(organizationName)) {
            String message = "there is already same name organization. organizationName : " + organizationName + ", userId :" + userId;
            logger.error(message);
            result.put("errorCode", "500");
            result.put("errorMessage", message);
            return result;
        };

        try {
            repositoryService.createRepository(organizationName, userId);
        } catch (Exception e) {
            String message = "fail create repository. organizationName : " + organizationName + ", userId : " + userId;
            logger.error(message, e);
            result.put("errorCode", "500");
            result.put("errorMessage", message);
            return result;
        }

        //TODO agent 인증키 생성
        result.put("result", "SUCCESS");
        return result;
    }
}
