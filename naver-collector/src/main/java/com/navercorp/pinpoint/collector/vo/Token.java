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

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class Token {

    private final String key;
    private final PaaSOrganizationInfo paaSOrganizationInfo;

    private final long expiryTime;

    private final String remoteAddress;
    private final TokenType tokenType;

    public Token(String key, PaaSOrganizationInfo paaSOrganizationInfo, long expiryTime, String remoteAddress, TokenType tokenType) {
        this.key = Assert.requireNonNull(key, "key must not be null");
        this.paaSOrganizationInfo = Assert.requireNonNull(paaSOrganizationInfo, "paaSOrganizationInfo must not be null");

        Assert.isTrue(expiryTime > 0, "expiryTime must be greater than 0");
        this.expiryTime = expiryTime;

        this.remoteAddress = Assert.requireNonNull(remoteAddress, "remoteAddress must not be null");
        this.tokenType = Assert.requireNonNull(tokenType, "tokenType must not be null");
    }

    public String getKey() {
        return key;
    }

    public PaaSOrganizationInfo getPaaSOrganizationInfo() {
        return paaSOrganizationInfo;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return expiryTime == token.expiryTime &&
                Objects.equals(key, token.key) &&
                Objects.equals(paaSOrganizationInfo, token.paaSOrganizationInfo) &&
                Objects.equals(remoteAddress, token.remoteAddress) &&
                tokenType == token.tokenType;
    }

    @Override
    public int hashCode() {

        return Objects.hash(key, paaSOrganizationInfo, expiryTime, remoteAddress, tokenType);
    }

    @Override
    public String toString() {
        return "Token{" +
                "key='" + key + '\'' +
                ", paaSOrganizationInfo=" + paaSOrganizationInfo +
                ", expiryTime=" + expiryTime +
                ", remoteAddress='" + remoteAddress + '\'' +
                ", tokenType=" + tokenType +
                '}';
    }

}
