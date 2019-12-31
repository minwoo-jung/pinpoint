/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sender.sender.security;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;

import com.google.inject.name.Names;

/**
 * @author Taejin Koo
 */
public class LicenseKeyHolder {

    private static final String KEY_LICENSE_KEY = "profiler.security.licensekey";

    private final String licenseKey;

    public LicenseKeyHolder(ProfilerConfig profilerConfig) {
        String licenseKey = profilerConfig.readString(KEY_LICENSE_KEY, null);
        Assert.isTrue(StringUtils.hasLength(licenseKey), "licenseKey may not be empty");

        this.licenseKey = licenseKey;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

}
