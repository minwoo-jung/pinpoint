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

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.StringValue;
import com.navercorp.pinpoint.collector.service.NamespaceService;
import com.navercorp.pinpoint.collector.service.TokenService;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationKey;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.collector.vo.TokenCreateRequest;
import com.navercorp.pinpoint.collector.vo.TokenType;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.grpc.auth.AuthGrpc;
import com.navercorp.pinpoint.grpc.auth.PCmdGetTokenRequest;
import com.navercorp.pinpoint.grpc.auth.PCmdGetTokenResponse;
import com.navercorp.pinpoint.grpc.auth.PTokenResponseCode;
import com.navercorp.pinpoint.grpc.auth.PTokenType;
import com.navercorp.pinpoint.grpc.server.ServerContext;
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
        // do not recrod request for security
        logger.info("getToken() started");

        try {
            if (!verifyRequest(request)) {
                doResponse(responseObserver, PTokenResponseCode.BAD_REQUEST);
                return;
            }

            String licenseKey = request.getLicenseKey().getValue().toStringUtf8();
            PaaSOrganizationKey paasKey = namespaceService.selectPaaSOrganizationkey(licenseKey);
            if (paasKey == null) {
                doResponse(responseObserver, PTokenResponseCode.UNAUTHORIZED);
                return;
            }

            String organization = paasKey.getOrganization();
            PaaSOrganizationInfo organizationInfo = namespaceService.selectPaaSOrganizationInfo(organization);
            if (organizationInfo == null || !verifyPaaSOrganizationInfo(organizationInfo)) {
                doResponse(responseObserver, PTokenResponseCode.INTERNAL_SERVER_ERROR);
                return;
            }

            InetSocketAddress remoteAddress = ServerContext.getTransportMetadata().getRemoteAddress();

            TokenCreateRequest tokenCreateRequest = new TokenCreateRequest(organizationInfo, TokenType.valueOf(request.getTokenType().name()), remoteAddress.getHostString());
            Token token = tokenService.create(tokenCreateRequest);
            doResponse(responseObserver, ByteString.copyFromUtf8(token.getKey()));
        } catch (Exception e) {
            logger.warn("Failed to handle createToken. message:{}", e.getMessage(), e);
            doResponse(responseObserver, PTokenResponseCode.INTERNAL_SERVER_ERROR);
        } finally {
            responseObserver.onCompleted();
        }
    }

    private void doResponse(StreamObserver<PCmdGetTokenResponse> responseObserver, PTokenResponseCode responseCode) {
        doResponse(responseObserver, responseCode, responseCode.name(), null);
    }

    private void doResponse(StreamObserver<PCmdGetTokenResponse> responseObserver, ByteString tokenValue) {
        doResponse(responseObserver, PTokenResponseCode.OK, PTokenResponseCode.OK.name(), tokenValue);
    }

    private void doResponse(StreamObserver<PCmdGetTokenResponse> responseObserver, PTokenResponseCode responseCode, String message, ByteString tokenValue) {
        PCmdGetTokenResponse.Builder responseBuilder = PCmdGetTokenResponse.newBuilder();
        responseBuilder.setCode(responseCode);
        responseBuilder.setMessage(StringValue.of(message));
        if (tokenValue != null) {
            responseBuilder.setToken(BytesValue.of(tokenValue));
        }
        responseObserver.onNext(responseBuilder.build());
    }


    private boolean verifyRequest(PCmdGetTokenRequest request) {
        String licenseKey = request.getLicenseKey().getValue().toStringUtf8();
        if (StringUtils.isEmpty(licenseKey)) {
            return false;
        }

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
