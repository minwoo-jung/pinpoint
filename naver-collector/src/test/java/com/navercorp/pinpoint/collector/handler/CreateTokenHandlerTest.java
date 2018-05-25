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

package com.navercorp.pinpoint.collector.handler;

import com.navercorp.pinpoint.collector.dao.NameSpaceDao;
import com.navercorp.pinpoint.collector.service.NameSpaceService;
import com.navercorp.pinpoint.collector.service.TokenConfig;
import com.navercorp.pinpoint.collector.service.TokenService;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TCmdGetAuthenticationToken;
import com.navercorp.pinpoint.thrift.dto.command.TCmdGetAuthenticationTokenRes;
import com.navercorp.pinpoint.thrift.dto.command.TTokenResponseCode;
import com.navercorp.pinpoint.thrift.dto.command.TTokenType;
import org.apache.thrift.TBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Taejin Koo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-tokenauth-test.xml")
@ActiveProfiles("tokenAuthentication")
public class CreateTokenHandlerTest {

    private static final String USER = "user";
    private static final String PASSWORD = "password";
    private static final String NAMESPACE = "namespace";

    @Autowired
    private CreateTokenHandler createTokenHandler;

    @Autowired
    private TokenConfig tokenConfig;

    @Autowired
    private NameSpaceDao nameSpaceDao;

    @Before
    public void setUp() throws Exception {
        nameSpaceDao.create(USER, NAMESPACE);
    }

    @Test
    public void tokenHandlerTsst() {
        TCmdGetAuthenticationToken tokenRequest = new TCmdGetAuthenticationToken();
        tokenRequest.setUserId(USER);
        tokenRequest.setPassword(PASSWORD);
        tokenRequest.setTokenType(TTokenType.SPAN);

        TCmdGetAuthenticationTokenRes tokenResponse = (TCmdGetAuthenticationTokenRes) createTokenHandler.handleRequest(tokenRequest);

        Assert.assertEquals(TTokenResponseCode.OK, tokenResponse.getCode());
    }

    @Test
    public void getNameSpaceFailTest() {
        TCmdGetAuthenticationToken tokenRequest = new TCmdGetAuthenticationToken();
        tokenRequest.setUserId(USER + "fail");
        tokenRequest.setPassword(PASSWORD);
        tokenRequest.setTokenType(TTokenType.SPAN);

        TCmdGetAuthenticationTokenRes tokenResponse = (TCmdGetAuthenticationTokenRes) createTokenHandler.handleRequest(tokenRequest);
        Assert.assertEquals(TTokenResponseCode.INTERNAL_SERVER_ERROR, tokenResponse.getCode());
    }

    @Test
    public void badRequestTest() {
        TCmdGetAuthenticationTokenRes tokenResponse = (TCmdGetAuthenticationTokenRes) createTokenHandler.handleRequest(new TResult());
        Assert.assertEquals(TTokenResponseCode.BAD_REQUEST, tokenResponse.getCode());
    }

}
