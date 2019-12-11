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
package com.navercorp.pinpoint.flink.job.span.stat.service;

import com.navercorp.pinpoint.flink.job.span.stat.Bootstrap;
import com.navercorp.pinpoint.flink.job.span.stat.vo.SpanStatAgentKey;
import com.navercorp.pinpoint.flink.job.span.stat.vo.SpanStatAgentVo;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.io.RichOutputFormat;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author minwoo.jung
 */
public class SpanStatService extends RichOutputFormat<Tuple3<SpanStatAgentKey, Long, Long>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private transient SpanStatAgentService spanStatAgentService;

    @Override
    public void configure(Configuration parameters) {
        ExecutionConfig.GlobalJobParameters globalJobParameters = getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        Bootstrap bootstrap = Bootstrap.getInstance(globalJobParameters.toMap());
        spanStatAgentService = bootstrap.getSpanStatAgentService();
    }

    @Override
    public void writeRecord(Tuple3<SpanStatAgentKey, Long, Long> value) throws IOException {
        SpanStatAgentVo spanStatAgentVo = new SpanStatAgentVo(value.f0, value.f1, value.f2);
        if (logger.isDebugEnabled()) {
            logger.debug("insert spanStatAgentVo : {}", spanStatAgentVo);
        }

        try {
            spanStatAgentService.insertSpanStatAgentVo(spanStatAgentVo);
        } catch (Exception e) {
            logger.error("occur exception while insert spanStatAgentVo. : {}", spanStatAgentVo, e);
        }
    }

    @Override
    public void open(int taskNumber, int numTasks) throws IOException {

    }

    @Override
    public void close() throws IOException {

    }
}

