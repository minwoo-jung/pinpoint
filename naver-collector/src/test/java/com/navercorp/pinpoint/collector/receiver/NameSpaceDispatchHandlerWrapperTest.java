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

package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.collector.namespace.RequestContextHolder;
import com.navercorp.pinpoint.collector.receiver.thrift.DispatchHandler;
import com.navercorp.pinpoint.io.request.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class NameSpaceDispatchHandlerWrapperTest {

    @Test
    public void testInterceptor() {
        NameSpaceDispatchHandlerWrapper nameSpaceDispatchHandlerWrapper = new NameSpaceDispatchHandlerWrapper(true, new TestDispatchHandler());
        ServerRequest serverRequest = new DefaultServerRequest(new EmptyMessage(), "127.0.0.1", 80);

        assertNull(RequestContextHolder.getAttributes());
        nameSpaceDispatchHandlerWrapper.dispatchSendMessage(serverRequest);
        assertNull(RequestContextHolder.getAttributes());

        assertNull(RequestContextHolder.getAttributes());
        nameSpaceDispatchHandlerWrapper.dispatchRequestMessage(serverRequest, new EmptyServerResponse());
        assertNull(RequestContextHolder.getAttributes());
    }

    private class TestDispatchHandler implements DispatchHandler {

        @Override
        public void dispatchSendMessage(ServerRequest serverRequest) {
        }

        @Override
        public void dispatchRequestMessage(ServerRequest serverRequest, ServerResponse serverResponse) {
        }
    }

    private class EmptyServerResponse implements ServerResponse {

        @Override
        public void write(Object message) {
        }
    }

}