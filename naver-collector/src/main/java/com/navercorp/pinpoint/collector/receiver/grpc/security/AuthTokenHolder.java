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

package com.navercorp.pinpoint.collector.receiver.grpc.security;

import com.navercorp.pinpoint.collector.vo.Token;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class AuthTokenHolder {

    private final Token token;

    public AuthTokenHolder(Token token) {
        Objects.requireNonNull(token, "token");
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AuthTokenHolder{");
        sb.append("token='").append(token).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
