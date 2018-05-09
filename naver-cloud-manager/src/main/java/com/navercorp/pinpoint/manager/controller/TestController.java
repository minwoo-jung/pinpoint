/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.manager.controller;

import com.navercorp.pinpoint.manager.service.ManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ManagementService managementService;

    @GetMapping("/getTimestamp")
    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    @GetMapping("/tableNames")
    public List<String> getTableNames(@RequestParam(value = "namespace", required = false, defaultValue = "default") String namespace) {
        return managementService.getTableNames(namespace);
    }
}
