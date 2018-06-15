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

package com.navercorp.pinpoint.thrift.io;

import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.v1.HeaderV1;
import com.navercorp.pinpoint.thrift.dto.command.TCmdAuthenticationToken;
import com.navercorp.pinpoint.thrift.dto.command.TCmdAuthenticationTokenRes;
import com.navercorp.pinpoint.thrift.dto.command.TCmdGetAuthenticationToken;
import com.navercorp.pinpoint.thrift.dto.command.TCmdGetAuthenticationTokenRes;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

/**
 * @author Taejin Koo
 */
public class AuthenticationTBaseLocator implements TBaseLocator {

    private final DefaultTBaseLocator defaultTBaseLocator = new DefaultTBaseLocator();

    public static final short GET_AUTHENTICATION_TOKEN = 760;
    private static final Header GET_AUTHENTICATION_TOKEN_HEADER = createHeader(GET_AUTHENTICATION_TOKEN);

    public static final short GET_AUTHENTICATION_TOKEN_RESPONSE = 761;
    private static final Header GET_AUTHENTICATION_TOKEN_RESPONSE_HEADER = createHeader(GET_AUTHENTICATION_TOKEN_RESPONSE);

    private static final short AUTHENTICATION_TOKEN = 762;
    private static final Header AUTHENTICATION_TOKEN_HEADER = createHeader(AUTHENTICATION_TOKEN);

    private static final short AUTHENTICATION_TOKEN_RESPONSE = 763;
    private static final Header AUTHENTICATION_TOKEN_RESPONSE_HEADER = createHeader(AUTHENTICATION_TOKEN_RESPONSE);

    private static Header createHeader(short type) {
        return new HeaderV1(type);
    }

    @Override
    public TBase<?, ?> tBaseLookup(short type) throws TException {
        switch (type) {
            case GET_AUTHENTICATION_TOKEN:
                return new TCmdGetAuthenticationToken();
            case GET_AUTHENTICATION_TOKEN_RESPONSE:
                return new TCmdGetAuthenticationTokenRes();
            case AUTHENTICATION_TOKEN:
                return new TCmdAuthenticationToken();
            case AUTHENTICATION_TOKEN_RESPONSE:
                return new TCmdAuthenticationTokenRes();
        }

        return defaultTBaseLocator.tBaseLookup(type);
    }

    public Header headerLookup(TBase<?, ?> tbase) throws TException {
        if (tbase == null) {
            throw new IllegalArgumentException("tbase must not be null");
        }
        if (tbase instanceof TCmdGetAuthenticationToken) {
            return GET_AUTHENTICATION_TOKEN_HEADER;
        }
        if (tbase instanceof TCmdGetAuthenticationTokenRes) {
            return GET_AUTHENTICATION_TOKEN_RESPONSE_HEADER;
        }
        if (tbase instanceof TCmdAuthenticationToken) {
            return AUTHENTICATION_TOKEN_HEADER;
        }
        if (tbase instanceof TCmdAuthenticationTokenRes) {
            return AUTHENTICATION_TOKEN_RESPONSE_HEADER;
        }

        return defaultTBaseLocator.headerLookup(tbase);
    }

    @Override
    public boolean isSupport(short type) {
        try {
            tBaseLookup(type);
            return true;
        } catch (TException ignore) {
            // skip
        }

        return defaultTBaseLocator.isSupport(type);
    }

    @Override
    public boolean isSupport(Class<? extends TBase> clazz) {
        if (clazz.equals(TCmdGetAuthenticationToken.class)) {
            return true;
        }
        if (clazz.equals(TCmdGetAuthenticationTokenRes.class)) {
            return true;
        }
        if (clazz.equals(TCmdAuthenticationToken.class)) {
            return true;
        }
        if (clazz.equals(TCmdAuthenticationTokenRes.class)) {
            return true;
        }

        return defaultTBaseLocator.isSupport(clazz);
    }

    @Override
    public Header getChunkHeader() {
        return defaultTBaseLocator.getChunkHeader();
    }

    @Override
    public boolean isChunkHeader(short type) {
        return defaultTBaseLocator.isChunkHeader(type);
    }

}
