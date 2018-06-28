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

package com.navercorp.pinpoint.collector.receiver.tcp.security.token;

import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.collector.vo.TokenType;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.io.request.DefaultMessage;
import com.navercorp.pinpoint.io.request.DefaultServerRequest;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.thrift.dto.TResult;
import org.apache.thrift.TBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * @author Taejin Koo
 */
public class AttachTokenDispatchHandlerTest {

    private Token token;

    private static String HEADER_VALUE_DATABASE_NAME = "namespace";


    @Before
    public void setUp() throws Exception {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + 3000;

        this.token = new Token("key", HEADER_VALUE_DATABASE_NAME, startTime, endTime, TokenType.SPAN);
    }

    @Test
    public void dispatchSendMessageTest1() {
        CountingDispatchHandler countingDispatchHandler = new CountingDispatchHandler();

        AttachTokenDispatchHandler attachTokenDispatchHandler = new AttachTokenDispatchHandler(token, countingDispatchHandler);

        TResult tBase = new TResult();
        ServerRequest<TBase<?, ?>> serverRequest = newServerRequest(tBase);
        attachTokenDispatchHandler.dispatchSendMessage(serverRequest);

        Assert.assertEquals(countingDispatchHandler.getCalledSendServerRequestCount(), 1);
        Assert.assertEquals(countingDispatchHandler.getCalledRequestServerRequestCount(), 0);

        ServerRequest<?> latestServerRequest = countingDispatchHandler.getLatestServerRequest();
        Assert.assertEquals(tBase, latestServerRequest.getData());
        Assert.assertNotNull(latestServerRequest.getAttribute(AttachTokenDispatchHandler.NAMESPACE_KEY));

    }

    private ServerRequest<TBase<?, ?>> newServerRequest(TResult tBase) {
        Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, (short) 100, new HashMap<String, String>());
        return newServerRequest(header, tBase);
    }

    private ServerRequest<TBase<?, ?>> newServerRequest(Header header, TResult tBase) {
        Message<TBase<?, ?>> message = new DefaultMessage<>(header, tBase);
        return new DefaultServerRequest<TBase<?, ?>>(message);
    }




}
