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
package com.navercorp.pinpoint.batch.service;

import com.navercorp.pinpoint.batch.dao.SpanStatAgentDao;
import com.navercorp.pinpoint.batch.vo.ApplicationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author minwoo.jung
 */
@Service
public class SpanStatAgentService {

    @Autowired
    SpanStatAgentDao spanStatAgentDao;

    public List<ApplicationInfo> selectApplicationList() {
        return spanStatAgentDao.selectApplicationList();
    }

    public void deleteSpanStatAgent(ApplicationInfo applicationInfo, long boundaryTime) {
        spanStatAgentDao.deleteSpanStatAgent(applicationInfo, boundaryTime);
    }
}
