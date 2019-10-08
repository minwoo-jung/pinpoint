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
package com.navercorp.pinpoint.batch;

import com.navercorp.pinpoint.batch.service.SpanStatAgentService;
import com.navercorp.pinpoint.batch.service.SpanStatApplicationService;
import com.navercorp.pinpoint.batch.vo.ApplicationInfo;
import com.navercorp.pinpoint.batch.vo.TimeRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class AggregateSpanStatHourTasklet implements Tasklet {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final static long HOUR_MILLIS = 3600000L;
    private final static long RANGE = 3599000L;

    @Autowired
    SpanStatApplicationService spanStatApplicationService;

    @Autowired
    SpanStatAgentService spanStatAgentService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        long hourUnitTime = currentHourUnitTime() - HOUR_MILLIS;
        final List<ApplicationInfo> applicationInfoList = spanStatAgentService.selectApplicationList();

        for (int i = 0; i <= 8; i++) {
            TimeRange timeRange = new TimeRange(hourUnitTime, hourUnitTime + RANGE);
            hourUnitTime = hourUnitTime - HOUR_MILLIS;
            insertSpanStatApplication(applicationInfoList, timeRange);
        }

        return RepeatStatus.FINISHED;
    }

    private void insertSpanStatApplication(List<ApplicationInfo> applicationInfoList, TimeRange timeRange) {
        for (ApplicationInfo applicationInfo : applicationInfoList) {
            boolean exist = spanStatApplicationService.existSpanStatApplication(applicationInfo, timeRange);

            if (exist) {
                logger.info("data exist for applicationInfo({}), timeRange({})", applicationInfo, timeRange.prettyToString());
            } else {
                logger.info("insert data for applicationInfo({}), timeRange({})", applicationInfo, timeRange.prettyToString());
                spanStatApplicationService.insertSpanStatApplication(applicationInfo, timeRange);
            }
        }

    }

    private long currentHourUnitTime() {
        long currentTimeMillis = System.currentTimeMillis();
        long remainder = currentTimeMillis % HOUR_MILLIS;
        return currentTimeMillis - remainder;
    }
}
