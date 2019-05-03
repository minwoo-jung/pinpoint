/*
 * Copyright 2019 NAVER Corp.
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

import com.navercorp.pinpoint.collector.config.SpanStatConfiguration;
import com.navercorp.pinpoint.collector.interceptor.HbaseTraceDaoV2Interceptor;
import com.navercorp.pinpoint.collector.interceptor.SpanStatData.SpanStatKey;
import com.navercorp.pinpoint.collector.mapper.thrift.span.TFSpanStatMapper;
import com.navercorp.pinpoint.collector.namespace.NameSpaceInfo;
import com.navercorp.pinpoint.collector.namespace.RequestAttributes;
import com.navercorp.pinpoint.collector.namespace.RequestContextHolder;
import com.navercorp.pinpoint.thrift.dto.flink.TFSpanStatBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author minwoo.jung
 */
public class SendSpanStatService extends SendDataToFlinkService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final int PARTITION_SIZE = 100;
    private final TFSpanStatMapper tFSpanStatMapper = new TFSpanStatMapper();
    private final HbaseTraceDaoV2Interceptor hbaseTraceDaoV2Interceptor;
    private final boolean flinkClusterEnable;

    public SendSpanStatService(SpanStatConfiguration spanStatConfiguration, HbaseTraceDaoV2Interceptor hbaseTraceDaoV2Interceptor) {
        if (Objects.isNull(hbaseTraceDaoV2Interceptor)) {
            throw new IllegalArgumentException("hbaseTraceDaoV2Interceptor must not be null.");
        }
        this.hbaseTraceDaoV2Interceptor = hbaseTraceDaoV2Interceptor;

        this.flinkClusterEnable = spanStatConfiguration.isFlinkClusterEnable();
    }

    public void send() {
        if (!flinkClusterEnable) {
            return;
        }

        logger.info("start sending span stat.");
        try {
            sendSpanStat();
        } catch (Exception e) {
            logger.error("occur exception while sending span data", e);
        } finally {
            logger.info("end sending span stat.");
        }
    }

    private void sendSpanStat() {
        setNameSpace();
        final Map<SpanStatKey, Long> spanStatData = hbaseTraceDaoV2Interceptor.getSnapshotAndEmptySpanStatData();
        List<Map.Entry<SpanStatKey, Long>> partition = new ArrayList<Map.Entry<SpanStatKey, Long>>(PARTITION_SIZE);
        int i = 0;

        for (Map.Entry<SpanStatKey, Long> entry : spanStatData.entrySet()) {
            partition.add(entry);
            i++;

            if (i % PARTITION_SIZE == 0) {
                TFSpanStatBatch tFSpanStatBatch = tFSpanStatMapper.map(partition);
                partition = new ArrayList<Map.Entry<SpanStatKey, Long>>(PARTITION_SIZE);
                sendData(tFSpanStatBatch);
                if (logger.isDebugEnabled()) {
                    logger.debug("span stat : " + tFSpanStatBatch);
                }
            }
        }

        if (partition.size() > 0) {
            TFSpanStatBatch tFSpanStatBatch = tFSpanStatMapper.map(partition);
            sendData(tFSpanStatBatch);
            if (logger.isDebugEnabled()) {
                logger.debug("span stat : " + tFSpanStatBatch);
            }
        }
    }

    private void setNameSpace() {
        RequestAttributes requestAttributes = new RequestAttributes(new ConcurrentHashMap<>());
        requestAttributes.setAttribute(NameSpaceInfo.NAMESPACE_INFO, NameSpaceInfo.DEFAULT);
        RequestContextHolder.setAttributes(requestAttributes);
    }
}
