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
import com.navercorp.pinpoint.flink.namespace.vo.PaaSOrganizationInfo;
import org.apache.flink.api.java.tuple.Tuple3;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class PaaSApplicationStatBoWindowInterceptorTest {

    @Test
    public void test() {
        PaaSApplicationStatBoWindowInterceptor paaSApplicationStatBoWindowInterceptor = new PaaSApplicationStatBoWindowInterceptor();

        assertNull(FlinkContextHolder.getAttributes());

        PaaSOrganizationInfo paaSOrganizationInfo = new PaaSOrganizationInfo("company", "userId", "databaseName", "hbaseNameSpace");
        PaaSJoinApplicationStatBo paaSJoinApplicationStatBo = new PaaSJoinApplicationStatBo(paaSOrganizationInfo, new JoinApplicationStatBo());
        Tuple3<String, JoinStatBo, Long> data = new Tuple3<String, JoinStatBo, Long>("key", paaSJoinApplicationStatBo, Long.MIN_VALUE);
        List<Tuple3<String, JoinStatBo, Long>> datas = new ArrayList<>();
        datas.add(data);
        paaSApplicationStatBoWindowInterceptor.before(datas);

        FlinkAttributes attributes = FlinkContextHolder.getAttributes();
        assertEquals(paaSOrganizationInfo, attributes.getAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO));


        Tuple3<String, JoinStatBo, Long> returnData = paaSApplicationStatBoWindowInterceptor.middle(data);
        assertTrue(returnData.f1 instanceof PaaSJoinApplicationStatBo);

        paaSApplicationStatBoWindowInterceptor.after();
        assertNull(FlinkContextHolder.getAttributes());
    }

}