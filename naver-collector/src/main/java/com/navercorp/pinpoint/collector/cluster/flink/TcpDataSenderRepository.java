/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.collector.cluster.flink;

import com.navercorp.pinpoint.collector.service.SendAgentStatService;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author minwoo.jung
 */
public class TcpDataSenderRepository {
    private final ConcurrentHashMap<SocketAddress, TcpDataSender> clusterConnectionRepository = new ConcurrentHashMap<>();
    private final SendAgentStatService sendAgentStatService;

    TcpDataSenderRepository(SendAgentStatService sendAgentStatService) {
        this.sendAgentStatService = sendAgentStatService;
    }

    public TcpDataSender putIfAbsent(SocketAddress address, TcpDataSender tcpDataSender) {
        TcpDataSender result =  clusterConnectionRepository.putIfAbsent(address, tcpDataSender);
        replaceDataInsendAgentStatService();
        return result;
    }

    public TcpDataSender remove(SocketAddress address) {
        TcpDataSender tcpDataSender = clusterConnectionRepository.remove(address);
        if (tcpDataSender != null) {
            tcpDataSender.stop();
        }
        replaceDataInsendAgentStatService();
        return tcpDataSender;
    }

    private void replaceDataInsendAgentStatService() {
        sendAgentStatService.replaceFlinkServerList(getClusterSocketList());
    }

    public boolean containsKey(SocketAddress address) {
        return clusterConnectionRepository.containsKey(address);
    }

    public List<SocketAddress> getAddressList() {
        // fix jdk 8 KeySetView compatibility
        Set<SocketAddress> socketAddresses = clusterConnectionRepository.keySet();
        return new ArrayList<>(socketAddresses);
    }

    public List<TcpDataSender> getClusterSocketList() {
        return new ArrayList<>(clusterConnectionRepository.values());
    }
}
