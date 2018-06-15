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

    private static String HEADER_KEY_ORG = "organization";
    private static String HEADER_VALUE_ORG = "kR";

    private static String HEADER_KEY_DATABASE_NAME = "databaseName";
    private static String HEADER_VALUE_DATABASE_NAME = "namespace";

    private static String HEADER_KEY_HBASE_NAMESPACE = "hbaseNameSpace";
    private static String HEADER_VALUE_HBASE_NAMESPACE = "namespace";

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

        Assert.assertTrue(countingDispatchHandler.checkCount( 1, 0));

        Object latestPuttedObject = countingDispatchHandler.getLatestPuttedObject();
        Assert.assertTrue(latestPuttedObject instanceof ServerRequest);
        Assert.assertEquals(tBase, ((ServerRequest) latestPuttedObject).getData());

        Header header = ((ServerRequest) latestPuttedObject).getHeader();
        Map<String, String> headerData = header.getHeaderData();
        Assert.assertEquals(headerData.get(HEADER_KEY_ORG), HEADER_VALUE_ORG);
        Assert.assertEquals(headerData.get(HEADER_KEY_DATABASE_NAME), token.getNamespace());
        Assert.assertEquals(headerData.get(HEADER_KEY_HBASE_NAMESPACE), token.getNamespace());
    }

    private ServerRequest<TBase<?, ?>> newServerRequest(TResult tBase) {
        Header header = new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, (short) 100, new HashMap<String, String>());
        return newServerRequest(header, tBase);
    }

    private ServerRequest<TBase<?, ?>> newServerRequest(Header header, TResult tBase) {
        Message<TBase<?, ?>> message = new DefaultMessage<>(header, tBase);
        return new DefaultServerRequest<>(message);
    }

    @Test
    public void dispatchSendMessageTest2() {
        CountingDispatchHandler countingDispatchHandler = new CountingDispatchHandler();

        AttachTokenDispatchHandler attachTokenDispatchHandler = new AttachTokenDispatchHandler(token, countingDispatchHandler);

        Header h = createHeader(null, "dbName", null);
        TResult tBase = new TResult();
        ServerRequest<TBase<?, ?>> request = newServerRequest(h, tBase);

        attachTokenDispatchHandler.dispatchSendMessage(request);

        Assert.assertTrue(countingDispatchHandler.checkCount(1, 0));

        Object latestPuttedObject = countingDispatchHandler.getLatestPuttedObject();
        Assert.assertTrue(latestPuttedObject instanceof ServerRequest);
        Assert.assertEquals(tBase, ((ServerRequest) latestPuttedObject).getData());

        Header header = ((ServerRequest) latestPuttedObject).getHeader();
        Map<String, String> headerData = header.getHeaderData();
        Assert.assertEquals(headerData.get(HEADER_KEY_ORG), HEADER_VALUE_ORG);
        Assert.assertNotEquals(headerData.get(HEADER_KEY_DATABASE_NAME), token.getNamespace());
        Assert.assertEquals(headerData.get(HEADER_KEY_HBASE_NAMESPACE), token.getNamespace());
    }

    @Test
    public void dispatchRequestMessageTest1() {
        CountingDispatchHandler countingDispatchHandler = new CountingDispatchHandler();

        AttachTokenDispatchHandler attachTokenDispatchHandler = new AttachTokenDispatchHandler(token, countingDispatchHandler);

        TResult tBase = new TResult();
        ServerRequest<TBase<?, ?>> request = newServerRequest(tBase);
        ServerResponse serverResponse = mock(ServerResponse.class);

        attachTokenDispatchHandler.dispatchRequestMessage(request, serverResponse);

        Assert.assertTrue(countingDispatchHandler.checkCount(0, 1));

        Object latestPuttedObject = countingDispatchHandler.getLatestPuttedObject();
        Assert.assertTrue(latestPuttedObject instanceof ServerRequest);
        Assert.assertEquals(tBase, ((ServerRequest<Object>) latestPuttedObject).getData());

        Header header = ((ServerRequest) latestPuttedObject).getHeader();
        Map<String, String> headerData = header.getHeaderData();
        Assert.assertEquals(headerData.get(HEADER_KEY_ORG), HEADER_VALUE_ORG);
        Assert.assertEquals(headerData.get(HEADER_KEY_DATABASE_NAME), token.getNamespace());
        Assert.assertEquals(headerData.get(HEADER_KEY_HBASE_NAMESPACE), token.getNamespace());
    }

    @Test
    public void dispatchRequestMessageTest2() {
        CountingDispatchHandler countingDispatchHandler = new CountingDispatchHandler();

        AttachTokenDispatchHandler attachTokenDispatchHandler = new AttachTokenDispatchHandler(token, countingDispatchHandler);

        Header headerV2 = createHeader(null, null, "hbaseName");
        TResult tBase = new TResult();
        ServerRequest serverRequest = newServerRequest(headerV2, tBase);
        ServerResponse serverResponse = mock(ServerResponse.class);

        attachTokenDispatchHandler.dispatchRequestMessage(serverRequest, serverResponse);

        Assert.assertTrue(countingDispatchHandler.checkCount( 0,  1));

        Object latestPuttedObject = countingDispatchHandler.getLatestPuttedObject();
        Assert.assertTrue(latestPuttedObject instanceof ServerRequest);
        Assert.assertEquals(tBase, ((ServerRequest) latestPuttedObject).getData());

        Header header = ((ServerRequest) latestPuttedObject).getHeader();
        Map<String, String> headerData = header.getHeaderData();
        Assert.assertEquals(headerData.get(HEADER_KEY_ORG), HEADER_VALUE_ORG);
        Assert.assertEquals(headerData.get(HEADER_KEY_DATABASE_NAME), token.getNamespace());
        Assert.assertNotEquals(headerData.get(HEADER_KEY_HBASE_NAMESPACE), token.getNamespace());
    }

    private Header createHeader(String org, String databaseName, String hbaseNamespace) {
        Map<String, String> headerData = new HashMap<>();
        if (org != null) {
            headerData.put(HEADER_KEY_ORG, org);
        }
        if (databaseName != null) {
            headerData.put(HEADER_KEY_DATABASE_NAME, databaseName);
        }
        if (hbaseNamespace != null) {
            headerData.put(HEADER_KEY_HBASE_NAMESPACE, hbaseNamespace);
        }

        return new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, (short) -1, headerData);
    }

}
