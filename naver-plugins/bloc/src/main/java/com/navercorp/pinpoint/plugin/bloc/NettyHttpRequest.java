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
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.bloc.v4.interceptor.MessageReceivedInterceptor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author Woonduk Kang(emeroad)
 */
public class NettyHttpRequest implements ServerRequestWrapper {
    private final ChannelHandlerContext ctx;
    private final FullHttpRequest request;

    public NettyHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        this.ctx = ctx;
        this.request = request;
    }


    @Override
    public String getHeader(String name) {
        final HttpHeaders headers = request.headers();
        return headers.get(name);
    }

    @Override
    public String getRpcName() {
        final String rpcName = request.getUri();
        return rpcName;
    }

    @Override
    public String getEndPoint() {
        final String endPoint = getIpPort(ctx.channel().localAddress());
        return endPoint;
    }

    @Override
    public String getRemoteAddress() {
        final String remoteAddress = getIp(ctx.channel().remoteAddress());
        return remoteAddress;
    }

    @Override
    public String getAcceptorHost() {
        return getEndPoint();
    }

    private String getIpPort(SocketAddress socketAddress) {
        String address = socketAddress.toString();

        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            return HostAndPort.toHostAndPortString(inetSocketAddress.getAddress().getHostAddress(), inetSocketAddress.getPort());
        }

        if (address.startsWith("/")) {
            return address.substring(1);
        } else {
            if (address.contains("/")) {
                return address.substring(address.indexOf('/') + 1);
            } else {
                return address;
            }
        }
    }

    private String getIp(SocketAddress socketAddress) {
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            return inetSocketAddress.getAddress().getHostAddress();
        } else {
            return "NOT_SUPPORTED_ADDRESS";
        }
    }

}
