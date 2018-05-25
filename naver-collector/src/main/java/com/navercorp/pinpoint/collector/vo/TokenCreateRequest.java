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

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.thrift.dto.command.TTokenType;

/**
 * @author Taejin Koo
 */
public class TokenCreateRequest {

    private final String userId;
    private final String password;
    private final TokenType tokenType;

    public TokenCreateRequest(String userId, String password, TTokenType tokenType) {
        this.userId = Assert.requireNonNull(userId, "userId must not be null");
        this.password = Assert.requireNonNull(password, "password must not be null");
        Assert.requireNonNull(tokenType, "tokenType must not be null");

        TokenType type = TokenType.valueOf(tokenType.name());
        Assert.isTrue(type != TokenType.UNKNOWN, "unknown token type");
        this.tokenType = type;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

}
