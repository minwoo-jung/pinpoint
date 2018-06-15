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
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import org.apache.thrift.TBase;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Taejin Koo
 */
class CountingDispatchHandler implements DispatchHandler {

    private Object latestPuttedObject;


    private final AtomicInteger calledSendServerRequestCount = new AtomicInteger();

    private final AtomicInteger calledRequestServerRequestCount = new AtomicInteger();


    @Override
    public void dispatchSendMessage(ServerRequest serverRequest) {
        calledSendServerRequestCount.incrementAndGet();
        this.latestPuttedObject = serverRequest;
    }

    @Override
    public void dispatchRequestMessage(ServerRequest serverRequest, ServerResponse response) {
        calledRequestServerRequestCount.incrementAndGet();
        this.latestPuttedObject = serverRequest;

    }

    Object getLatestPuttedObject() {
        return latestPuttedObject;
    }

    int getCalledSendServerRequestCount() {
        return calledSendServerRequestCount.get();
    }


    int getCalledRequestServerRequestCount() {
        return calledRequestServerRequestCount.get();
    }

    boolean checkCount(int calledSendServerRequestCount, int calledRequestServerRequestCount) {

        if (this.calledSendServerRequestCount.get() != calledSendServerRequestCount) {
            return false;
        }

        if (this.calledRequestServerRequestCount.get() != calledRequestServerRequestCount) {
            return false;
        }
        return true;
    }

}
