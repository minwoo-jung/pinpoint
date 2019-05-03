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
package com.navercorp.pinpoint.collector.interceptor;

import com.navercorp.pinpoint.collector.config.SpanStatConfiguration;
import com.navercorp.pinpoint.collector.interceptor.SpanStatData.SpanStatKey;
import com.navercorp.pinpoint.collector.namespace.NameSpaceInfo;
import com.navercorp.pinpoint.collector.namespace.RequestContextHolder;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.util.DefaultTimeSlot;
import com.navercorp.pinpoint.common.util.TimeSlot;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author minwoo.jung
 */
public class HbaseTraceDaoV2Interceptor {

    private final static TimeSlot timeSlot = new DefaultTimeSlot(60000);

    private final Logger logger = LoggerFactory.getLogger(HbaseTraceDaoV2Interceptor.class);
    private final SpanStatData spanStatData;
    private final boolean flinkClusterEnable;

    public HbaseTraceDaoV2Interceptor(SpanStatConfiguration config) {
        this.flinkClusterEnable = config.isFlinkClusterEnable();
        this.spanStatData = new SpanStatData();
    }

    public boolean aroundIntercept(ProceedingJoinPoint joinPoint, SpanBo spanBo) throws Throwable {
        boolean insertSuccess = (boolean) joinPoint.proceed();

        if (!flinkClusterEnable) {
            return insertSuccess;
        }

        if (insertSuccess) {
            incrementSpanCount(spanBo);
        }

        return insertSuccess;
    }


    public void incrementSpanCount(SpanBo spanBo) {
        try {
            final String applicationId = spanBo.getApplicationId();
            final String agentId = spanBo.getAgentId();
            final NameSpaceInfo nameSpaceInfo = (NameSpaceInfo)RequestContextHolder.getAttributes().getAttribute(NameSpaceInfo.NAMESPACE_INFO);
            final String organization = nameSpaceInfo.getOrganization();
            final long spanTime = timeSlot.getTimeSlot(spanBo.getStartTime());

            spanStatData.incrementSpanCount(new SpanStatKey(organization, applicationId, agentId, spanTime));
        } catch (Exception e) {
            logger.error("occur exception while count span stat.", e);
        }
    }

    public Map<SpanStatKey, Long> getSnapshotAndEmptySpanStatData() {
        return spanStatData.getSnapshotAndRemoveSpanStatData();
    }
}
