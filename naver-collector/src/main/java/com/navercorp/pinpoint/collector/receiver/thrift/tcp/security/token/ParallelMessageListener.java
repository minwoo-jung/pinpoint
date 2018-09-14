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

package com.navercorp.pinpoint.collector.receiver.thrift.tcp.security.token;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.MessageListener;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;

import java.util.concurrent.Executor;

/**
 * @author Taejin Koo
 */
abstract class ParallelMessageListener implements MessageListener {

    private final Executor executor;

    ParallelMessageListener(Executor executor) {
        this.executor = Assert.requireNonNull(executor, "executor must not be null");
    }

    @Override
    public final void handleSend(final SendPacket sendPacket, final PinpointSocket pinpointSocket) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                handleSend0(sendPacket, pinpointSocket);
            }
        });
    }

    protected abstract void handleSend0(SendPacket sendPacket, PinpointSocket pinpointSocket);

    @Override
    public final void handleRequest(final RequestPacket requestPacket, final PinpointSocket pinpointSocket) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                handleRequest0(requestPacket, pinpointSocket);
            }
        });
    }

    protected abstract void handleRequest0(RequestPacket requestPacket, PinpointSocket pinpointSocket);

}
