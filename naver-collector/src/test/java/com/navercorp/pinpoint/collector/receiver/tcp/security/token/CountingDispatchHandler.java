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
import com.navercorp.pinpoint.io.request.ServerRequest;
import org.apache.thrift.TBase;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Taejin Koo
 */
class CountingDispatchHandler implements DispatchHandler {

    private Object latestPuttedObject;

    private final AtomicInteger calledSendTBaseCount = new AtomicInteger();
    private final AtomicInteger calledSendServerRequestCount = new AtomicInteger();
    private final AtomicInteger calledRequestTBaseCount = new AtomicInteger();
    private final AtomicInteger calledRequestServerRequestCount = new AtomicInteger();

    @Override
    public void dispatchSendMessage(TBase<?, ?> tBase) {
        calledSendTBaseCount.incrementAndGet();
        this.latestPuttedObject = tBase;
    }

    @Override
    public void dispatchSendMessage(ServerRequest serverRequest) {
        calledSendServerRequestCount.incrementAndGet();
        this.latestPuttedObject = serverRequest;
    }

    @Override
    public TBase dispatchRequestMessage(TBase<?, ?> tBase) {
        calledRequestTBaseCount.incrementAndGet();
        this.latestPuttedObject = tBase;
        return null;
    }

    @Override
    public TBase dispatchRequestMessage(ServerRequest serverRequest) {
        calledRequestServerRequestCount.incrementAndGet();
        this.latestPuttedObject = serverRequest;
        return null;
    }

    Object getLatestPuttedObject() {
        return latestPuttedObject;
    }

    int getCalledSendTBaseCount() {
        return calledSendTBaseCount.get();
    }

    int getCalledSendServerRequestCount() {
        return calledSendServerRequestCount.get();
    }

    int getCalledRequestTBaseCount() {
        return calledRequestTBaseCount.get();
    }

    int getCalledRequestServerRequestCount() {
        return calledRequestServerRequestCount.get();
    }

    boolean checkCount(int calledSendTBaseCount, int calledSendServerRequestCount, int calledRequestTBaseCount, int calledRequestServerRequestCount) {
        if (this.calledSendTBaseCount.get() != calledSendTBaseCount) {
            return false;
        }
        if (this.calledSendServerRequestCount.get() != calledSendServerRequestCount) {
            return false;
        }
        if (this.calledRequestTBaseCount.get() != calledRequestTBaseCount) {
            return false;
        }
        if (this.calledRequestServerRequestCount.get() != calledRequestServerRequestCount) {
            return false;
        }
        return true;
    }

}
