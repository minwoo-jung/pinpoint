/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.flink.process;

import com.navercorp.pinpoint.common.server.bo.stat.join.*;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.flink.namespace.FlinkAttributes;
import com.navercorp.pinpoint.flink.namespace.FlinkContextHolder;
import com.navercorp.pinpoint.flink.namespace.FlinkContextInterceptor;
import com.navercorp.pinpoint.flink.namespace.vo.PaaSOrganizationInfo;
import com.navercorp.pinpoint.flink.vo.RawData;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.util.FlinkRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class PaaSTBaseFlatMapperInterceptor extends FlinkContextInterceptor implements TBaseFlatMapperInterceptor {

    @Override
    public void before(RawData rawData) {
        String organization = rawData.getMetaInfo("organization");
        String databaseName = rawData.getMetaInfo("databaseName");
        String hbaseNameSpace = rawData.getMetaInfo("hbaseNameSpace");
        initFlinkcontextHolder(new PaaSOrganizationInfo(organization, FLINK, databaseName, hbaseNameSpace));
    }

    @Override
    public void after() {
        FlinkContextHolder.resetAttributes();
    }

    @Override
    public List<Tuple3<String, JoinStatBo, Long>> middle(final List<Tuple3<String, JoinStatBo, Long>> outDataList) {
        if (CollectionUtils.isEmpty(outDataList)) {
            return outDataList;
        }
        FlinkAttributes attributes = FlinkContextHolder.getAttributes();
        if (attributes == null) {
            return outDataList;
        }
        PaaSOrganizationInfo paaSOrganizationInfo = (PaaSOrganizationInfo) attributes.getAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO);
        if (paaSOrganizationInfo == null) {
            return outDataList;
        }

        List<Tuple3<String, JoinStatBo, Long>> convertedOutData = new ArrayList<>(outDataList.size());
        for (Tuple3<String, JoinStatBo, Long> outData : outDataList) {
            StringBuilder sb = new StringBuilder();
            sb.append(paaSOrganizationInfo.getOrganization());
            sb.append(TUPLE_KEY_DELIMITER);
            sb.append(outData.f0);
            final String tupleKey = sb.toString();
            final Long timestamp = outData.f2;
            final JoinStatBo paaSJoinStatBo;

            if (outData.f1 instanceof JoinAgentStatBo) {
                paaSJoinStatBo = new PaaSJoinAgentStatBo(paaSOrganizationInfo, (JoinAgentStatBo)outData.f1);
            } else if (outData.f1 instanceof JoinApplicationStatBo) {
                paaSJoinStatBo = new PaaSJoinApplicationStatBo(paaSOrganizationInfo, (JoinApplicationStatBo) outData.f1);
            } else {
                throw new FlinkRuntimeException("can not find type for joinStatBo : " + outData.f1);
            }

            convertedOutData.add(new Tuple3<>(tupleKey, paaSJoinStatBo, timestamp));
        }

        return convertedOutData;
    }
}
