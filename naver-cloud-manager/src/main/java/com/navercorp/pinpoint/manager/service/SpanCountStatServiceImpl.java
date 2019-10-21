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
package com.navercorp.pinpoint.manager.service;

import com.navercorp.pinpoint.manager.dao.SpanCountStatDao;
import com.navercorp.pinpoint.manager.vo.Range;
import com.navercorp.pinpoint.manager.vo.SpanCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service
public class SpanCountStatServiceImpl implements SpanCountStatService {

    @Autowired
    private SpanCountStatDao spanCountStatDao;

    public SpanCountStatServiceImpl(SpanCountStatDao spanCountStatDao) {
        this.spanCountStatDao = Objects.requireNonNull(spanCountStatDao, "spanCountStatDao");
    }

    @Override
    public List<SpanCount> getOrganizationSpanCount(String organzationName, Range range) {
        return spanCountStatDao.selectOrganizationSpanCount(organzationName, range);
    }

    @Override
    public List<SpanCount> getApplicationSpanCount(String organzationName, String applicationId, Range range) {
        return spanCountStatDao.selectApplicationSpanCount(organzationName, applicationId, range);
    }

    @Override
    public List<SpanCount> getAgentSpanCount(String organzationName, String applicationId, String agentId, Range range) {
        return spanCountStatDao.selectAgentSpanCount(organzationName, applicationId, agentId, range);
    }
}
