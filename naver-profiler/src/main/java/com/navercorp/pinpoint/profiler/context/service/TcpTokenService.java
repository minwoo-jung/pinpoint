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

package com.navercorp.pinpoint.profiler.context.service;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.io.request.EmptyMessage;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.io.util.TypeLocator;
import com.navercorp.pinpoint.profiler.ResponseMessageFutureListener;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.rpc.DefaultFuture;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.thrift.dto.command.TCmdGetAuthenticationToken;
import com.navercorp.pinpoint.thrift.dto.command.TCmdGetAuthenticationTokenRes;
import com.navercorp.pinpoint.thrift.dto.command.TTokenResponseCode;
import com.navercorp.pinpoint.thrift.dto.command.TTokenType;
import com.navercorp.pinpoint.thrift.io.AuthenticationTBaseLocator;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class TcpTokenService implements TokenService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EnhancedDataSender dataSender;
    private final DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory;

    public TcpTokenService(EnhancedDataSender dataSender) {
        this.dataSender = Assert.requireNonNull(dataSender, "dataSender must not be null");

        TypeLocator<TBase<?, ?>> typeLocator = AuthenticationTBaseLocator.getTypeLocator();
        this.deserializerFactory = new HeaderTBaseDeserializerFactory(typeLocator);
    }

    @Override
    public byte[] getToken(String licenseKey, String tokenType) {
        Assert.requireNonNull(licenseKey, "licenseKey must not be null");
        Assert.requireNonNull(tokenType, "tokenType must not be null");

        TCmdGetAuthenticationToken tokenRequest = new TCmdGetAuthenticationToken();
        tokenRequest.setLicenseKey(licenseKey);

        TTokenType tTokenType = getTokenType(tokenType);
        if (tTokenType == TTokenType.UNKNOWN) {
            throw new IllegalArgumentException("tokenType is invalid");
        }
        tokenRequest.setTokenType(tTokenType);

        TCmdGetAuthenticationTokenRes response = request(tokenRequest);
        if (response == null) {
            return null;
        }

        if (response.getCode() != TTokenResponseCode.OK) {
           logger.warn("failed to get authentication token. code:{}, message:{}", response.getCode().name(), response.getMessage());
           return null;
        }
        return response.getToken();
    }

    private static final Set<TTokenType> TOKEN_TYPES = EnumSet.allOf(TTokenType.class);

    private static TTokenType getTokenType(String name) {
        if (name == null) {
            return TTokenType.UNKNOWN;
        }
        for (TTokenType tokenType : TOKEN_TYPES) {
            if (name.equalsIgnoreCase(tokenType.name())) {
                return tokenType;
            }
        }

        return TTokenType.UNKNOWN;
    }

    private TCmdGetAuthenticationTokenRes request(TCmdGetAuthenticationToken tokenRequest) {
        try {
            final DefaultFuture<ResponseMessage> future = new DefaultFuture<ResponseMessage>();
            dataSender.request(tokenRequest, new ResponseMessageFutureListener(future));
            if (!future.await()) {
                logger.warn("request timed out while waiting for response.");
                return null;
            }
            if (!future.isSuccess()) {
                Throwable t = future.getCause();
                logger.warn("request failed.", t);
                return null;
            }
            ResponseMessage responseMessage = future.getResult();
            if (responseMessage == null) {
                logger.warn("result not set.");
                return null;
            }
            return getResult(responseMessage);
        } catch (Exception e) {
            logger.warn("failed to get authentication token. caused:{}", e.getMessage(), e);
        }
        return null;
    }

    private TCmdGetAuthenticationTokenRes getResult(ResponseMessage responseMessage) {
        byte[] message = responseMessage.getMessage();
        final Message<TBase<?, ?>> deserialize = SerializationUtils.deserialize(message, deserializerFactory, EmptyMessage.INSTANCE);
        TBase<?, ?> tbase = deserialize.getData();
        if (tbase == null) {
            logger.warn("tbase is null");
            return null;
        }
        if (!(tbase instanceof TCmdGetAuthenticationTokenRes)) {
            logger.warn("Invalid response : {}", tbase.getClass());
            return null;
        }
        return (TCmdGetAuthenticationTokenRes) tbase;
    }

}
