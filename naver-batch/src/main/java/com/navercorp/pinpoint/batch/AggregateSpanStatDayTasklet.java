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

import com.navercorp.pinpoint.batch.service.SpanStatOrganizationService;
import com.navercorp.pinpoint.batch.vo.TimeRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author minwoo.jung
 */
public class AggregateSpanStatDayTasklet implements Tasklet {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final static long DAY_MILLIS = 86400000L;
    private final static long NINE_HOUR = 32400000L;
    private final static long RANGE = 86399000L;

    @Autowired
    SpanStatOrganizationService spanStatOrganizationService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        long dayUnitTime = currentDayUnitTime() - DAY_MILLIS;

        for (int i = 0; i <= 8; i++) {
            TimeRange timeRange = new TimeRange(dayUnitTime, dayUnitTime + RANGE);
            dayUnitTime = dayUnitTime - DAY_MILLIS;
            boolean exist = spanStatOrganizationService.existSpanStatOrganization(timeRange);

            if (exist) {
                logger.info("data exist for timeRange({})", timeRange.prettyToString());
            } else {
                logger.info("insert data for timeRange({})", timeRange.prettyToString());
                spanStatOrganizationService.insertSpanStatOrganization(timeRange);
            }
        }

        return RepeatStatus.FINISHED;
    }

    public long currentDayUnitTime() {
        long currentTimeMillis = System.currentTimeMillis();
        long remainder = currentTimeMillis % DAY_MILLIS;
        return currentTimeMillis - remainder - NINE_HOUR;
    }
}

