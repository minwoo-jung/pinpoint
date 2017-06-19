/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.lucy.net;

import com.navercorp.pinpoint.common.plugin.util.HostAndPort;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class EndPointUtils {

    private EndPointUtils() {
    }

    public static String getEndPoint(InetSocketAddress socketAddress) {
        final String hostName = getHostAddress(socketAddress);
        final int port = HostAndPort.getPortOrNoPort(socketAddress.getPort());
        return HostAndPort.toHostAndPortString(hostName, port);
    }

    private static String getHostAddress(InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) {
            return null;
        }
        // TODO JDK 1.7 InetSocketAddress.getHostString();
        // Warning : Avoid unnecessary DNS lookup  (warning:InetSocketAddress.getHostName())
        final InetAddress inetAddress = inetSocketAddress.getAddress();
        if (inetAddress == null) {
            return null;
        }
        return inetAddress.getHostAddress();
    }

}
