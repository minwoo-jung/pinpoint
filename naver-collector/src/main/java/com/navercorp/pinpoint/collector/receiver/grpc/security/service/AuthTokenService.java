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

package com.navercorp.pinpoint.collector.receiver.grpc.security.service;

import com.navercorp.pinpoint.collector.receiver.grpc.security.AuthTokenException;
import com.navercorp.pinpoint.collector.service.NamespaceService;
import com.navercorp.pinpoint.collector.service.TokenService;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationKey;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.collector.vo.TokenCreateRequest;
import com.navercorp.pinpoint.collector.vo.TokenType;
import com.navercorp.pinpoint.grpc.auth.AuthGrpc;
import com.navercorp.pinpoint.grpc.auth.PAuthCode;
import com.navercorp.pinpoint.grpc.auth.PAuthorizationToken;
import com.navercorp.pinpoint.grpc.auth.PCmdGetTokenRequest;
import com.navercorp.pinpoint.grpc.auth.PCmdGetTokenResponse;
import com.navercorp.pinpoint.grpc.auth.PSecurityResult;
import com.navercorp.pinpoint.grpc.auth.PTokenType;
import com.navercorp.pinpoint.grpc.security.server.AuthKeyHolder;
import com.navercorp.pinpoint.grpc.security.server.GrpcSecurityContext;
import com.navercorp.pinpoint.grpc.server.ServerContext;

import com.google.protobuf.StringValue;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;

/**
 * @author Taejin Koo
 */
@Service("authTokenService")
@Profile("tokenAuthentication")
public class AuthTokenService extends AuthGrpc.AuthImplBase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TokenService tokenService;

    @Autowired
    private NamespaceService namespaceService;

    @Override
    public void getToken(PCmdGetTokenRequest request, StreamObserver<PCmdGetTokenResponse> responseObserver) {
        // do not record request for security
        logger.info("getToken() started");

        try {
            if (!verifyRequest(request)) {
                throw new AuthTokenException(PAuthCode.BAD_REQUEST);
            }

            AuthKeyHolder authKeyHolder = GrpcSecurityContext.getAuthKeyHolder();
            if (authKeyHolder == null || authKeyHolder.getKey() == null) {
                throw new AuthTokenException(PAuthCode.INTERNAL_SERVER_ERROR);
            }
            String securityKey = authKeyHolder.getKey();

            PaaSOrganizationKey paasKey = namespaceService.selectPaaSOrganizationkey(securityKey);
            if (paasKey == null) {
                throw new AuthTokenException(PAuthCode.UNAUTHORIZED);
            }

            String organization = paasKey.getOrganization();
            PaaSOrganizationInfo organizationInfo = namespaceService.selectPaaSOrganizationInfo(organization);
            if (organizationInfo == null || !verifyPaaSOrganizationInfo(organizationInfo)) {
                throw new AuthTokenException(PAuthCode.INTERNAL_SERVER_ERROR);
            }

            InetSocketAddress remoteAddress = ServerContext.getTransportMetadata().getRemoteAddress();

            TokenCreateRequest tokenCreateRequest = new TokenCreateRequest(organizationInfo, TokenType.valueOf(request.getTokenType().name()), remoteAddress.getHostString());
            Token token = tokenService.create(tokenCreateRequest);
            doResponse(responseObserver, token.getKey());
        } catch (AuthTokenException e) {
            logger.warn("Failed to handle createToken. message:{}", e.getMessage(), e);
            doResponse(responseObserver, e.getAuthCode());
        } catch (Exception e) {
            logger.warn("Failed to handle createToken. message:{}", e.getMessage(), e);
            doResponse(responseObserver, PAuthCode.INTERNAL_SERVER_ERROR);
        } finally {
            responseObserver.onCompleted();
        }
    }

    public boolean authenticate(String licenseKey) {
        PaaSOrganizationKey paaSOrganizationKey = namespaceService.selectPaaSOrganizationkey(licenseKey);
        return paaSOrganizationKey != null;
    }

    public boolean authorization(String tokenKey, TokenType tokenType) {
        Token token = tokenService.getAndRemove(tokenKey, tokenType);
        return token != null;
    }

    private PSecurityResult createResult(PAuthCode code) {
        return createResult(code, code.name());
    }

    private PSecurityResult createResult(PAuthCode code, String message) {
        PSecurityResult.Builder builder = PSecurityResult.newBuilder();
        builder.setCode(code);
        builder.setMessage(StringValue.of(message));
        return builder.build();
    }

    private void doResponse(StreamObserver<PCmdGetTokenResponse> responseObserver, PAuthCode authCode) {
        doResponse(responseObserver, authCode, null);
    }

    private void doResponse(StreamObserver<PCmdGetTokenResponse> responseObserver, String tokenValue) {
        doResponse(responseObserver, PAuthCode.OK, tokenValue);
    }

    private void doResponse(StreamObserver<PCmdGetTokenResponse> responseObserver, PAuthCode authCode, String tokenValue) {
        PSecurityResult result = createResult(authCode);

        PCmdGetTokenResponse.Builder responseBuilder = PCmdGetTokenResponse.newBuilder();
        responseBuilder.setResult(result);

        if (tokenValue != null) {
            PAuthorizationToken token = PAuthorizationToken.newBuilder().setToken(tokenValue).build();
            responseBuilder.setAuthorizationToken(token);
        }
        responseObserver.onNext(responseBuilder.build());
    }


    private boolean verifyRequest(PCmdGetTokenRequest request) {
        PTokenType tokenType = request.getTokenType();
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

}
