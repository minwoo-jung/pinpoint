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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.TokenDao;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.collector.vo.TokenCreateRequest;
import com.navercorp.pinpoint.collector.vo.TokenType;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
@Service("tokenService")
@Profile("tokenAuthentication")
public class TokenServiceImpl implements TokenService {

    @Autowired
    private TokenConfig tokenConfig;

    @Autowired
    private NameSpaceService nameSpaceService;

    @Autowired
    private TokenDao tokenDao;

    private Timer timer;

    @PostConstruct
    public void setup() {
        this.timer = createTimer();
    }

    private Timer createTimer() {
        String clazzName = this.getClass().getSimpleName();
        HashedWheelTimer timer = TimerFactory.createHashedWheelTimer(clazzName + "-Timer", 100, TimeUnit.MILLISECONDS, 512);
        timer.start();
        return timer;
    }

    @PreDestroy
    public void teardown() {
        if (timer != null) {
            timer.stop();
        }
    }


    @Override
    public Token create(TokenCreateRequest request) {
        String nameSpace = nameSpaceService.getNameSpace(request.getUserId());
        if (nameSpace == null) {
            throw new TokenException("Can't find nameSpace");
        }

        for (int i = 0; i < tokenConfig.getMaxRetryCount(); i++) {
            Token token = createToken(nameSpace, request.getTokenType());
            if (tokenDao.create(token)) {

                ExpiredTokenTask expiredTokenTask = new ExpiredTokenTask(token.getKey());
                timer.newTimeout(expiredTokenTask, tokenConfig.getTtl(), TimeUnit.MILLISECONDS);

                return token;
            }
        }
        return null;
    }

    private Token createToken(String namespace, TokenType tokenType) {
        String tokenKey = UUID.randomUUID().toString();

        long currentTime = System.currentTimeMillis();
        long expiryTime = currentTime + tokenConfig.getTtl();

        return new Token(tokenKey, namespace, currentTime, expiryTime, tokenType);
    }

    @Override
    public Token getAndRemove(String tokenKey, TokenType tokenType) {
        return tokenDao.getAndRemove(tokenKey);
    }

    private class ExpiredTokenTask implements TimerTask {

        private final String tokenKey;

        public ExpiredTokenTask(String tokenKey) {
            this.tokenKey = Assert.requireNonNull(tokenKey, "tokenKey must not be null");
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            if (timeout.isCancelled()) {
                return;
            }

            tokenDao.getAndRemove(tokenKey);
        }

    }


}
