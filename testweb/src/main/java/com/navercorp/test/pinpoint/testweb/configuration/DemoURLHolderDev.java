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

import java.util.Random;

/**
 * 
 * @author netspider
 *
 */
public class DemoURLHolderDev extends DemoURLHolder {

    private final Random random = new Random();

    private static final String[] BACKENDWEB_CALL_URL = {
        "http://dev-pinpoint-workload02.ncl:8080/backendweb.pinpoint",
        "http://dev-pinpoint-workload02.ncl:9080/backendweb.pinpoint"
    };

    private static final String[] BACKENDAPI_CALL_URL = {
        "http://dev-pinpoint-workload03.ncl:8080/backendapi.pinpoint",
        "http://dev-pinpoint-workload03.ncl:9080/backendapi.pinpoint"
    };

    @Override
    public String getBackendWebURL() {
        return BACKENDWEB_CALL_URL[random.nextInt(BACKENDWEB_CALL_URL.length)];
    }

    @Override
    public String getBackendApiURL() {
        return BACKENDAPI_CALL_URL[random.nextInt(BACKENDAPI_CALL_URL.length)];
    }
}
