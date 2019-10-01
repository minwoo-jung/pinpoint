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

package com.navercorp.pinpoint.collector.dao.memory;

import com.navercorp.pinpoint.collector.dao.TokenDao;
import com.navercorp.pinpoint.collector.vo.Token;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Taejin Koo
 */
@Repository
@Profile("tokenAuthentication")
public class MemoryTokenDao implements TokenDao {

    private final ConcurrentHashMap<String, Token> tokenMap = new ConcurrentHashMap<>();

    @Override
    public boolean create(Token token) {
        Token oldValue = tokenMap.putIfAbsent(token.getKey(), token);
        return oldValue == null;
    }

    @Override
    public Token getAndRemove(String tokenKey) {
        try {
            return tokenMap.remove(tokenKey);
        } catch (Exception e) {
            // throws exception if tokenKey is not exist
        }
        return null;
    }

}
