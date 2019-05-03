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

import com.navercorp.pinpoint.flink.job.span.stat.dao.SpanStatAgentDao;
import com.navercorp.pinpoint.flink.job.span.stat.vo.SpanStatAgentVo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class SpanStatAgentService {

    private final SpanStatAgentDao spanStatAgentDao;

    public SpanStatAgentService(SpanStatAgentDao spanStatAgentDao) {
        this.spanStatAgentDao = Objects.requireNonNull(spanStatAgentDao, "spanStatAgentDao must not be null");
    }

    public void insertSpanStatAgentVo(SpanStatAgentVo spanStatAgentVo) {
        spanStatAgentDao.insertSpanStatAgentVo(spanStatAgentVo);
    }
}
