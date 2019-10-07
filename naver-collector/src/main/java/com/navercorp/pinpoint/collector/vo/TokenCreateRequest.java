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
import com.navercorp.pinpoint.grpc.security.TokenType;

/**
 * @author Taejin Koo
 */
public class TokenCreateRequest {

    private final PaaSOrganizationInfo paaSOrganizationInfo;
    private final TokenType tokenType;
    private final String remoteAddress;

    public TokenCreateRequest(PaaSOrganizationInfo paaSOrganizationInfo, TokenType tokenType, String remoteAddress) {
        this.paaSOrganizationInfo = Assert.requireNonNull(paaSOrganizationInfo, "paaSOrganizationInfo must not be null");
        this.tokenType = Assert.requireNonNull(tokenType, "tokenType must not be null");
        this.remoteAddress = Assert.requireNonNull(remoteAddress, "remoteAddress must not be null");
    }

    public PaaSOrganizationInfo getPaaSOrganizationInfo() {
        return paaSOrganizationInfo;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

}
