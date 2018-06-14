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

package com.navercorp.pinpoint.collector.receiver.tcp.security.token;

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.thrift.dto.ThriftRequest;
import com.navercorp.pinpoint.thrift.io.TBaseLocator;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
class AttachTokenDispatchHandler implements DispatchHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, String> headerData;
    private final DispatchHandler dispatchHandler;
    private final TBaseLocator tBaseLocator;

    AttachTokenDispatchHandler(Token token, DispatchHandler dispatchHandler, TBaseLocator tBaseLocator) {
        Assert.requireNonNull(token, "token must not be null");
        this.dispatchHandler = Assert.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
        this.tBaseLocator = Assert.requireNonNull(tBaseLocator, "tBaseLocator must not be null");

        // need to change it later.
        Map<String, String> headerData = new HashMap<>();
        headerData.put("organization", "kR");
        headerData.put("databaseName", token.getNamespace());
        headerData.put("hbaseNameSpace", token.getNamespace());

        this.headerData = Collections.unmodifiableMap(headerData);
    }

    @Override
    public void dispatchSendMessage(TBase<?, ?> tBase) {
        Header header = createHeader(getType(tBase), headerData);
        ThriftRequest serverRequest = new ThriftRequest(header, tBase);

        dispatchHandler.dispatchSendMessage(serverRequest);
    }

    private short getType(TBase<?, ?> tBase) {
        try {
            Header header = tBaseLocator.headerLookup(tBase);
            return header.getType();
        } catch (TException e) {
            logger.warn("can't find type. tBase:{}", tBase);
        }
        return -1;
    }

    @Override
    public void dispatchSendMessage(ServerRequest serverRequest) {
        if (serverRequest instanceof ThriftRequest) {
            Header serverRequestHeader = serverRequest.getHeader();

            Map<String, String> mergedHeadData = createHeaderData(serverRequestHeader.getHeaderData(), headerData);
            Header header = createHeader(serverRequestHeader.getType(), mergedHeadData);

            dispatchHandler.dispatchSendMessage(new ThriftRequest(header, (TBase<?, ?>) serverRequest.getData()));
        } else {
            dispatchHandler.dispatchSendMessage(serverRequest);
        }
    }

    @Override
    public TBase dispatchRequestMessage(TBase<?, ?> tBase) {
        Header header = createHeader(getType(tBase), headerData);
        ThriftRequest serverRequest = new ThriftRequest(header, tBase);

        return dispatchHandler.dispatchRequestMessage(serverRequest);
    }

    @Override
    public TBase dispatchRequestMessage(ServerRequest serverRequest) {
        if (serverRequest instanceof ThriftRequest) {
            Header serverRequestHeader = serverRequest.getHeader();

            Map<String, String> mergedHeadData = createHeaderData(serverRequestHeader.getHeaderData(), headerData);
            Header header = createHeader(serverRequestHeader.getType(), mergedHeadData);

            return dispatchHandler.dispatchRequestMessage(new ThriftRequest(header, (TBase<?, ?>) serverRequest.getData()));
        } else {
            return dispatchHandler.dispatchRequestMessage(serverRequest);
        }
    }


    private Header createHeader(Map<String, String> headerData) {
        return createHeader((short) -1, headerData);
    }

    private Header createHeader(short type, Map<String, String> headerData) {
        return new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, type, headerData);
    }

    private Map<String, String> createHeaderData(Map<String, String> headerData1, Map<String, String> headerData2) {
        int initialSize = 0;
        if (headerData1 != null) {
            initialSize += headerData1.size();
        }
        if (headerData2 != null) {
            initialSize += headerData2.size();
        }

        Map<String, String> headerData = new HashMap<>(initialSize);
        if (headerData1 != null) {
            for (Map.Entry<String, String> entry : headerData1.entrySet()) {
                headerData.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        if (headerData2 != null) {
            for (Map.Entry<String, String> entry : headerData2.entrySet()) {
                headerData.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }

        return headerData;
    }

}
