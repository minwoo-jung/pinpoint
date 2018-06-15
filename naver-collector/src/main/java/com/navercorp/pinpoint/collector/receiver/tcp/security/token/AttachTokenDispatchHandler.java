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

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.vo.Token;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.MapUtils;
import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.v2.HeaderV2;
import com.navercorp.pinpoint.io.request.DefaultMessage;
import com.navercorp.pinpoint.io.request.DefaultServerRequest;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import org.apache.thrift.TBase;
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


    AttachTokenDispatchHandler(Token token, DispatchHandler dispatchHandler) {
        Assert.requireNonNull(token, "token must not be null");
        this.dispatchHandler = Assert.requireNonNull(dispatchHandler, "dispatchHandler must not be null");

        // need to change it later.
        Map<String, String> headerData = new HashMap<>();
        headerData.put("organization", "kR");
        headerData.put("databaseName", token.getNamespace());
        headerData.put("hbaseNameSpace", token.getNamespace());

        this.headerData = Collections.unmodifiableMap(headerData);
    }

    @Override
    public void dispatchSendMessage(ServerRequest serverRequest) {
        final Object data = serverRequest.getData();
        if (data instanceof TBase<?, ?>) {
            Header serverRequestHeader = serverRequest.getHeader();

            Map<String, String> mergedHeadData = createHeaderData(serverRequestHeader.getHeaderData(), headerData);
            Header header = createHeader(serverRequestHeader.getType(), mergedHeadData);

            Message<TBase<?, ?>> message = new DefaultMessage<>(header, (TBase<?, ?>) data);
            ServerRequest<TBase<?, ?>> copyRequest = new DefaultServerRequest<>(message);

            dispatchHandler.dispatchSendMessage(copyRequest);
        } else {
            dispatchHandler.dispatchSendMessage(serverRequest);
        }

    }


    @Override
    public void dispatchRequestMessage(ServerRequest serverRequest, ServerResponse serverResponse) {
        final Object data = serverRequest.getData();
        if (data instanceof TBase<?, ?>) {
            Header serverRequestHeader = serverRequest.getHeader();

            Map<String, String> mergedHeadData = createHeaderData(serverRequestHeader.getHeaderData(), headerData);
            Header header = createHeader(serverRequestHeader.getType(), mergedHeadData);
            Message<TBase<?, ?>> message = new DefaultMessage<>(header, (TBase<?, ?>) data);
            ServerRequest<TBase<?, ?>> copyRequest = new DefaultServerRequest<>(message);
            dispatchHandler.dispatchRequestMessage(copyRequest, serverResponse);
        } else {
            dispatchHandler.dispatchRequestMessage(serverRequest, serverResponse);
        }
    }



    private Header createHeader(short type, Map<String, String> headerData) {
        return new HeaderV2(Header.SIGNATURE, HeaderV2.VERSION, type, headerData);
    }

    private Map<String, String> createHeaderData(Map<String, String> headerData1, Map<String, String> headerData2) {
        final int initialSize = MapUtils.nullSafeSize(headerData1) + MapUtils.nullSafeSize(headerData2);

        Map<String, String> headerData = new HashMap<>(initialSize);
        addAll(headerData, headerData1);
        addAll(headerData, headerData2);

        return headerData;
    }

    private void addAll(Map<String, String> headerData, Map<String, String> append) {
        if (append == null) {
            return;
        }
        for (Map.Entry<String, String> entry : append.entrySet()) {
            headerData.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    private int getLength(Map<String, String> headerData1) {
        if (MapUtils.isEmpty(headerData1)) {
            return 0;
        }
        return headerData1.size();
    }

}
