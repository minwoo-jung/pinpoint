/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.handler.thrift;

import com.navercorp.pinpoint.collector.dao.MetadataDao;
import com.navercorp.pinpoint.collector.dao.memory.MemoryMetadataDao;
import com.navercorp.pinpoint.collector.service.TokenConfig;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.collector.vo.PaaSOrganizationKey;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TCmdGetAuthenticationToken;
import com.navercorp.pinpoint.thrift.dto.command.TCmdGetAuthenticationTokenRes;
import com.navercorp.pinpoint.thrift.dto.command.TTokenResponseCode;
import com.navercorp.pinpoint.thrift.dto.command.TTokenType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

/**
 * @author Taejin Koo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-tokenauth-test.xml")
@ActiveProfiles("tokenAuthentication")
public class ThriftCreateTokenHandlerTest {

    private static final String LICENSE_KEY = UUID.randomUUID().toString();
    private static final String ORGANIZATION = "org";
    private static final String NAMESPACE = "namespace";
    private static final String REMOTE_ADDRESS = "127.0.0.1";

    @Autowired
    private ThriftCreateTokenHandler createTokenHandler;

    @Autowired
    private TokenConfig tokenConfig;

    @Autowired
    private MetadataDao metadataDao;

    @Before
    public void setUp() throws Exception {
        if (metadataDao instanceof MemoryMetadataDao) {
            PaaSOrganizationKey paaSOrganizationKey = new PaaSOrganizationKey(LICENSE_KEY, ORGANIZATION);
            ((MemoryMetadataDao) metadataDao).createPaaSOrganizationkey(LICENSE_KEY, paaSOrganizationKey);

            PaaSOrganizationInfo paaSOrganizationInfo = new PaaSOrganizationInfo(ORGANIZATION, NAMESPACE, NAMESPACE);
            ((MemoryMetadataDao) metadataDao).createPaaSOrganizationInfo(ORGANIZATION, paaSOrganizationInfo);
        }
    }

    @Test
    public void tokenHandlerTest() {
        TCmdGetAuthenticationToken tokenRequest = new TCmdGetAuthenticationToken();
        tokenRequest.setLicenseKey(LICENSE_KEY);
        tokenRequest.setTokenType(TTokenType.SPAN);

        TCmdGetAuthenticationTokenRes tokenResponse = (TCmdGetAuthenticationTokenRes) createTokenHandler.handleRequest(tokenRequest, REMOTE_ADDRESS);

        Assert.assertEquals(TTokenResponseCode.OK, tokenResponse.getCode());
    }

    @Test
    public void getNameSpaceFailTest() {
        TCmdGetAuthenticationToken tokenRequest = new TCmdGetAuthenticationToken();
        tokenRequest.setLicenseKey(LICENSE_KEY + "fail");
        tokenRequest.setTokenType(TTokenType.SPAN);

        TCmdGetAuthenticationTokenRes tokenResponse = (TCmdGetAuthenticationTokenRes) createTokenHandler.handleRequest(tokenRequest, REMOTE_ADDRESS);
        Assert.assertEquals(TTokenResponseCode.UNAUTHORIZED, tokenResponse.getCode());
    }

    @Test
    public void badRequestTest() {
        TCmdGetAuthenticationTokenRes tokenResponse = (TCmdGetAuthenticationTokenRes) createTokenHandler.handleRequest(new TResult(), REMOTE_ADDRESS);
        Assert.assertEquals(TTokenResponseCode.BAD_REQUEST, tokenResponse.getCode());
    }

}
