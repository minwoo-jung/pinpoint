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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinApplicationStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.PaaSJoinApplicationStatBo;
import com.navercorp.pinpoint.flink.namespace.FlinkAttributes;
import com.navercorp.pinpoint.flink.namespace.FlinkContextHolder;
import com.navercorp.pinpoint.flink.namespace.vo.PaaSOrganizationInfo;
import org.apache.flink.api.java.tuple.Tuple3;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class PaaSStatisticsDaoInterceptorTest {

    @Test
    public void test() {
        PaaSOrganizationInfo paaSOrganizationInfo = new PaaSOrganizationInfo("company", "userId", "databaseName", "hbaseNameSpace");
        PaaSJoinApplicationStatBo paaSJoinApplicationStatBo = new PaaSJoinApplicationStatBo(paaSOrganizationInfo, new JoinApplicationStatBo());

        assertNull(FlinkContextHolder.getAttributes());

        PaaSStatisticsDaoInterceptor paaSStatisticsDaoInterceptor = new PaaSStatisticsDaoInterceptor();
        Tuple3<String, JoinStatBo, Long> data = new Tuple3<String, JoinStatBo, Long>("key", paaSJoinApplicationStatBo, Long.MIN_VALUE);
        paaSStatisticsDaoInterceptor.before(data);

        FlinkAttributes attributes = FlinkContextHolder.getAttributes();
        assertEquals(paaSOrganizationInfo, attributes.getAttribute(PaaSOrganizationInfo.PAAS_ORGANIZATION_INFO));

        paaSStatisticsDaoInterceptor.after();
        assertNull(FlinkContextHolder.getAttributes());
    }

    @Test
    public void test2() {
        assertNull(FlinkContextHolder.getAttributes());

        PaaSStatisticsDaoInterceptor paaSStatisticsDaoInterceptor = new PaaSStatisticsDaoInterceptor();
        Tuple3<String, JoinStatBo, Long> data = new Tuple3<String, JoinStatBo, Long>("key", new EmptyJoinStatBo(), Long.MIN_VALUE);
        paaSStatisticsDaoInterceptor.before(data);

        assertNull(FlinkContextHolder.getAttributes());
    }

    private class EmptyJoinStatBo implements JoinStatBo {

        @Override
        public long getTimestamp() {
            return 0;
        }

        @Override
        public String getId() {
            return null;
        }
    }
}