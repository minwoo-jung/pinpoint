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
package com.navercorp.pinpoint.collector.vo;

import org.springframework.util.StringUtils;

/**
 * @author minwoo.jung
 */
public class PaaSOrganizationKey {

    private String uuKey;
    private String organization;

    public PaaSOrganizationKey() {
    }

    public PaaSOrganizationKey(String uuKey, String organization) {
        if (StringUtils.isEmpty(uuKey)) {
            throw new IllegalArgumentException("key must not be empty");
        }
        if (StringUtils.isEmpty(organization)) {
            throw new IllegalArgumentException("organization must not be empty");
        }

        this.uuKey = uuKey;
        this.organization = organization;
    }

    public String getUuKey() {
        return uuKey;
    }

    public String getOrganization() {
        return organization;
    }

    public void setUuKey(String uuKey) {
        this.uuKey = uuKey;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
