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

package com.navercorp.pinpoint.grpc.security.server;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author Taejin Koo
 */
public class AuthKeyHolder {

    private final String key;

    public AuthKeyHolder(String key) {
        Assert.isTrue(StringUtils.hasLength(key), "key must not be empty");
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AuthKeyHolder{");
        sb.append("key='").append(key).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
