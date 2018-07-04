/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.handler;

import com.navercorp.pinpoint.collector.service.MetadataService;
import com.navercorp.pinpoint.collector.service.TokenService;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationKey;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.collector.vo.TokenCreateRequest;
import com.navercorp.pinpoint.collector.vo.TokenType;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.thrift.dto.command.TCmdGetAuthenticationToken;
import com.navercorp.pinpoint.thrift.dto.command.TCmdGetAuthenticationTokenRes;
import com.navercorp.pinpoint.thrift.dto.command.TTokenResponseCode;
import com.navercorp.pinpoint.thrift.dto.command.TTokenType;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * @author Taejin Koo
 */
@Service("createTokenHandler")
@Profile("tokenAuthentication")
public class CreateTokenHandler implements RequestResponseHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TokenService tokenService;

    @Autowired
    private MetadataService metadataService;

    @Override
    public void handleRequest(ServerRequest serverRequest, ServerResponse serverResponse) {
        final Object data = serverRequest.getData();
        if (data instanceof TBase) {
            TBase<?, ?> response = handleRequest((TBase<?, ?>) data, serverRequest.getRemoteAddress());

            serverResponse.write(response);
            return;
        } else {
            logger.warn("{} is not support type : ", ClassUtils.simpleClassName(serverRequest));
            TCmdGetAuthenticationTokenRes response = createResponse(TTokenResponseCode.BAD_REQUEST);

            serverResponse.write(response);
            return;
        }
    }

    TBase<?, ?> handleRequest(TBase<?, ?> tbase, String remoteAddress) {
        if (tbase instanceof TCmdGetAuthenticationToken) {
            TCmdGetAuthenticationToken getTokenCommand = (TCmdGetAuthenticationToken) tbase;
            if (!verifyRequest(getTokenCommand)) {
                return createResponse(TTokenResponseCode.BAD_REQUEST);
            }

            String licenseKey = getTokenCommand.getLicenseKey();
            PaaSOrganizationKey paasKey = metadataService.selectPaaSOrganizationkey(licenseKey);
            if (paasKey == null) {
                return createResponse(TTokenResponseCode.UNAUTHORIZED);
            }

            String organization = paasKey.getOrganization();
            PaaSOrganizationInfo organizationInfo = metadataService.selectPaaSOrganizationInfo(organization);
            if (organizationInfo == null || !verifyPaaSOrganizationInfo(organizationInfo)) {
                return createResponse(TTokenResponseCode.INTERNAL_SERVER_ERROR);
            }

            TokenCreateRequest tokenCreateRequest = createTokenCreateRequest(getTokenCommand, organizationInfo, remoteAddress);

            TCmdGetAuthenticationTokenRes response = null;
            try {
                Token token = tokenService.create(tokenCreateRequest);
                response = createResponse(token.getKey());
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
                response = createResponse(TTokenResponseCode.INTERNAL_SERVER_ERROR, e.getMessage());
            }
            return response;
        } else {
            return createResponse(TTokenResponseCode.BAD_REQUEST);
        }
    }

    private TCmdGetAuthenticationTokenRes createResponse(String token) {
        TCmdGetAuthenticationTokenRes response = new TCmdGetAuthenticationTokenRes();
        response.setCode(TTokenResponseCode.OK);
        response.setToken(token.getBytes());
        return response;
    }

    private TCmdGetAuthenticationTokenRes createResponse(TTokenResponseCode responseCode) {
        return createResponse(responseCode, responseCode.name());
    }

    private TCmdGetAuthenticationTokenRes createResponse(TTokenResponseCode responseCode, String message) {
        TCmdGetAuthenticationTokenRes response = new TCmdGetAuthenticationTokenRes();
        response.setCode(responseCode);
        response.setMessage(message);
        return response;
    }

    private boolean verifyRequest(TCmdGetAuthenticationToken request) {
        String licenseKey = request.getLicenseKey();
        if (licenseKey == null) {
            return false;
        }

        TTokenType tokenType = request.getTokenType();
        if (tokenType == null) {
            return false;
        }

        TokenType type = TokenType.valueOf(tokenType.name());
        if (type == TokenType.UNKNOWN) {
            return false;
        }

        return true;
    }

    private boolean verifyPaaSOrganizationInfo(PaaSOrganizationInfo organizationInfo) {
        String databaseName = organizationInfo.getDatabaseName();
        if (databaseName == null) {
            return false;
        }

        String hbaseNameSpace = organizationInfo.getHbaseNameSpace();
        if (hbaseNameSpace == null) {
            return false;
        }

        String organization = organizationInfo.getOrganization();
        if (organization == null) {
            return false;
        }

        return true;
    }

    private TokenCreateRequest createTokenCreateRequest(TCmdGetAuthenticationToken getTokenCommand, PaaSOrganizationInfo organizationInfo, String remoteAddress) {
        try {
            TTokenType ttokenType = getTokenCommand.getTokenType();
            TokenType tokenType = TokenType.valueOf(ttokenType.name());

            TokenCreateRequest tokenCreateRequest = new TokenCreateRequest(organizationInfo, tokenType, remoteAddress);
            return tokenCreateRequest;
        } catch (Exception e) {
            // skip
        }
        return null;
    }

}
