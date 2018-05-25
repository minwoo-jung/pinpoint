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

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public enum TokenType {

    ALL,
    SPAN,
    STAT,
    UNKNOWN;

    private static final Set<TokenType> TOKEN_TYPES = EnumSet.allOf(TokenType.class);

    public static TokenType getValue(String name) {
        if (name == null) {
            return UNKNOWN;
        }
        for (TokenType tokenType : TOKEN_TYPES) {
            if (name.equals(tokenType.name())) {
                return tokenType;
            }
        }

        return UNKNOWN;
    }

}
