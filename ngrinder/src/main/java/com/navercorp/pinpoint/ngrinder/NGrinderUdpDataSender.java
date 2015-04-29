package com.navercorp.pinpoint.ngrinder;

import org.apache.thrift.TBase;

import com.navercorp.pinpoint.profiler.sender.AsyncQueueingExecutor;
import com.navercorp.pinpoint.profiler.sender.UdpDataSender;

/**
 * Copyright 2014 NAVER Corp.
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

/**
 * @author Jongho Moon
 *
 */
public class NGrinderUdpDataSender extends UdpDataSender {
    
    
    public NGrinderUdpDataSender(String host, int port) {
        super(host, port, "udp-sender", 1, SOCKET_TIMEOUT, SEND_BUFFER_SIZE);   
    }
    
    public NGrinderUdpDataSender(String host, int port, int timeout, int sendBufferSize) {
        super(host, port, "udp-sender", 1, timeout, sendBufferSize);
    }
    
    public boolean send(TBase span) {
        sendPacket(span);
        return true;
    };

    protected AsyncQueueingExecutor createAsyncQueueingExecutor(int arg0, String arg1) {
        return null;
    };
}
