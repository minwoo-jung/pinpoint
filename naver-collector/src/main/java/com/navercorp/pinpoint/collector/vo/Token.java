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

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class Token {

    private final String key;
    private final String namespace;

    private final long createdTime;
    private final long expiryTime;

    private final TokenType tokenType;

    public Token(String key, String namespace, long createdTime, long expiryTime, TokenType tokenType) {
        this.key = key;
        this.namespace = namespace;
        this.createdTime = createdTime;
        this.expiryTime = expiryTime;
        this.tokenType = tokenType;
    }

    public String getKey() {
        return key;
    }

    public String getNamespace() {
        return namespace;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return createdTime == token.createdTime &&
                expiryTime == token.expiryTime &&
                Objects.equals(key, token.key) &&
                Objects.equals(namespace, token.namespace) &&
                tokenType == token.tokenType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, namespace, createdTime, expiryTime, tokenType);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Token{");
        sb.append("key='").append(key).append('\'');
        sb.append(", namespace='").append(namespace).append('\'');
        sb.append(", createdTime=").append(createdTime);
        sb.append(", expiryTime=").append(expiryTime);
        sb.append(", tokenType=").append(tokenType);
        sb.append('}');
        return sb.toString();
    }

}
