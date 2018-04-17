/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.flink.dao.hbase;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.PaaSJoinAgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.PaaSJoinApplicationStatBo;
import com.navercorp.pinpoint.flink.namespace.FlinkAttributes;
import com.navercorp.pinpoint.flink.namespace.FlinkContextHolder;
import com.navercorp.pinpoint.flink.namespace.FlinkContextInterceptor;
import com.navercorp.pinpoint.flink.namespace.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.flink.receiver.TcpSourceFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author minwoo.jung
 */
public class PaaSStatisticsDaoInterceptor extends FlinkContextInterceptor implements StatisticsDaoInterceptor {

    @Override
    public void before(Tuple3<String, JoinStatBo, Long> statData) {
        if (!(statData.f1 instanceof PaaSJoinApplicationStatBo)) {
            return;
        }

        initFlinkcontextHolder(((PaaSJoinApplicationStatBo) statData.f1).getPaaSOrganizationInfo());
    }

    @Override
    public void after() {
        resetFlinkcontextHolder();
    }
}
