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
package com.navercorp.pinpoint.manager.validator;

import org.springframework.util.StringUtils;

/**
 * @author minwoo.jung
 */
public abstract class ValueValidator {
    protected boolean validateLength(final String value, final int max, final int min) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }

        if (value.length() > max) {
            return false;
        }

        if (value.length() < min) {
            return false;
        }

        return true;
    }
}
