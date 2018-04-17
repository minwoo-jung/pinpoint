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
package com.navercorp.pinpoint.flink.function;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinApplicationStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.PaaSJoinApplicationStatBo;
import com.navercorp.pinpoint.flink.namespace.FlinkAttributes;
import com.navercorp.pinpoint.flink.namespace.FlinkContextHolder;
import com.navercorp.pinpoint.flink.namespace.FlinkContextInterceptor;
import com.navercorp.pinpoint.flink.namespace.vo.PaaSOrganizationInfo;
import org.apache.flink.api.java.tuple.Tuple3;

/**
 * @author minwoo.jung
 */
public class PaaSApplicationStatBoWindowInterceptor extends FlinkContextInterceptor implements ApplicationStatBoWindowInterceptor {

    @Override
    public void before(Iterable<Tuple3<String, JoinStatBo, Long>> values) {
        Tuple3<String, JoinStatBo, Long> value = values.iterator().next();
        if (!(value.f1 instanceof PaaSJoinApplicationStatBo)) {
            return;
        }

        initFlinkcontextHolder(((PaaSJoinApplicationStatBo) value.f1).getPaaSOrganizationInfo());
    }

    @Override
    public Tuple3<String, JoinStatBo, Long> middle(final Tuple3<String, JoinStatBo, Long> value) {
        FlinkAttributes attributes = FlinkContextHolder.getAttributes();
        if (attributes == null) {
            return value;
        }
        PaaSOrganizationInfo paaSOrganizationInfo = (PaaSOrganizationInfo) attributes.getAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO);
        if (paaSOrganizationInfo == null) {
            return value;
        }

        final String tupleKey = value.f0;
        final Long timestamp = value.f2;
        final JoinStatBo paaSJoinStatBo = new PaaSJoinApplicationStatBo(paaSOrganizationInfo, (JoinApplicationStatBo) value.f1);

        return new Tuple3<>(tupleKey, paaSJoinStatBo, timestamp);
    }

    @Override
    public void after() {
        resetFlinkcontextHolder();
    }
}
