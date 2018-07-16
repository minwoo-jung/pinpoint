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

package com.navercorp.pinpoint.plugin.bloc;

import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestWrapper;
import com.navercorp.pinpoint.plugin.bloc.v4.NimmServerSocketAddressAccessor;
import com.nhncorp.lucy.npc.NpcMessage;

import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class NimmRequest implements ServerRequestWrapper {


    private final Object target;
    private final NpcMessage npcMessage;
    private final String remoteAddress;

    private Map<String, String> pinpointOptions;

    public NimmRequest(Object target, NpcMessage npcMessage, String remoteAddress) {
        this.target = target;
        this.npcMessage = npcMessage;
        this.remoteAddress = remoteAddress;
    }

    @Override
    public String getHeader(String name) {
        if (pinpointOptions == null) {
           this.pinpointOptions = LucyNetUtils.getPinpointOptions(npcMessage);
        }
        return pinpointOptions.get(name);
    }

    @Override
    public String getRpcName() {
        final String rpcName = LucyNetUtils.getRpcName(npcMessage);
        return rpcName;

    }

    @Override
    public String getEndPoint() {
        String dstAddress = BlocConstants.UNKNOWN_ADDRESS;
        if (target instanceof NimmServerSocketAddressAccessor) {
            dstAddress = ((NimmServerSocketAddressAccessor) target)._$PINPOINT$_getNimmAddress();
        }
        return dstAddress;
    }

    @Override
    public String getRemoteAddress() {
        return remoteAddress;
    }


    @Override
    public String getAcceptorHost() {
        return getEndPoint();
    }
}
