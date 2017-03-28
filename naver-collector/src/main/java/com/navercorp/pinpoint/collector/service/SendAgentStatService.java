/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.config.NaverCollectorConfiguration;
import com.navercorp.pinpoint.collector.dao.AgentStatDaoV2;
import com.navercorp.pinpoint.collector.handler.AgentStatHandlerV2;
import com.navercorp.pinpoint.collector.mapper.thrift.stat.AgentStatBatchMapper;
import com.navercorp.pinpoint.collector.mapper.thrift.stat.AgentStatMapper;
import com.navercorp.pinpoint.common.server.bo.stat.*;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import org.apache.commons.collections.iterators.ArrayListIterator;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author minwoo.jung
 */
public class SendAgentStatService implements AgentStatService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean flinkClusterEnable;

    private volatile List<TcpDataSender> flinkServerList = new CopyOnWriteArrayList();
    private AtomicInteger callCount = new AtomicInteger(1);

    public SendAgentStatService(NaverCollectorConfiguration config) {
        this.flinkClusterEnable = config.isFlinkClusterEnable();
    }

    @Override
    public void save(TBase<?, ?> tbase) {
        if (!flinkClusterEnable) {
            return;
        }

        TcpDataSender tcpDataSender = roundRobinTcpDataSender();

        if (tcpDataSender == null) {
            logger.warn("not send flink server. Because TcpDataSender is null");
            return;
        }
        if (tbase instanceof TAgentStatBatch) {
            logger.info("send to flinkserver : " + tbase);
            tcpDataSender.send(tbase);
        } else {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + TAgentStat.class.getName() + " or " + TAgentStatBatch.class.getName());
        }
    }

    public TcpDataSender roundRobinTcpDataSender() {
        if (flinkServerList.size() == 0) {
            return null;
        }

        int count = callCount.getAndIncrement();
        int tcpDataSenderIndex = count % flinkServerList.size();

        if (tcpDataSenderIndex < 0) {
            tcpDataSenderIndex = tcpDataSenderIndex * -1;
            callCount.set(0);
        }

        try {
            return flinkServerList.get(tcpDataSenderIndex);
        } catch (Exception e) {
            logger.warn("not get TcpDataSender", e);
        }

        return null;
    }

    public void replaceFlinkServerList(List<TcpDataSender> flinkServerList) {
        this.flinkServerList = new CopyOnWriteArrayList(flinkServerList);
    }
}
