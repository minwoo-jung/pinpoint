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
import com.navercorp.pinpoint.batch.vo.ApplicationInfo;
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
public class DeleteAgentSpanStatTasklet implements Tasklet {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final static long HOUR_MILLIS = 3600000L;
    private final long range;

    @Autowired
    SpanStatAgentService spanStatAgentService;

    public DeleteAgentSpanStatTasklet(NaverBatchConfiguration naverBatchConfiguration) {
        range = naverBatchConfiguration.getDeleteRangeAgentSpanStat();
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        long boundaryTime = calcurateBoundaryTime();

        List<ApplicationInfo> applicationInfoList = spanStatAgentService.selectApplicationList();

        for(ApplicationInfo applicationInfo : applicationInfoList) {
            spanStatAgentService.deleteSpanStatAgent(applicationInfo, boundaryTime);
        }

        return RepeatStatus.FINISHED;
    }

    public long calcurateBoundaryTime() {
        long currentTimeMillis = System.currentTimeMillis();
        long remainder = currentTimeMillis % HOUR_MILLIS;
        return currentTimeMillis - remainder - range;
    }
}
