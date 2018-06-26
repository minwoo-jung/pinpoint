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

package com.navercorp.test.pinpoint.testweb.configuration;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Random;

/**
 * @author netspider
 */
public class DemoURLHolder {

    private final Random random = new Random();

    @Value("#{testWebProps['backendweb.url.list'].split(',')}")
    private List<String> backendWebUrlList;

    @Value("#{testWebProps['backendapi.url.list'].split(',')}")
    private List<String> backendApiUrlList;

    public String getBackendWebURL() {
        return backendWebUrlList.get(random.nextInt()%backendApiUrlList.size());
    }

    public String getBackendApiURL() {
        return backendApiUrlList.get(random.nextInt()%backendApiUrlList.size());
    }


}